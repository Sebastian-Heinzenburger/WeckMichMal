package de.heinzenburger.g2_weckmichmal.calculation

import de.heinzenburger.g2_weckmichmal.specifications.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Implementation of the [WakeUpCalculationSpecification] interface, responsible for calculating
 * the next wake-up event based on user configuration, course data, and route planning.
 *
 * @param routePlanner Interface for route planning to determine travel time.
 * @param courseFetcher Interface for fetching course schedules to determine arrival times.
 */
class WakeUpCalculator(
    private val routePlanner: RoutePlannerSpecification,
    private val courseFetcher: CourseFetcherSpecification
) : WakeUpCalculationSpecification {

    /**
     * Calculates the next [Event] (wake-up time) based on the provided [Configuration].
     *
     * @param configuration The configuration object defining wake-up rules and preferences.
     * @param strict If true, only exact matching routes are considered.
     * @return An [Event] object containing the calculated wake-up time and related info.
     * @throws WakeUpCalculatorException If any error occurs during calculation.
     */
    @Throws(WakeUpCalculatorException::class)
    override fun calculateNextEvent(configuration: Configuration, strict: Boolean): Event {
        val eventDate = deriveNextValidDate(configuration.days, configuration.ichHabGeringt)
        val (atPlaceTime, courses) = deriveAtPlaceTime(courseFetcher, configuration, eventDate)
        val arrivalTime = atPlaceTime.minusMinutes(configuration.endBuffer.toLong())
        val (departureTime, routes) = deriveDepartureTime(routePlanner, configuration, arrivalTime, strict)
        val wakeUpTime = departureTime.minusMinutes(configuration.startBuffer.toLong())

        return Event(
            configID = configuration.uid,
            wakeUpTime = wakeUpTime.toLocalTime(),
            date = eventDate,
            days = setOf(wakeUpTime.dayOfWeek),
            courses = courses,
            routes = routes
        )
    }

    private companion object {

        /**
         * Finds the next valid date from a given set of allowed weekdays.
         *
         * @param daySelection The set of valid [DayOfWeek] values.
         * @param skipDate Optional date to exclude (e.g., the last alarm date).
         * @return The next date matching one of the allowed weekdays.
         * @throws WakeUpCalculatorException.InvalidStateException If no valid day is found.
         */
        @Throws(WakeUpCalculatorException.InvalidStateException::class)
        fun deriveNextValidDate(daySelection: Set<DayOfWeek>, skipDate: LocalDate): LocalDate {
            val today = LocalDate.now()
            val referenceDate = if (skipDate.isEqual(today)) today.plusDays(1) else today
            return (0..6)
                .map { referenceDate.plusDays(it.toLong()) }
                .firstOrNull { it.dayOfWeek in daySelection }
                ?: throw WakeUpCalculatorException.InvalidStateException()
        }

        /**
         * Determines the target arrival time at the destination.
         *
         * If a fixed arrival time is configured, it is used. Otherwise, the earliest course is used.
         *
         * @param courseFetcher Interface to fetch courses for a given day.
         * @param config The user configuration.
         * @param eventDate The date to fetch or apply the arrival time to.
         * @return A pair of the arrival time and the list of courses (if applicable).
         * @throws WakeUpCalculatorException If course data cannot be retrieved or is invalid.
         */
        @Throws(
            WakeUpCalculatorException.EventDoesNotYetExist::class,
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
                        ?: throw WakeUpCalculatorException.EventDoesNotYetExist()
                    firstCourse.startDate to courses
                } catch (e: CourseFetcherException.ConnectionError) {
                    throw WakeUpCalculatorException.CoursesConnectionError(e)
                } catch (e: CourseFetcherException.DataFormatError) {
                    throw WakeUpCalculatorException.CoursesInvalidDataFormatError(e)
                }
            }
        }

        /**
         * Calculates the departure time based on configuration and planned arrival time.
         *
         * Uses either a fixed travel buffer or a dynamic route plan to determine when to leave.
         *
         * @param routePlanner Interface to plan routes.
         * @param config The user configuration.
         * @param arrivalTime The desired arrival time at the destination.
         * @param strict Whether to enforce strict timing on route selection.
         * @return A pair of the departure time and the list of routes (if used).
         * @throws WakeUpCalculatorException For routing errors or invalid configurations.
         */
        @Throws(
            WakeUpCalculatorException.RouteInvalidResponse::class,
            WakeUpCalculatorException.RouteInvalidConfiguration::class,
            WakeUpCalculatorException.RouteConnectionError::class,
            WakeUpCalculatorException.NoRoutesFound::class,
            WakeUpCalculatorException.InvalidConfiguration::class
        )
        fun deriveDepartureTime(
            routePlanner: RoutePlannerSpecification,
            config: Configuration,
            arrivalTime: LocalDateTime,
            strict: Boolean
        ): Pair<LocalDateTime, List<Route>?> {
            return when {
                config.fixedTravelBuffer != null -> {
                    val departure = arrivalTime.minusMinutes(config.fixedTravelBuffer!!.toLong())
                    departure to null
                }

                config.startStation != null && config.endStation != null -> {
                    try {
                        var routes = routePlanner.planRoute(
                            config.startStation!!,
                            config.endStation!!,
                            arrivalTime,
                            strict
                        )
                        routes = if (!strict && config.enforceStartBuffer) {
                            routes.filter {
                                it.startTime.isAfter(LocalDateTime.now().plusMinutes(config.startBuffer.toLong()))
                            }
                        } else {
                            routes.filter { it.startTime.isAfter(LocalDateTime.now()) }
                        }

                        val selectedRoute = routes.maxByOrNull { it.startTime }
                            ?: throw WakeUpCalculatorException.NoRoutesFound()
                        selectedRoute.startTime to routes
                    } catch (e: RoutePlannerException) {
                        throw when (e) {
                            is RoutePlannerException.NetworkException ->
                                WakeUpCalculatorException.RouteConnectionError(e)

                            is RoutePlannerException.MalformedStationNameException ->
                                WakeUpCalculatorException.RouteInvalidConfiguration(e)

                            is RoutePlannerException.InvalidResponseFormatException ->
                                WakeUpCalculatorException.RouteInvalidResponse(e)
                        }
                    }
                }
                else -> throw WakeUpCalculatorException.InvalidConfiguration(
                    "Configuration must either contain a fixed travel buffer OR a start and end station"
                )
            }
        }
    }
}
