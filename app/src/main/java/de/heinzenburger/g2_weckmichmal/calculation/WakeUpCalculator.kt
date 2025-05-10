package de.heinzenburger.g2_weckmichmal.calculation

import de.heinzenburger.g2_weckmichmal.api.courses.Batch
import de.heinzenburger.g2_weckmichmal.api.courses.batchFrom
import de.heinzenburger.g2_weckmichmal.api.courses.innerJoinBranches
import de.heinzenburger.g2_weckmichmal.specifications.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Wake-up time calculator implementation.
 * See: https://gitlab.com/dhbw-se/se-tinf23b2/G2-WeckMichMal/g2-weckmichmal/-/wikis/home/Implementierung/WakeUpCalculation
 */
class WakeUpCalculator(
    private val routePlanner: I_RoutePlannerSpecification,
    private val courseFetcher: CourseFetcherSpecification
) : WakeUpCalculationSpecification {

    @Throws(WakeUpCalculatorException::class)
    override fun calculateNextEvent(configuration: Configuration): Event {
        val eventDate = deriveNextValidDate(configuration.days)
        return calculateEventForDate(configuration, eventDate)
    }

    @Throws(
        WakeUpCalculatorException.NoCoursesFound::class,
        WakeUpCalculatorException.CoursesConnectionError::class,
        WakeUpCalculatorException.CoursesInvalidDataFormatError::class
    )
    private fun calculateEventForDate(configuration: Configuration, date: LocalDate): Event {
        val (atPlaceTime, courses) = deriveAtPlaceTime(courseFetcher, configuration, date)
        val arrivalTime = atPlaceTime.minusMinutes(configuration.endBuffer.toLong())
        val (departureTime, routes) = deriveDepartureTime(routePlanner, configuration, arrivalTime)
        val wakeUpTime = departureTime.minusMinutes(configuration.startBuffer.toLong())

        return Event(
            configID = configuration.uid,
            wakeUpTime = wakeUpTime.toLocalTime(),
            date = date,
            days = setOf(wakeUpTime.dayOfWeek),
            courses = courses,
            routes = routes
        )
    }

    @Throws(
        WakeUpCalculatorException.InvalidStateException::class,
        WakeUpCalculatorException.CoursesConnectionError::class,
        WakeUpCalculatorException.CoursesInvalidDataFormatError::class
    )
    override fun batchCalculateNextEvent(configurations: List<Configuration>): List<Event> {
        val configsWithDates = configurations.map { it to deriveNextValidDate(it.days) }
        return batchCalculateEventForDates(configsWithDates)
    }

    @Throws(
        WakeUpCalculatorException.CoursesConnectionError::class,
        WakeUpCalculatorException.CoursesInvalidDataFormatError::class
    )
    private fun batchCalculateEventForDates(configsWithDates: List<Pair<Configuration, LocalDate>>): List<Event> {
        val batchedConfigs = batchFrom(configsWithDates)
        val batchedAtPlaceTimes = batchDeriveAtPlaceTimes(courseFetcher, batchedConfigs)

        return innerJoinBranches(batchedConfigs, batchedAtPlaceTimes).map { batchEntry ->
            val (bConfigs, bAtPlaceTime) = batchEntry
            val (configuration, date) = bConfigs
            val (atPlaceTime, courses) = bAtPlaceTime

            val arrivalTime = atPlaceTime.minusMinutes(configuration.endBuffer.toLong())
            val (departureTime, routes) = deriveDepartureTime(routePlanner, configuration, arrivalTime)
            val wakeUpTime = departureTime.minusMinutes(configuration.startBuffer.toLong())

            Event(
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

        @Throws(WakeUpCalculatorException.InvalidStateException::class)
        fun deriveNextValidDate(daySelection: Set<DayOfWeek>): LocalDate {
            val today = LocalDate.now()
            return (0..6)
                .map { today.plusDays(it.toLong()) }
                .firstOrNull { it.dayOfWeek in daySelection }
                ?: throw WakeUpCalculatorException.InvalidStateException()
        }

        @Throws(
            WakeUpCalculatorException.NoCoursesFound::class,
            WakeUpCalculatorException.CoursesInvalidDataFormatError::class,
            WakeUpCalculatorException.CoursesConnectionError::class
        )
        fun deriveAtPlaceTime(
            courseFetcher: CourseFetcherSpecification,
            config: Configuration,
            eventDate: LocalDate
        ): Pair<LocalDateTime, List<Course>?> {
            return config.fixedArrivalTime?.let {
                it.atDate(eventDate) to null
            } ?: run {
                val period = Period(eventDate.atStartOfDay(), eventDate.atTime(23, 59))
                try {
                    val courses = courseFetcher.fetchCoursesBetween(period)
                    val firstCourse = courses.minByOrNull { it.startDate }
                        ?: throw WakeUpCalculatorException.NoCoursesFound()
                    firstCourse.startDate to courses
                } catch (e: CourseFetcherException.ConnectionError) {
                    throw WakeUpCalculatorException.CoursesConnectionError(e)
                } catch (e: CourseFetcherException.DataFormatError) {
                    throw WakeUpCalculatorException.CoursesInvalidDataFormatError(e)
                }
            }
        }

        @Throws(
            WakeUpCalculatorException.CoursesInvalidDataFormatError::class,
            WakeUpCalculatorException.CoursesConnectionError::class
        )
        fun batchDeriveAtPlaceTimes(
            courseFetcher: CourseFetcherSpecification,
            configs: Batch<Pair<Configuration, LocalDate>>
        ): Batch<Pair<LocalDateTime, List<Course>>> {
            val (withFixed, withoutFixed) = configs.partition { it.value.first.fixedArrivalTime != null }

            val fixedResults: Batch<Pair<LocalDateTime, List<Course>>> = withFixed.map { entry ->
                val (config, date) = entry.value
                entry.map(config.fixedArrivalTime!!.atDate(date) to emptyList())
            }

            val periods = withoutFixed.map { entry ->
                val (_, date) = entry.value
                entry.map(Period(date.atStartOfDay(), date.atTime(23, 59)))
            }

            return try {
                val variableResults = courseFetcher.batchFetchCoursesBetween(periods).mapNotNull { entry ->
                    val courses = entry.value
                    val earliestCourse = courses.minByOrNull { it.startDate }
                    earliestCourse?.let { entry.map(it.startDate to courses) }
                }

                variableResults + fixedResults
            } catch (e: CourseFetcherException.ConnectionError) {
                throw WakeUpCalculatorException.CoursesConnectionError(e)
            } catch (e: CourseFetcherException.DataFormatError) {
                throw WakeUpCalculatorException.CoursesInvalidDataFormatError(e)
            }
        }

        fun deriveDepartureTime(
            routePlanner: I_RoutePlannerSpecification,
            config: Configuration,
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
                        config.endStation!!,
                        arrivalTime
                    )
                    val selectedRoute = routes.maxByOrNull { it.startTime }
                        ?: throw Exception("") // TODO: Transform to WakeUpCalculatorException
                    selectedRoute.startTime to routes
                }

                else -> throw IllegalArgumentException(
                    "Invalid Configuration: Missing travel buffer or station info"
                ) // TODO: Transform to WakeUpCalculatorException
            }
        }
    }
}
