package de.heinzenburger.g2_weckmichmal.api.rapla

import java.time.LocalDateTime

interface Courses {
    fun fetchCoursesBetween(start: LocalDateTime, end: LocalDateTime): List<Course>
}
