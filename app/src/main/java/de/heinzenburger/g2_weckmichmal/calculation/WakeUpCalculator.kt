package de.heinzenburger.g2_weckmichmal.calculation

import de.heinzenburger.g2_weckmichmal.specifications.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Wake-up time calculator implementation.
 * See: https://gitlab.com/dhbw-se/se-tinf23b2/G2-WeckMichMal/g2-weckmichmal/-/wikis/home/Implementierung/WakeUpCalculation
 */
class WakeUpCalculator(
    private val routePlanner: RoutePlannerSpecification,
    private val courseFetcher: CourseFetcherSpecification
) : WakeUpCalculationSpecification {

    @Throws(WakeUpCalculatorException::class)
    override fun calculateNextEvent(configuration: Configuration, strict: Boolean, skipIfToday: Boolean): Event {
        var eventDate = deriveNextValidDate(configuration.days)
        if (skipIfToday && eventDate.isEqual(LocalDate.now())) {
            eventDate = deriveNextValidDate(configuration.days, LocalDate.now().plusDays(1))
        }

        return calculateEventForDate(configuration, eventDate, strict)
    }

    @Throws(
        WakeUpCalculatorException.NoCoursesFound::class,
        WakeUpCalculatorException.CoursesConnectionError::class,
        WakeUpCalculatorException.CoursesInvalidDataFormatError::class,
        WakeUpCalculatorException.RouteConnectionError::class,
        WakeUpCalculatorException.RouteInvalidConfiguration::class,
        WakeUpCalculatorException.RouteInvalidResponse::class,
        WakeUpCalculatorException.NoRoutesFound::class,
        WakeUpCalculatorException.InvalidConfiguration::class
    )
    private fun calculateEventForDate(configuration: Configuration, date: LocalDate, strict: Boolean): Event {
        val (atPlaceTime, courses) = deriveAtPlaceTime(courseFetcher, configuration, date)
        val arrivalTime = atPlaceTime.minusMinutes(configuration.endBuffer.toLong())
        val (departureTime, routes) = deriveDepartureTime(routePlanner, configuration, arrivalTime, strict)
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


    private companion object {

        @Throws(WakeUpCalculatorException.InvalidStateException::class)
        fun deriveNextValidDate(daySelection: Set<DayOfWeek>, referenceDay: LocalDate = LocalDate.now()): LocalDate {
            val today = referenceDay
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

        @Throws(WakeUpCalculatorException.RouteInvalidResponse::class, WakeUpCalculatorException.RouteInvalidConfiguration::class,
            WakeUpCalculatorException.RouteConnectionError::class, WakeUpCalculatorException.NoRoutesFound::class,
            WakeUpCalculatorException.InvalidConfiguration::class)
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
                        routes = if(!strict && config.enforceStartBuffer) {
                            routes.filter { it.startTime.isAfter(LocalDateTime.now().plusMinutes(config.startBuffer.toLong())) }
                        }else {
                            routes.filter { it.startTime.isAfter(LocalDateTime.now()) }
                        }
                        val selectedRoute = routes.maxByOrNull { it.startTime }
                            ?: throw WakeUpCalculatorException.NoRoutesFound()
                        selectedRoute.startTime to routes
                    }catch (e: RoutePlannerException) {
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
