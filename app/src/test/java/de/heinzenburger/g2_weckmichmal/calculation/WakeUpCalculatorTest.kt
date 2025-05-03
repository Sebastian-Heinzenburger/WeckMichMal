package de.heinzenburger.g2_weckmichmal.calculation

import de.heinzenburger.g2_weckmichmal.specifications.*
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class WakeUpCalculatorTest {

    private val routePlannerMock: I_RoutePlannerSpecification = mock()
    private val courseFetcherMock: CourseFetcherSpecification = mock()
    private val wakeUpCalculator = WakeUpCalculator(routePlannerMock, courseFetcherMock)

    @Test
    fun `test calculateNextEvent returns valid event`() {
        val configuration = ConfigurationEntity(
            uid = 123L,
            name = "Test Config",
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            fixedArrivalTime = null,
            fixedTravelBuffer = null,
            startBuffer = 15,
            endBuffer = 30,
            startStation = "StationA",
            endStation = "StationB",
            isActive = true
        )

        val eventDate = LocalDate.of(2025, 4, 28)
        val course = Course(
            name = "Course 1",
            lecturer = "Dr. Smith",
            room = "Room 101",
            startDate = LocalDateTime.of(2025, 4, 28, 10, 0),
            endDate = LocalDateTime.of(2025, 4, 28, 12, 0)
        )

        whenever(courseFetcherMock.fetchCoursesBetween(any())).thenReturn(listOf(course))

        val route = Route(
            startTime = LocalDateTime.of(2025, 4, 28, 9, 0),
            endTime = LocalDateTime.of(2025, 4, 28, 9, 30),
            startStation = "",
            endStation = "",
            sections = listOf()
        )
        whenever(routePlannerMock.planRoute(any(), any(), any())).thenReturn(listOf(route))

        // dirty hack because its not always Monday.
        wakeUpCalculator.clock = Clock.fixed(
            LocalDateTime.of(2025, 4, 28, 8, 0).atZone(java.time.ZoneId.systemDefault()).toInstant(),
            java.time.ZoneId.systemDefault().rules.getOffset(LocalDateTime.of(2025, 4, 28, 8, 0))
        )
        val result = wakeUpCalculator.calculateNextEvent(configuration)

        assertNotNull(result)
        assertEquals(123L, result.configID)
        assertEquals(LocalDateTime.of(2025, 4, 28, 8, 45).toLocalTime(), result.wakeUpTime)
        assertEquals(eventDate, result.date)
    }

    @Test
    fun `test batchCalculateNextEvent returns multiple events`() {
        val configuration1 = ConfigurationEntity(
            uid = 123L,
            name = "Config1",
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            fixedArrivalTime = null,
            fixedTravelBuffer = 20,
            startBuffer = 15,
            endBuffer = 30,
            startStation = "StationA",
            endStation = "StationB",
            isActive = true
        )
        val configuration2 = ConfigurationEntity(
            uid = 456L,
            name = "Config2",
            days = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            fixedArrivalTime = null,
            fixedTravelBuffer = 25,
            startBuffer = 20,
            endBuffer = 25,
            startStation = "StationC",
            endStation = "StationD",
            isActive = false
        )

        val course1 = Course(
            name = "Course 1",
            lecturer = "Dr. Smith",
            room = "Room 101",
            startDate = LocalDateTime.of(2025, 4, 28, 10, 0),
            endDate = LocalDateTime.of(2025, 4, 28, 12, 0)
        )

        val course2 = Course(
            name = "Course 2",
            lecturer = "Dr. Johnson",
            room = "Room 102",
            startDate = LocalDateTime.of(2025, 4, 29, 11, 0),
            endDate = LocalDateTime.of(2025, 4, 29, 13, 0)
        )

        whenever(courseFetcherMock.batchFetchCoursesBetween(any()))
            .thenReturn(
                listOf(
                    BatchTuple(0L, listOf(course1)),
                    BatchTuple(1L, listOf(course2))
                )
            )

        val route1 = Route(
            startTime = LocalDateTime.of(2025, 4, 28, 9, 30),
            endTime = LocalDateTime.of(2025, 4, 28, 10, 0),
            startStation = "",
            endStation = "",
            sections = listOf()
        )
        val route2 = Route(
            startTime = LocalDateTime.of(2025, 4, 29, 10, 30),
            endTime = LocalDateTime.of(2025, 4, 29, 11, 0),
            startStation = "",
            endStation = "",
            sections = listOf()
        )
        whenever(routePlannerMock.planRoute(any(), any(), any()))
            .thenReturn(listOf(route1), listOf(route2))

        val result = wakeUpCalculator.batchCalculateNextEvent(listOf(configuration1, configuration2))

        assertNotNull(result)
        assertEquals(2, result.size)
        result.forEach { event ->
            assertNotNull(event)
            assertNotNull(event.wakeUpTime)
        }

        assertEquals(LocalDateTime.of(2025, 4, 28, 8, 55).toLocalTime(), result[0].wakeUpTime)
        assertEquals(LocalDateTime.of(2025, 4, 29, 9, 50).toLocalTime(), result[1].wakeUpTime)
    }

}
