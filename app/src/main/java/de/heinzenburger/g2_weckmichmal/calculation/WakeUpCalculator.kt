package de.heinzenburger.g2_weckmichmal.calculation

import de.heinzenburger.g2_weckmichmal.api.rapla.Batch
import de.heinzenburger.g2_weckmichmal.api.rapla.batchFrom
import de.heinzenburger.g2_weckmichmal.api.rapla.innerJoinBranches
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.CourseFetcherSpecification
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_RoutePlannerSpecification
import de.heinzenburger.g2_weckmichmal.specifications.WakeUpCalculationSpecification
import de.heinzenburger.g2_weckmichmal.specifications.Period
import de.heinzenburger.g2_weckmichmal.specifications.Route
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

// Detailled description under https://gitlab.com/dhbw-se/se-tinf23b2/G2-WeckMichMal/g2-weckmichmal/-/wikis/home/Implementierung/WakeUpCalculation
class WakeUpCalculator(
    private val routePlanner: I_RoutePlannerSpecification,
    private val courseFetcher: CourseFetcherSpecification
) : WakeUpCalculationSpecification {

    override fun calculateNextEvent(configuration: ConfigurationEntity): EventEntity {
        val eventDate = deriveNextValidDate(configuration.days)
        return calculateEventForDate(configuration, eventDate)
    }

    private fun calculateEventForDate(
        configuration: ConfigurationEntity,
        date: LocalDate
    ): EventEntity {
        val (atPlaceTime, courses) = deriveAtPlaceTime(courseFetcher, configuration, date)
        val arrivalTime = atPlaceTime.minusMinutes(configuration.endBuffer.toLong())
        val (departureTime, routes) = deriveDepartureTime(routePlanner, configuration, arrivalTime)
        val wakeUpTime = departureTime.minusMinutes(configuration.startBuffer.toLong())

        return EventEntity(
            configID = configuration.uid,
            wakeUpTime = wakeUpTime.toLocalTime(),
            date = date,
            days = setOf(wakeUpTime.dayOfWeek),
            courses = courses,
            routes = routes
        )
    }


    override fun batchCalculateNextEvent(configurations: List<ConfigurationEntity>): List<EventEntity> {
        val configsWithDates = configurations.map { it to deriveNextValidDate(it.days) }
        return batchCalculateEventForDates(configsWithDates)
    }

    private fun batchCalculateEventForDates(
        configsWithDates: List<Pair<ConfigurationEntity, LocalDate>>
    ): List<EventEntity> {
        val batchedConfigs = batchFrom(configsWithDates)

        val batchedAtPlaceTimes = batchDeriveAtPlaceTimes(courseFetcher, batchedConfigs)

        return innerJoinBranches(batchedConfigs, batchedAtPlaceTimes)
            .map { batchEntry ->
                val (bConfigs, bAtPlaceTime) = batchEntry
                val (configuration, date) = bConfigs
                val (atPlaceTime, courses) = bAtPlaceTime

                val arrivalTime = atPlaceTime.minusMinutes(configuration.endBuffer.toLong())
                val (departureTime, routes) = deriveDepartureTime(
                    routePlanner,
                    configuration,
                    arrivalTime
                )
                val wakeUpTime = departureTime.minusMinutes(configuration.startBuffer.toLong())

                EventEntity(
                    configID = configuration.uid,
                    wakeUpTime = wakeUpTime.toLocalTime(),
                    date = date,
                    days = setOf(wakeUpTime.dayOfWeek),
                    courses = courses,
                    routes = routes
                )
            }
    }

    private companion object {
        fun deriveNextValidDate(daySelection: Set<DayOfWeek>): LocalDate {
            val today = LocalDate.now()
            return (0..6)
                .map { today.plusDays(it.toLong()) }
                .firstOrNull { it.dayOfWeek in daySelection }
                ?: throw IllegalStateException("No valid event date found in selected days")
        }

        fun deriveAtPlaceTime(
            courseFetcher: CourseFetcherSpecification,
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

        fun batchDeriveAtPlaceTimes(
            courseFetcher: CourseFetcherSpecification,
            configs: Batch<Pair<ConfigurationEntity, LocalDate>>
        ): Batch<Pair<LocalDateTime, List<Course>>> {
            val (withFixed, withoutFixed) = configs.partition { it.value.first.fixedArrivalTime != null }

            val fixedResults: Batch<Pair<LocalDateTime, List<Course>>> =
                withFixed.map { bEntry ->
                    val (config, date) = bEntry.value
                    bEntry.map(config.fixedArrivalTime!!.atDate(date) to emptyList())
                }

            val periods = withoutFixed.map { bEntry ->
                val (_, date) = bEntry.value
                bEntry.map(Period(date.atStartOfDay(), date.atTime(23, 59)))
            }

            val variableResults: Batch<Pair<LocalDateTime, List<Course>>> =
                courseFetcher.batchFetchCoursesBetween(periods)
                    .mapNotNull { batchEntry ->
                        val courses = batchEntry.value
                        val earliestCourse = courses.minByOrNull { it.startDate }
                        earliestCourse?.let {
                            batchEntry.map(it.startDate to courses)
                        }
                    }

            return variableResults + fixedResults
        }

        fun deriveDepartureTime(
            routePlanner: I_RoutePlannerSpecification,
            config: ConfigurationEntity,
            arrivalTime: LocalDateTime
        ): Pair<LocalDateTime, List<Route>?> {
            return when {
                config.fixedTravelBuffer != null -> {
                    val departure = arrivalTime.minusMinutes(config.fixedTravelBuffer!!.toLong())
                    departure to null
                }

                config.startStation != null && config.endStation != null -> {
                    val startStation = config.startStation!!
                    val endStation = config.endStation!!
                    val routes = routePlanner.planRoute(startStation, endStation, arrivalTime)
                    val selectedRoute = routes.maxByOrNull { it.startTime }
                        ?: throw IllegalStateException("No route found")
                    selectedRoute.startTime to routes
                }

                else -> throw IllegalArgumentException("Invalid Configuration: Missing travel buffer or station info")
            }
        }
    }
}
