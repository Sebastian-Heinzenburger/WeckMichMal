package de.heinzenburger.g2_weckmichmal.api.rapla

import de.heinzenburger.g2_weckmichmal.specifications.BatchTuple
import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.I_CoursesFetcherSpecification
import de.heinzenburger.g2_weckmichmal.specifications.Period
import biweekly.Biweekly
import biweekly.component.VEvent
import java.io.InputStream
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId


class CoursesFetcher(private val raplaUrl: URL) : I_CoursesFetcherSpecification {

    private val validCategories = setOf("Prüfung", "Lehrveranstaltung")

    override fun hasValidCourseURL(): Boolean {
        return try {
            fetchCoursesBetween(Period(LocalDateTime.now(), LocalDateTime.now().plusSeconds(1)))
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun fetchCoursesBetween(period: Period): List<Course> {
        val inputStreamRAPLA = fetchRapla() ?: throw Exception("Could not load RAPLA")

        val ical = Biweekly.parse(inputStreamRAPLA).first()
        return ical.events
            .filter { eventInCategory(it, validCategories) }
            .filter { eventInPeriod(it, period) }
            .mapNotNull { eventToCourse(it) }
    }

    override fun batchFetchCoursesBetween(periods: List<BatchTuple<Period>>): List<BatchTuple<List<Course>>> {
        val inputStreamRAPLA = fetchRapla() ?: throw Exception("Could not load RAPLA")

        val ical = Biweekly.parse(inputStreamRAPLA).first()
        val validEvents = ical.events.filter { eventInCategory(it, validCategories) }

        return periods.map { batchEntry ->
            val courses = validEvents
                .filter { eventInPeriod(it, batchEntry.value) }
                .mapNotNull { eventToCourse(it) }
            BatchTuple(batchEntry.id, courses)
        }
    }

    fun eventToCourse(e: VEvent): Course? {
        return try {
            val name = e.summary?.value
            val lecturer = e.attendees.firstOrNull()?.commonName
            val room = e.location.toString()
            val optRange = getTimeRange(e)
            if (optRange == null) return null
            Course(name, lecturer, room, optRange.first, optRange.second)
        } catch (_: Exception) {
            null
        }
    }

    fun getTimeRange(e: VEvent): Pair<LocalDateTime, LocalDateTime>? {
        return try {
            val startDate = e.dateStart?.value
            val endDate = e.dateEnd?.value
            if (startDate == null || endDate == null) return null
            val start = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault())
            val end = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault())
            Pair(start, end)
        } catch (_: Exception) {
            null
        }
    }

    fun eventInCategory(event: VEvent, validCategories: Set<String>): Boolean {
        return try {
            val eventCategories = event.categories.flatMap { it.values }.toSet()
            validCategories.intersect(eventCategories).isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    fun eventInPeriod(event: VEvent, period: Period): Boolean {
        val timeRange = getTimeRange(event) ?: return false
        return timeRange.first.isBefore(period.end) && timeRange.second.isAfter(period.start)
    }

    fun fetchRapla(): InputStream? {
        return try {
            raplaUrl.openConnection().getInputStream()
        } catch (_: Exception) {
            null
        }
    }
}
