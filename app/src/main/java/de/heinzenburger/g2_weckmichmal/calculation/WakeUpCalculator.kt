package de.heinzenburger.g2_weckmichmal.calculation

import de.heinzenburger.g2_weckmichmal.specifications.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class WakeUpCalculator(
    private val routePlanner: I_RoutePlannerSpecification,
    private val courseFetcher: I_CoursesFetcherSpecification
) : I_WakeUpCalculationSpecification {

    override fun calculateNextEvent(configuration: ConfigurationEntity): EventEntity {
        val nextDate = getNextValidDate(configuration.days)
        return calculateEventForDate(configuration, nextDate)
    }

    override fun batchCalculateNextEvent(configurations: List<ConfigurationEntity>): List<EventEntity> {
        val configsWithDates = configurations.map { it to getNextValidDate(it.days) }
        return batchCalculateEventForDates(configsWithDates)
    }

    private fun batchCalculateEventForDates(
        configsWithDates: List<Pair<ConfigurationEntity, LocalDate>>
    ): List<EventEntity> {
        val batchedConfigs = configsWithDates.mapIndexed { index, pair ->
            BatchTuple(index.toLong(), pair)
        }

        val batchedAtPlaceTimes = batchDeriveAtPlaceTimes(batchedConfigs)

        return batchedAtPlaceTimes.map { (id, value) ->
            val (config, date) = batchedConfigs.first { it.id == id }.value
            val (atPlaceTime, _) = value

            createEventEntity(config, date, atPlaceTime)
        }
    }

    private fun calculateEventForDate(
        config: ConfigurationEntity,
        date: LocalDate
    ): EventEntity {
        val (atPlaceTime, _) = deriveAtPlaceTime(config, date)
        return createEventEntity(config, date, atPlaceTime)
    }

    private fun createEventEntity(
        config: ConfigurationEntity,
        date: LocalDate,
        atPlaceTime: LocalDateTime
    ): EventEntity {
        val arrivalTime = atPlaceTime.minusMinutes(config.endBuffer.toLong())
        val (departureTime, _) = deriveDepartureTime(config, arrivalTime)
        val wakeUpTime = departureTime.minusMinutes(config.startBuffer.toLong())

        return EventEntity(
            configID = config.uid,
            wakeUpTime = wakeUpTime.toLocalTime(),
            date = date,
            days = config.days
        )
    }

    private fun deriveAtPlaceTime(
        config: ConfigurationEntity,
        eventDate: LocalDate
    ): Pair<LocalDateTime, List<Course>?> {
        return config.fixedArrivalTime?.let {
            it.atDate(eventDate) to null
        } ?: run {
            val period = Period(eventDate.atStartOfDay(), eventDate.atTime(23, 59))
            val courses = courseFetcher.fetchCoursesBetween(period)
            val firstCourse = courses.minByOrNull { it.startDate }
                ?: throw IllegalArgumentException("No courses found")
            firstCourse.startDate to courses
        }
    }

    private fun batchDeriveAtPlaceTimes(
        configs: List<BatchTuple<Pair<ConfigurationEntity, LocalDate>>>
    ): List<BatchTuple<Pair<LocalDateTime, List<Course>>>> {
        val (withFixed, withoutFixed) = configs.partition { it.value.first.fixedArrivalTime != null }

        val fixedResults : List<BatchTuple<Pair<LocalDateTime, List<Course>>>> = withFixed.map { (id, value) ->
            val (config, date) = value
            BatchTuple(id, config.fixedArrivalTime!!.atDate(date) to emptyList())
        }

        val periods = withoutFixed.map { (id, value) ->
            val (_, date) = value
            BatchTuple(id, Period(date.atStartOfDay(), date.atTime(23, 59)))
        }

        val fetchedCourses = courseFetcher.batchFetchCoursesBetween(periods)

        val variableResults = periods.mapNotNull { bPeriod ->
            fetchedCourses.find { it.isSameBatch(bPeriod) }?.let { coursesBatch ->
                val earliestCourse = coursesBatch.value.minByOrNull { it.startDate }
                earliestCourse?.let { BatchTuple(bPeriod.id, it.startDate to coursesBatch.value) }
            }
        }

        return variableResults + fixedResults
    }

    private fun deriveDepartureTime(
        config: ConfigurationEntity,
        arrivalTime: LocalDateTime
    ): Pair<LocalDateTime, List<Route>?> {
        return when {
            config.fixedTravelBuffer != null -> {
                val departure = arrivalTime.minusMinutes(config.fixedTravelBuffer!!.toLong())
                departure to null
            }

            config.startStation != null && config.endStation != null -> {
                val routes = routePlanner.planRoute(
                    config.startStation!!,
                    config.endStation!!, arrivalTime)
                val selectedRoute = routes.maxByOrNull { it.startTime }
                    ?: throw IllegalArgumentException("No valid route found")
                selectedRoute.startTime to routes
            }

            else -> throw IllegalArgumentException("Invalid Configuration: Missing travel buffer or station info")
        }
    }

    private fun getNextValidDate(days: Set<DayOfWeek>): LocalDate {
        val today = LocalDate.now()
        return (0..6)
            .map { today.plusDays(it.toLong()) }
            .firstOrNull { it.dayOfWeek in days }
            ?: throw IllegalStateException("No valid event date found in selected days")
    }
}
