package de.heinzenburger.g2_weckmichmal.api.courses

import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.CourseFetcherSpecification
import de.heinzenburger.g2_weckmichmal.specifications.Period
import biweekly.Biweekly
import biweekly.component.VEvent
import biweekly.util.Frequency
import biweekly.util.ICalDate
import de.heinzenburger.g2_weckmichmal.specifications.CourseFetcherException
import de.heinzenburger.g2_weckmichmal.specifications.NotEnoughParameterException
import java.io.InputStream
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date


val urlTemplate: (String, String) -> String = { director, course ->
    "https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=${director.lowercase()}&file=${course.lowercase()}"
}

@Throws(CourseFetcherException::class, NotEnoughParameterException::class)
fun deriveValidCourseURL(vararg params: String): URL {
    if(params.size < 2) {
        throw NotEnoughParameterException()
    }

    val url = URL(urlTemplate(params[0], params[1]));
    val testFetcher = RaplaFetcher(url);
    testFetcher.throwIfInvalidCourseURL()
    return url
}

class RaplaFetcher(
    private val raplaUrl: URL,
    private val validCourseCategories: Set<String> = setOf("Prüfung", "Lehrveranstaltung"),
    private val excludedCourseNames: Set<String> = emptySet()
) : CourseFetcherSpecification {

    @Throws(CourseFetcherException::class)
    override fun fetchCoursesBetween(period: Period): List<Course> {
        val icalStream: InputStream = fetchInputStream(raplaUrl)

        return try {
            Biweekly.parse(icalStream).first()
                .events
                .filter { eventInCategory(it, validCourseCategories) }
                .filter { !eventNameExcluded(it, excludedCourseNames) }
                .map { expandOccurrences(it) }.flatten()
                .filter { eventInPeriod(it, period) }
                .mapNotNull { eventToCourse(it) }
        } catch (e: Exception) {
            throw CourseFetcherException.DataFormatError(e)
        }
    }

    @Throws(CourseFetcherException::class)
    override fun throwIfInvalidCourseURL() {
        fetchCoursesBetween(Period(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
    }

    @Throws(CourseFetcherException::class)
    override fun getAllCourseNames(): List<String> {
        val icalStream: InputStream = fetchInputStream(raplaUrl)

        return try {
            return Biweekly.parse(icalStream).first()
                .events
                .filter { eventInCategory(it, validCourseCategories) }
                .map { expandOccurrences(it) }.flatten()
                .filter { eventIsNowOrAfter(it, LocalDate.now()) }
                .map { it.summary.value.trim() }
                .distinct()
        } catch (e: Exception) {
            throw CourseFetcherException.DataFormatError(e)
        }
    }

    private companion object {

        @Throws(CourseFetcherException.ConnectionError::class)
        fun fetchInputStream(url: URL): InputStream {
            return try {
                url.openConnection().inputStream
            } catch (e: Exception) {
                throw CourseFetcherException.ConnectionError(e)
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

        fun eventNameExcluded(event: VEvent, excludedCourseNames: Set<String>): Boolean {
            return try {
                val eventName = event.summary.value.trim();
                return excludedCourseNames.contains(eventName)
            } catch (_: Exception) {
                false
            }
        }

        fun eventIsNowOrAfter(event: VEvent, now: LocalDate): Boolean {
            return try {
                val (eventStart, _) = getTimeRange(event) ?: return false
                return eventStart.toLocalDate().isAfter(now)
            } catch (_: Exception){
                return false
            }
        }

        fun expandOccurrences(event: VEvent): List<VEvent> {
            val rrule = event.recurrenceRule ?: return listOf(event)

            val eventStartDate = event.dateStart?.value?.toInstant()?.atZone(ZoneId.systemDefault())
                ?.toLocalDateTime()
                ?: return emptyList()
            val eventEndDate =
                event.dateEnd?.value?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                    ?: return emptyList()
            val eventDuration = ChronoUnit.SECONDS.between(eventStartDate, eventEndDate)

            // Rule modifiers
            val excludedDates = event.exceptionDates
                .flatMap { it.values }
                .mapNotNull { it.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() }.toSet()
            val frequency = rrule.value.frequency ?: return listOf(event)

            var occurrences = mutableListOf<VEvent>()
            val untilDate =
                rrule.value.until?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            var count = rrule.value.count
            var iterDate = eventStartDate

            if (count == null && untilDate == null) return listOf(event)

            while (true) {
                if (untilDate != null && iterDate.isAfter(untilDate)) break
                if (count != null && count-- < 0) break
                // Check excluded
                if (!excludedDates.contains(iterDate.toLocalDate())) {
                    val occurrence = event.copy()
                    occurrence.dateStart.value =
                        ICalDate(Date.from(iterDate.atZone(ZoneId.systemDefault()).toInstant()))
                    val endDate = iterDate.plusSeconds(eventDuration)
                    occurrence.dateEnd.value =
                        ICalDate(Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant()))

                    occurrences.add(occurrence)
                }

                // Update logic by frequency
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
            return occurrences
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
