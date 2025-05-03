package de.heinzenburger.g2_weckmichmal.specifications

import de.heinzenburger.g2_weckmichmal.api.rapla.Batch
import java.time.LocalDateTime

/**
 * Interface defining the contract for fetching course data from external systems.
 */
interface CourseFetcherSpecification {

    /**
     * Fetches all courses occurring within the specified period.
     *
     * This method retrieves and parses all events from the configured course system (e.g., RAPLA),
     * filters them by valid categories, expands recurring events, filters them by the provided period,
     * and maps them into [Course] objects.
     *
     * @param period The [Period] defining the start and end date/time of the time range.
     * @return A list of [Course] objects that fall within the specified time period.
     * @throws Exception if the course data cannot be fetched or parsed (e.g., due to network issues or invalid data).
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
