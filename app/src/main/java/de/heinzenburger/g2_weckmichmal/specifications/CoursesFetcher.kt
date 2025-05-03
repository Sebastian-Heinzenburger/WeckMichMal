package de.heinzenburger.g2_weckmichmal.specifications

import de.heinzenburger.g2_weckmichmal.api.rapla.BatchTuple
import java.time.LocalDateTime

/**
 * Interface defining the contract for fetching course data from external systems.
 */
interface CourseFetcherSpecification {

    /**
     * Fetches all courses occurring within the specified period.
     *
     * @param period The [Period] defining the start and end date/time of the time range.
     * @return A list of [Course] objects that fall within the specified time period.
     * @throws Exception if the course data cannot be fetched (e.g., due to network issues).
     */
    @Throws(Exception::class)
    fun fetchCoursesBetween(period: Period): List<Course>

    /**
     * Checks whether the course data source (URL) is reachable and returns valid data.
     *
     * @return true if the course data source is valid and reachable, false otherwise.
     */
    fun hasValidCourseURL(): Boolean

    /**
     * Fetches courses for multiple periods in batches.
     *
     * @param periods A list of [BatchTuple]s, each containing an identifier and a [Period].
     * @return A list of [BatchTuple]s where each contains the original identifier and the
     *         list of [Course]s found in the corresponding period.
     * @throws Exception if the course data cannot be fetched.
     */
    @Throws(Exception::class)
    fun batchFetchCoursesBetween(periods: List<BatchTuple<Period>>): List<BatchTuple<List<Course>>>
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
