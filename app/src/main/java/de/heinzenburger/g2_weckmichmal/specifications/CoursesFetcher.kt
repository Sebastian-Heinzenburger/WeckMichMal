package de.heinzenburger.g2_weckmichmal.specifications

import java.time.LocalDateTime
import java.time.temporal.Temporal

/**
 * Interface defining the behavior for fetching courses.
 */
interface I_CoursesFetcherSpecification {

    /**
     * Fetches a list of courses within a specified time range.
     *
     * @param start The start date and time of the time range.
     * @param end The end date and time of the time range.
     * @return A list of [Course] objects representing the courses within the specified time range.
     */
    @Throws(Exception::class)
    fun fetchCoursesBetween(period: Period): List<Course>


    fun batchFetchCoursesBetween(periods: List<BatchTuple<Period>>): List<BatchTuple<List<Course>>>
}

data class BatchTuple<T> (
    val id: Long,
    val value: T
) {
    fun isSameBatch(otherTuple: BatchTuple<*>): Boolean {
        return id  == otherTuple.id
    }
}


data class Period (
    val start: LocalDateTime,
    val end: LocalDateTime
) {
    fun toCalPeriod(): net.fortuna.ical4j.model.Period<Temporal> {
        return net.fortuna.ical4j.model.Period(start, end)
    }
}

/**
 * Represents a course as defined by RAPLA.
 *
 * @property name The name of the course.
 * @property lecturer The name of the lecturer.
 * @property room The room number where the course is held.
 * @property startDate The date and time when the course starts.
 * @property endDate The date and time when the course ends.
 */
data class Course (

    /** Name of the Course */
    val name: String?,

    /** Name of the Lecturer */
    val lecturer: String?,

    /** Room number of the Course */
    val room: String?,

    /** Date and Time at which the course starts */
    val startDate: LocalDateTime,

    /** Date and Time at which the course ends */
    val endDate: LocalDateTime
)