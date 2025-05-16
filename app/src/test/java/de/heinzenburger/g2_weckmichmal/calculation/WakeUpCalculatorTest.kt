package de.heinzenburger.g2_weckmichmal.calculation

import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.CourseFetcherSpecification
import de.heinzenburger.g2_weckmichmal.specifications.RoutePlannerSpecification
import de.heinzenburger.g2_weckmichmal.specifications.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class WakeUpCalculatorTest {

    private val referenceDate = LocalDate.now()
    private val eventDate = referenceDate.plusDays(1)
    private val eventDay = eventDate.dayOfWeek

    private val routePlannerMock: RoutePlannerSpecification = mock()
    private val courseFetcherMock: CourseFetcherSpecification = mock()
    private val wakeUpCalculator = WakeUpCalculator(routePlannerMock, courseFetcherMock)

    private val routesMock = listOf(
        Route(
            startTime = LocalDateTime.of(2025, 4, 28, 8, 45),
            endTime = LocalDateTime.of(2025, 4, 28, 9, 30),
            startStation = "",
            endStation = "",
            sections = listOf()
        ),
        Route(
            startTime = LocalDateTime.of(2025, 4, 28, 9, 0),
            endTime = LocalDateTime.of(2025, 4, 28, 9, 30),
            startStation = "",
            endStation = "",
            sections = listOf()
        )
    )


    private val courseMocks = listOf(
        Course(
            name = "CourseA",
            lecturer = "ProfA",
            room = "Room 101",
            startDate = eventDate.atTime(10, 0),
            endDate = eventDate.atTime(12, 0),
        ),
        Course(
            name = "CourseB",
            lecturer = "ProfB",
            room = "Room 102",
            startDate = eventDate.atTime(12, 0),
            endDate = eventDate.atTime(14, 0),
        )
    )

    @Test
    fun `test calculateNextEvent with CourseReference`() {
        whenever(courseFetcherMock.fetchCoursesBetween(any())).thenReturn(courseMocks)
        whenever(routePlannerMock.planRoute(any(), any(), any())).thenReturn(routesMock)

        val config = Configuration(
            uid = 1L,
            name = "Test Config",
            days = setOf(eventDay, DayOfWeek.WEDNESDAY),
            fixedArrivalTime = null,
            fixedTravelBuffer = null,
            startBuffer = 10,
            endBuffer = 30,
            startStation = "StationA",
            endStation = "StationB",
            isActive = true,
            enforceStartBuffer = true
        )

        val result = wakeUpCalculator.calculateNextEvent(config)
        assertDoesNotThrow { wakeUpCalculator.calculateNextEvent(config) }
        assertNotNull("Expected to receive result", result)
        assertEquals("Expected to have same configID", 1L, result.configID)
        assertEquals("Wrong wakeUpTime", eventDate.atTime(8,50).toLocalTime(), result.wakeUpTime)
    }

    @Test
    fun `test calculateNextEvent with fixed time`() {
        whenever(courseFetcherMock.fetchCoursesBetween(any())).thenReturn(courseMocks)
        whenever(routePlannerMock.planRoute(any(), any(), any())).thenReturn(routesMock)

        val config = Configuration(
            uid = 1L,
            name = "Test Config",
            days = setOf(eventDay, DayOfWeek.WEDNESDAY),
            fixedArrivalTime = LocalTime.of(10, 0, 0),
            fixedTravelBuffer = 30,
            startBuffer = 10,
            endBuffer = 30,
            startStation = null,
            endStation = null,
            isActive = true,
            enforceStartBuffer = true
        )

        val result = wakeUpCalculator.calculateNextEvent(config)
        assertDoesNotThrow { wakeUpCalculator.calculateNextEvent(config) }
        assertNotNull("Expected to receive result", result)
        assertEquals("Expected to have same configID", 1L, result.configID)
        assertEquals("Wrong wakeUpTime", eventDate.atTime(8,50).toLocalTime(), result.wakeUpTime)
    }

    @Test
    fun `test batchCalculateNextEvent with fixed time`() {
        whenever(courseFetcherMock.fetchCoursesBetween(any())).thenReturn(courseMocks)
        whenever(routePlannerMock.planRoute(any(), any(), any())).thenReturn(routesMock)


        val configs = listOf(
            Configuration(
                uid = 1L,
                name = "Test Config",
                days = setOf(eventDay, DayOfWeek.WEDNESDAY),
                fixedArrivalTime = LocalTime.of(10, 0, 0),
                fixedTravelBuffer = 30,
                startBuffer = 10,
                endBuffer = 30,
                startStation = null,
                endStation = null,
                isActive = true,
                enforceStartBuffer = true
            ),

        )


        val config = Configuration(
            uid = 1L,
            name = "Test Config",
            days = setOf(eventDay, DayOfWeek.WEDNESDAY),
            fixedArrivalTime = LocalTime.of(10, 0, 0),
            fixedTravelBuffer = 30,
            startBuffer = 10,
            endBuffer = 30,
            startStation = null,
            endStation = null,
            isActive = true,
            enforceStartBuffer = true
        )

        val result = wakeUpCalculator.calculateNextEvent(config)
        assertDoesNotThrow { wakeUpCalculator.calculateNextEvent(config) }
        assertNotNull("Expected to receive result", result)
        assertEquals("Expected to have same configID", 1L, result.configID)
        assertEquals("Wrong wakeUpTime", eventDate.atTime(8,50).toLocalTime(), result.wakeUpTime)
    }

}
