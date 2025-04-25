package de.heinzenburger.g2_weckmichmal.api.rapla

import de.heinzenburger.g2_weckmichmal.specifications.BatchTuple
import de.heinzenburger.g2_weckmichmal.specifications.Period
import org.junit.Test

import org.junit.Assert.*
import java.net.URL
import java.time.LocalDateTime

class CoursesFetcherTest {

    @Test
    fun `test fetchCoursesBetween successfully returns courses`() {
        // Given
        val url = URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN2")
        val fetcher = CoursesFetcher(url)
        val start = LocalDateTime.now()
        val end = start.plusDays(5)

        // When
        val courses = fetcher.fetchCoursesBetween(Period(start, end))

        // Then
        assertNotNull("Courses should not be null", courses)
        assertTrue("Courses list should not be empty", courses.isNotEmpty())
    }

    @Test
    fun `test batchFetchCoursesBetween returns a list of courses`() {
        // Given
        val url = URL("https://rapla.dhbw-karlsruhe.de/rapla?page=ical&user=ritterbusch&file=TINF23BN2")
        val fetcher = CoursesFetcher(url)
        val start = LocalDateTime.now()
        val end = start.plusDays(5)

        val periods = listOf(
            BatchTuple(0L, Period(start, end)),
            BatchTuple(1L, Period(start, end))
        )

        // When
        val result = fetcher.batchFetchCoursesBetween(periods)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Batch size should match the input size", periods.size, result.size)
        result.forEach { batch ->
            assertNotNull("Course list in each batch should not be null", batch.value)
            assertTrue("Course list in each batch should not be empty", batch.value.isNotEmpty(), )
        }
    }
}
