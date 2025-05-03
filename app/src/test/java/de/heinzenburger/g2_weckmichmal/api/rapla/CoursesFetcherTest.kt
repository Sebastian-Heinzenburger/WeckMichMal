package de.heinzenburger.g2_weckmichmal.api.rapla

import de.heinzenburger.g2_weckmichmal.specifications.BatchTuple
import de.heinzenburger.g2_weckmichmal.specifications.Period
import org.junit.Test

import org.junit.Assert.*
import java.net.URL
import java.time.LocalDateTime

class CoursesFetcherTest {

    private val url =
        URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN2")

    @Test
    fun `test hasValidURL`() {
        val validFetcher = CoursesFetcher(url)
        assertEquals("Fetcher should be valid", true, validFetcher.hasValidCourseURL())

        val invalidFetcher = CoursesFetcher(URL("https://foo"))
        assertEquals("Fetcher should be invalid", false, invalidFetcher.hasValidCourseURL())
    }

    @Test
    fun `test fetchCoursesBetween`() {
        val fetcher = CoursesFetcher(url)
        val start = LocalDateTime.of(2025, 4, 28, 0, 0, 0)
        val end = start.plusDays(4)
        val expCourseNames = listOf(
            "Numerik",
            "Compilerbau",
            "Systemnahe Programmierung",
            "Betriebssysteme",
            "Software Engineering"
        )

        val courses = fetcher.fetchCoursesBetween(Period(start, end))
        assertEquals(5, courses.size)
        courses.forEach { assert(expCourseNames.contains(it.name)) }
    }

    @Test
    fun `test batchFetchCoursesBetween`() {
        val fetcher = CoursesFetcher(url)
        val startA = LocalDateTime.of(2025, 4, 28, 0, 0, 0)
        val startB = LocalDateTime.of(2025, 4, 30, 0, 0, 0)
        val expCourseNamesA = listOf(
            "Numerik",
            "Compilerbau",
            "Systemnahe Programmierung",
            "Betriebssysteme",
        )
        val expCourseNamesB = listOf(
            "Numerik",
            "Compilerbau",
            "Systemnahe Programmierung",
            "Software Engineering"
        )

        val periods = listOf(
            BatchTuple(0L, Period(startA, startA.plusDays(2))),
            BatchTuple(1L, Period(startB, startB.plusDays(2)))
        )

        val batchCourses = fetcher.batchFetchCoursesBetween(periods)
        val coursesA = batchCourses.find { it.id == 0L }?.value ?: return assert(false)
        assertEquals(4, coursesA.size)
        coursesA.forEach { assert(expCourseNamesA.contains(it.name)) }
        val coursesB = batchCourses.find { it.id == 1L }?.value ?: return assert(false)
        assertEquals(1, coursesB.size)
        coursesB.forEach { assert(expCourseNamesB.contains(it.name)) }
    }

    @Test
    fun `invalid RAPLA URL caught`() {
        val url =
            URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN99")
        assertFalse(CoursesFetcher(url).hasValidCourseURL())
    }

    @Test
    fun `valid RAPLA URL`() {
        val url =
            URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN2")
        assertTrue(CoursesFetcher(url).hasValidCourseURL())
    }

}
