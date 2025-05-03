package de.heinzenburger.g2_weckmichmal.api.rapla

import de.heinzenburger.g2_weckmichmal.specifications.BatchTuple
import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.I_CoursesFetcherSpecification
import de.heinzenburger.g2_weckmichmal.specifications.Period
import biweekly.Biweekly
import biweekly.component.VEvent
import biweekly.util.Frequency
import biweekly.util.ICalDate
import java.io.InputStream
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoUnit
import java.util.Date


class CoursesFetcher(
    private val raplaUrl: URL,
    private val validCourseCategories: Set<String> = setOf("Prüfung", "Lehrveranstaltung")
) : I_CoursesFetcherSpecification {

    override fun fetchCoursesBetween(period: Period): List<Course> {
        val icalStream: InputStream = fetchInputStream(raplaUrl) ?: throw Exception("Could not load RAPLA")

        return Biweekly.parse(icalStream).first()
            .events
            .filter { eventInCategory(it, validCourseCategories) }
            .map { expandOccurrences(it) }.flatten()
            .filter { eventInPeriod(it, period) }
            .mapNotNull { eventToCourse(it) }
    }

    override fun batchFetchCoursesBetween(periods: List<BatchTuple<Period>>): List<BatchTuple<List<Course>>> {
        val icalStream: InputStream = fetchInputStream(raplaUrl) ?: throw Exception("Could not load RAPLA")

        val events = Biweekly.parse(icalStream).first().events
        val eventsInCategory = events
            .filter { eventInCategory(it, validCourseCategories) }
            .map { expandOccurrences(it) }
            .flatten()

        return periods.map { batchPeriod ->
            val courses = eventsInCategory
                .filter { eventInPeriod(it, batchPeriod.value) }
                .mapNotNull { eventToCourse(it) }
            BatchTuple(batchPeriod.id, courses)
        }
    }

    override fun hasValidCourseURL(): Boolean {
        return try {
            fetchCoursesBetween(Period(LocalDateTime.now(), LocalDateTime.now().plusSeconds(1)))
            true
        }catch (_: Exception){
            false
        }
    }

    companion object {
        fun fetchInputStream(url: URL): InputStream? {
            return try {
                url.openConnection().inputStream
            }catch (_: Exception) {
                null
            }
        }

        fun eventInCategory(event: VEvent, validCategories: Set<String>): Boolean {
            return try {
                val eventCategories = event.categories.flatMap { it.values }.toSet();
                validCategories.intersect(eventCategories).isNotEmpty()
            } catch (_: Exception){
                false
            }
        }

        fun expandOccurrences(event: VEvent): List<VEvent> {
            try {
                val seriesStartDate = event.dateStart?.value?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                    ?: return emptyList()
                val seriesEndDate = event.dateEnd?.value?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                    ?: return emptyList()
                val eventDuration = ChronoUnit.SECONDS.between(seriesStartDate, seriesEndDate)

                val rrule = event.recurrenceRule
                if (rrule == null) {
                    return listOf(event)
                }

                val excludedDates = event.exceptionDates
                    .flatMap { it.values }
                    .mapNotNull {it.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()}.toSet()

                val frequency = rrule.value.frequency
                val untilDate = rrule.value.until?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                    ?: seriesStartDate.plusYears(1)

                val occurences = mutableSetOf<VEvent>()
                var iterDate = seriesStartDate

                while (iterDate.isBefore(untilDate)){
                    val occStartDate = iterDate.toLocalDate()
                    if(!excludedDates.contains(occStartDate)) {
                        val occEndDate = iterDate.plusSeconds(eventDuration)
                        val occurence = event.copy()
                        occurence.dateStart.value = ICalDate(Date.from(iterDate.atZone(ZoneId.systemDefault()).toInstant()))
                        occurence.dateEnd.value = ICalDate(Date.from(occEndDate.atZone(ZoneId.systemDefault()).toInstant()))
                        occurences.add(occurence)

                        val testStart = LocalDateTime.of(2025, 4, 28, 0, 0, 0)
                        val testEnd = LocalDateTime.of(2025, 5, 3, 0, 0, 0)
                        if(iterDate.isAfter(testStart) && iterDate.isBefore(testEnd)){
                            print(occurence.summary)
                            println(occurence.dateStart)
                        }
                    }

                    iterDate = when (frequency) {
                        Frequency.SECONDLY -> iterDate.plusSeconds(1)
                        Frequency.MINUTELY -> iterDate.plusMinutes(1)
                        Frequency.HOURLY -> iterDate.plusHours(1)
                        Frequency.DAILY -> iterDate.plusDays(1)
                        Frequency.WEEKLY -> iterDate.plusWeeks(1)
                        Frequency.MONTHLY -> iterDate.plusMonths(1)
                        Frequency.YEARLY -> iterDate.plusYears(1)
                    }

                }

                return occurences.toList()
            } catch (_: Exception){
                return listOf(event)
            }
        }

        fun eventInPeriod(event: VEvent, period: Period): Boolean {
                val (eventStart, eventEnd) = getTimeRange(event) ?: return false
                return eventStart.isBefore(period.end) && eventEnd.isAfter(period.start)
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

        fun getTimeRange(event: VEvent): Pair<LocalDateTime, LocalDateTime>? {
            return try {
                var startDate = event.dateStart?.value
                var endDate = event.dateEnd?.value
                if (startDate == null || endDate == null) return null
                val start = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault())
                val end = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault())
                start to end
            } catch (_: Exception) {
                null
            }
        }
    }

}
