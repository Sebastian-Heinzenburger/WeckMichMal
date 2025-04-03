package de.heinzenburger.g2_weckmichmal.calculation

import de.heinzenburger.g2_weckmichmal.specifications.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * WakeUpCalculator calculates the next wake-up event based on alarm configuration.
 * It determines the optimal wake-up time considering travel time, buffers, and scheduled courses.
 *
 * @property routePlanner Provides route planning functionality.
 * @property courseFetcher Fetches course schedules for a given date.
 */
class WakeUpCalculator(
    private val routePlanner: I_RoutePlannerSpecification,
    private val courseFetcher: I_CoursesFetcherSpecification
) : I_WakeUpCalculationSpecification {

    /**
     * Calculates the next event based on the provided alarm configuration.
     *
     * @param configuration The alarm configuration containing user preferences and settings.
     * @return The calculated Event or null if no valid event can be determined.
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun calculateNextEvent(configuration: AlarmConfiguration): Event {
        val eventDate: LocalDate = getNextDateOutOfSelection(configuration.selectedDays);
        return calculateEventForDate(configuration, eventDate);
    }

    /**
     * Computes an [Event] for a specific [DayOfWeek] based on the provided [AlarmConfiguration].
     *
     * This method verifies whether the specified day is part of the user's selected days.
     * If it is, it calculates the corresponding event date and determines the wake-up event accordingly.
     *
     * @param configuration The [AlarmConfiguration] containing user-defined preferences,
     * including selected days, time buffers, and stations.
     * @param day The [DayOfWeek] for which the event should be calculated.
     * @return The computed [Event] for the specified day.
     * @throws IllegalArgumentException if the specified day is not part of the selected days.
     */
    @Throws(IllegalArgumentException::class)
    override fun calculateEventForDay(configuration: AlarmConfiguration, day: DayOfWeek): Event {
        if(!configuration.selectedDays.contains(day)) throw IllegalArgumentException("The specified day ($day) is not included in the selected alarm days.")
        val eventDate: LocalDate = getNextDateOutOfSelection(setOf(day));

        return calculateEventForDate(configuration, eventDate);
    }

    /**
     * Computes an [Event] for a specific calendar date based on the provided [AlarmConfiguration].
     *
     * This method calculates the optimal wake-up time by determining the at-place arrival time,
     * adjusting for travel buffers, and selecting an appropriate route if applicable.
     *
     * @param configuration The [AlarmConfiguration] containing user-defined settings,
     * such as time buffers, stations, and fixed or course-based arrival times.
     * @param date The [LocalDate] for which the event should be computed.
     * @return The computed [Event] for the specified date.
     * @throws IllegalArgumentException if no valid course or travel route is found.
     */
    @Throws(IllegalArgumentException::class)
    override fun calculateEventForDate(configuration: AlarmConfiguration, date: LocalDate): Event {
        val (atPlaceTime, courses) = deriveAtPlaceTime(configuration, date)

        // Adjust arrival time with the end buffer
        val arrivalTime: LocalDateTime = atPlaceTime.minusMinutes(configuration.endBuffer.toLong())

        val (departureTime, route) = deriveDepartureTime(configuration, arrivalTime)

        // Adjust departure time with the start buffer
        val wakeUpTime = departureTime.minusMinutes(configuration.startBuffer.toLong())

        return Event(wakeUpTime.toLocalTime(), date.dayOfWeek, date, route, courses)
    }

    /**
     * Determines the at-place arrival time based on configuration and available courses.
     *
     * @return A pair containing the at-place arrival time and the list of relevant courses (if applicable).
     */
    private fun deriveAtPlaceTime(configuration: AlarmConfiguration, eventDate: LocalDate): Pair<LocalDateTime, List<Course>?> {
        return if (configuration.fixedArrivalTime != null) {
            // Fixed arrival time case
            Pair(configuration.fixedArrivalTime.atDate(eventDate), null)
        } else {
            // Determine arrival time based on available courses
            val courses: List<Course> = courseFetcher.fetchCoursesBetween(eventDate.atTime(0, 0), eventDate.atTime(23, 59))
            val firstCourse = courses.minByOrNull { it.startDate } ?: throw IllegalArgumentException("No courses found")
            Pair(firstCourse.startDate, courses)
        }
    }

    /**
     * Determines the departure time based on the provided configuration and arrival time.
     *
     * @throws IllegalArgumentException if the configuration is invalid.
     * @return A pair containing the calculated departure time and the selected route (if applicable).
     */
    @Throws(IllegalArgumentException::class)
    private fun deriveDepartureTime(configuration: AlarmConfiguration, arrivalTime: LocalDateTime): Pair<LocalDateTime, Route?> {
        return when {
            configuration.fixedTravelBuffer != null -> {
                // Fixed travel buffer case
                val departureTime = arrivalTime.minusMinutes(configuration.fixedTravelBuffer.toLong())
                Pair(departureTime, null)
            }
            configuration.startStation != null && configuration.endStation != null -> {
                // Travel time based on route planner
                val routes: List<Route> = routePlanner.planRoute(configuration.startStation, configuration.endStation, arrivalTime)
                val route: Route = routes.maxByOrNull { it.startTime } ?: throw IllegalArgumentException("No valid route found")
                Pair(route.startTime, route)
            }
            else -> throw IllegalArgumentException("Invalid Configuration: Missing travel buffer or station details")
        }
    }

    /**
     * Finds the next available date based on the selected days of the week.
     *
     * @param selectedDays The set of preferred days for wake-up.
     * @return The next valid date or null if no suitable day is found.
     */
    @Throws(IllegalStateException::class)
    private fun getNextDateOutOfSelection(selectedDays: Set<DayOfWeek>): LocalDate {
        var date = LocalDate.now()

        repeat(7) {
            if (selectedDays.contains(date.dayOfWeek)) {
                return date
            }
            date = date.plusDays(1)
        }

        throw IllegalStateException("No valid event date found in selectedDays")
    }
}