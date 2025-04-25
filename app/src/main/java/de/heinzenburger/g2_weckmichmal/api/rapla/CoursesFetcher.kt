package de.heinzenburger.g2_weckmichmal.api.rapla

import de.heinzenburger.g2_weckmichmal.specifications.BatchTuple
import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.I_CoursesFetcherSpecification
import de.heinzenburger.g2_weckmichmal.specifications.Period
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.property.Attendee
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.parameter.Cn
import net.fortuna.ical4j.model.property.Categories
import net.fortuna.ical4j.model.property.DtEnd
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.Location
import net.fortuna.ical4j.model.property.Summary
import java.io.InputStream
import java.net.URL
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import kotlin.collections.map
import kotlin.jvm.optionals.getOrNull

class CoursesFetcher(private val raplaUrl: URL): I_CoursesFetcherSpecification {

    private val validCategories = setOf<String>("Prüfung", "Lehrveranstaltung");

    @Throws(Exception::class)
    override fun fetchCoursesBetween(
        period: Period
    ): List<Course> {
        val inputStreamRAPLA = fetchRapla()?: throw Exception("Could not load RAPLA");

        return CalendarBuilder()
            .build(inputStreamRAPLA)
            .getComponents<VEvent>("VEVENT")
            .filter { c -> eventInCategory(c, validCategories) }
            .map { c -> c.getOccurrences(period.toCalPeriod()) }
            .flatten().mapNotNull { c -> eventToCourse(c) }
            .toList()
    }

    override fun batchFetchCoursesBetween(periods: List<BatchTuple<Period>>): List<BatchTuple<List<Course>>> {
        val inputStreamRAPLA = fetchRapla()?: throw Exception("Could not load RAPLA");

        val validEvents = CalendarBuilder()
            .build(inputStreamRAPLA)
            .getComponents<VEvent>("VEVENT")
            .filter { c -> eventInCategory(c, validCategories) }
            .filterNotNull();

        return periods.map { batchEntry ->
            BatchTuple(
                batchEntry.id,
                validEvents
                    .map { c -> c.getOccurrences(batchEntry.value.toCalPeriod()) }
                    .flatten().mapNotNull { c -> eventToCourse(c) }
                    .toList()
            )
        }
    }

    fun eventToCourse(e: VEvent): Course? {
        return try {
            val name = e.getProperty<Summary>("SUMMARY").getOrNull()?.value;
            val lecturer = e.getProperty<Attendee>("ATTENDEE")
                .getOrNull()
                ?.getParameter<Cn>("CN")
                ?.getOrNull()?.value
            val room = e.getProperty<Location>("LOCATION").getOrNull()?.value
            val optRange = getTimeRange(e);
            if (optRange == null) return null;
            return Course (name, lecturer, room, optRange.first, optRange.second)
        }catch (_: Exception) {
            return null;
        }
    }

    fun getTimeRange(e: VEvent): Pair<LocalDateTime, LocalDateTime>? {
        try {
            val start = e.getProperty<DtStart<ZonedDateTime>>("DTSTART").getOrNull()?.date?.toLocalDateTime()
            val end = e.getProperty<DtEnd<ZonedDateTime>>("DTEND").getOrNull()?.date?.toLocalDateTime()
            if(start == null || end == null) return null;
            return Pair (start, end)
        }catch (_: Exception){}

        try {
            val start = e.getProperty<DtStart<OffsetDateTime>>("DTSTART").getOrNull()?.date?.toLocalDateTime()
            val end = e.getProperty<DtEnd<OffsetDateTime>>("DTEND").getOrNull()?.date?.toLocalDateTime()
            if(start == null || end == null) return null;
            return Pair (start, end)
        }catch (_: Exception){
            return null;
        }
    }


    fun eventInCategory(vEvent: VEvent, validCategories: Set<String>): Boolean {
        return try {
            val optEventCategory = vEvent.getProperty<Categories>("CATEGORIES");
            if(optEventCategory.isEmpty) return false
            val eventCategories = optEventCategory.get().categories.texts.toSet()
            return !validCategories.intersect(eventCategories).isEmpty()
        }catch (_: Exception){
            return false;
        }
    }

    fun fetchRapla(): InputStream? {
        return try{
            raplaUrl.openConnection().inputStream
        } catch (_: Error) {
            return null;
        }
    }
}