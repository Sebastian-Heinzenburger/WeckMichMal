package de.heinzenburger.g2_weckmichmal.specifications

import java.time.LocalDateTime

/**
 * Interface defining the contract for fetching course data from external systems.
 *
 * This interface provides methods for retrieving course data, checking the validity of the course data source,
 * and fetching course data in batches. It also includes error handling for various scenarios like network issues
 * or data format problems during the fetching process.
 */
interface CourseFetcherSpecification {

    /**
     * Fetches courses between the specified time period.
     *
     * This method retrieves the list of courses that occur within the given [Period].
     * If there is any error in fetching or parsing the courses, a [CourseFetcherException] will be thrown.
     *
     * @param period The [Period] defining the time range for which courses are to be fetched.
     * @return A list of [Course] objects that occur within the specified period.
     * @throws CourseFetcherException if there is an error fetching or parsing the courses.
     *
     * This method is responsible for interacting with the external system to fetch the courses within a given time range.
     * If any issue occurs during the process, such as network problems or data parsing failures, a [CourseFetcherException]
     * will be thrown to indicate the failure.
     */
    @Throws(CourseFetcherException::class)
    fun fetchCoursesBetween(period: Period): List<Course>

    /**
     * Validates the course URL by attempting to fetch a test course range.
     *
     * This method checks whether the course URL is valid and reachable by invoking [fetchCoursesBetween]
     * with a short test period (e.g., one day from the current time). If the URL is invalid or unreachable,
     * a [CourseFetcherException] is thrown.
     *
     * @throws CourseFetcherException If the course URL is invalid or the fetch fails.
     */
    @Throws(CourseFetcherException::class)
    fun throwIfInvalidCourseURL()
}

/**
 * Represents a time range with a start and end date/time.
 *
 * This class is used to define a period of time between a start and end, which is useful when specifying
 * a time window for fetching courses.
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
 * This class contains information about a specific course, including its name, lecturer, room, and start and end times.
 * Courses are typically fetched from external systems for scheduling and event management purposes.
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

/**
 * Represents exceptions related to fetching course data.
 *
 * This sealed class defines the types of errors that can occur during the course-fetching process.
 * It includes specific error types for connection failures and data format issues. The exception includes
 * a message and an optional cause to help with troubleshooting and debugging.
 */
sealed class CourseFetcherException(message: String?, cause: Throwable?) :
    Throwable(message, cause) {

    /**
     * Represents a connection error that occurred during the course-fetching process.
     *
     * This could be due to network issues, an unreachable external system, or a timeout during the fetch process.
     *
     * @param cause The underlying exception that caused the connection error, such as a network timeout or unreachable server.
     */
    class ConnectionError(cause: Throwable?) : CourseFetcherException("Connection error", cause)

    /**
     * Represents a data format error during the course-fetching process.
     *
     * This error occurs when the fetched data is invalid or incorrectly formatted, causing issues during parsing or data
     * handling. Such errors may arise when the course data from the external system doesn't match the expected structure
     * or contains malformed entries.
     *
     * @param cause The underlying exception that caused the format error, such as invalid data structure or parsing failure.
     */
    class DataFormatError(cause: Throwable?) : CourseFetcherException("Data format error", cause)
}
