package de.heinzenburger.g2_weckmichmal.specifications

import de.heinzenburger.g2_weckmichmal.api.rapla.Batch
import java.time.LocalDateTime

/**
 * Interface defining the contract for fetching course data from external systems.
 */
interface CourseFetcherSpecification {

    /**
    * Determines the next wake-up event based on the provided configuration.
    *
    * The calculation includes:
    * - Identifying the next valid date based on the configured active days.
    * - Determining the arrival time at the destination (e.g., from course schedules or a fixed target time).
    * - Calculating the required departure time, factoring in the selected route and estimated travel duration.
    * - Subtracting the configured wake-up buffer to arrive at the final wake-up time.
    *
    * WARNING: The "next valid date" always refers to the next scheduled occurrence of the event,
    * based on the configuration. The reference point is **LocalDate**, not LocalDateTime.
    * 
    * @param configuration The [Configuration] containing alarm settings, including buffers,
    * travel preferences, station details, and active weekdays.
    * @return A calculated [Event] including the wake-up time, event date,
    * associated courses, and travel routes.
    * @throws Exception if the calculation fails (e.g., due to missing course data or invalid configuration).
    */
    @Throws(Exception::class)
    fun fetchCoursesBetween(period: Period): List<Course>

    /**
     * Checks whether the course data source (e.g., RAPLA URL) is reachable and returns valid data.
     *
     * This is typically done by performing a minimal fetch to verify connectivity and data format.
     *
     * @return true if the course data source is valid and reachable, false otherwise.
     */
    fun hasValidCourseURL(): Boolean

    /**
     * Fetches courses for multiple periods in batches.
     *
     * This method retrieves all events once from the external system, expands recurring events,
     * and partitions them into batches by matching them against the provided periods.
     *
     * @param periods A [Batch] of [Period]s, each associated with an identifier.
     * @return A [Batch] of [List<Course>]s, each paired with the corresponding identifier.
     * @throws Exception if the course data cannot be fetched or parsed.
     */
    @Throws(Exception::class)
    fun batchFetchCoursesBetween(periods: Batch<Period>): Batch<List<Course>>
}

/**
 * Represents a time range with a start and end date/time.
 *
 * @property start The start date and time of the period.
 * @property end The end date and time of the period.
 */
data class Period(
    val start: LocalDateTime,
    val end: LocalDateTime
)

/**
 * Represents a course retrieved from the external course system (e.g., RAPLA).
 *
 * @property name The name or title of the course.
 * @property lecturer The name of the lecturer or instructor.
 * @property room The room or location where the course takes place.
 * @property startDate The date and time when the course begins.
 * @property endDate The date and time when the course ends.
 */
data class Course(
    val name: String?,
    val lecturer: String?,
    val room: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)
