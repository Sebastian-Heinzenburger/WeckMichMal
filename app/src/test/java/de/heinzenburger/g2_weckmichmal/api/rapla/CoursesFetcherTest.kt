package de.heinzenburger.g2_weckmichmal.api.rapla

import de.heinzenburger.g2_weckmichmal.specifications.BatchTuple
import de.heinzenburger.g2_weckmichmal.specifications.Period
import org.junit.Test

import org.junit.Assert.*
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime

class CoursesFetcherTest {
    private val url = URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN2")
    @Test
    fun `test hasValidURL`(){
        val validFetcher = CoursesFetcher(url)
        val start = LocalDateTime.of(2025, 4, 29, 13, 12);
        val end = start.plusDays(5)
        assertEquals("Fetcher should be valid", true, validFetcher.hasValidCourseURL())

        val invalidFetcher = CoursesFetcher(URL("foo"))
        assertEquals("Fetcher should be invalid", false, validFetcher.hasValidCourseURL())
    }

    @Test
    fun `test fetchCoursesBetween`(){
        val fetcher = CoursesFetcher(url)
        val start = LocalDateTime.of(2025, 4, 28, 0, 0, 0)
        val end = LocalDateTime.of(2025, 5, 3, 0, 0, 0)
        fetcher.fetchCoursesBetween(Period(start, end)).forEach { print(it) }



    }







    @Test
    fun `test fetchCoursesBetween successfully returns courses`() {
        val url = URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN2")
        val fetcher = CoursesFetcher(url)
        val start = LocalDateTime.of(2025, 4, 29, 13, 12);
        val end = start.plusDays(5)

        val courses = fetcher.fetchCoursesBetween(Period(start, end))

        assertNotNull("Courses should not be null", courses)
        assertTrue("Courses list should not be empty", courses.isNotEmpty())
    }

    @Test
    fun `test batchFetchCoursesBetween returns a list of courses`() {
        val url =
            URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN2")
        val fetcher = CoursesFetcher(url)
        val start = LocalDateTime.of(2025, 4, 29, 13, 12);
        val end = start.plusDays(5)

        val periods = listOf(
            BatchTuple(0L, Period(start, end)),
            BatchTuple(1L, Period(start, end))
        )

        val result = fetcher.batchFetchCoursesBetween(periods)

        assertNotNull("Result should not be null", result)
        assertEquals("Batch size should match the input size", periods.size, result.size)
        result.forEach { batch ->
            assertNotNull("Course list in each batch should not be null", batch.value)
            assertTrue("Course list in each batch should not be empty", batch.value.isNotEmpty(),)
        }
    }

    @Test
    fun `invalid RAPLA URL caught`() {
        val url = URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN99")
        assertFalse(CoursesFetcher(url).hasValidCourseURL())
    }

    @Test
    fun `valid RAPLA URL`() {
        val url = URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN2")
        assertTrue(CoursesFetcher(url).hasValidCourseURL())
    }

}
