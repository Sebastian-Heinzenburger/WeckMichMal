package de.heinzenburger.g2_weckmichmal.api.courses

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
        val validFetcher = RaplaFetcher(url)
        assertEquals("Fetcher should be valid", true, validFetcher.throwIfInvalidCourseURL())

        val invalidFetcher = RaplaFetcher(URL("https://foo"))
        assertEquals("Fetcher should be invalid", false, invalidFetcher.throwIfInvalidCourseURL())
    }

    @Test
    fun `test fetchCoursesBetween`() {
        val fetcher = RaplaFetcher(url)
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
    fun `invalid RAPLA URL caught`() {
        val url =
            URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN99")
        val isValid = try {
            RaplaFetcher(url).throwIfInvalidCourseURL()
            true
        }
        catch (_: Exception){
            false
        }
        assertFalse(isValid)
    }

    @Test
    fun `valid RAPLA URL`() {
        val url =
            URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN2")
        val isValid = try {
            RaplaFetcher(url).throwIfInvalidCourseURL()
            true
        }
        catch (_: Exception){
            false
        }
        assertTrue(isValid)
    }
}
