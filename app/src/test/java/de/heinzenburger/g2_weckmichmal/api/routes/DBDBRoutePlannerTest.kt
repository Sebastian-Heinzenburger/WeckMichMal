package de.heinzenburger.g2_weckmichmal.api.routes

import de.heinzenburger.g2_weckmichmal.specifications.RoutePlannerException
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class DBDBRoutePlannerTest {
    @Test
    fun `deriveValidStationNames with Wiesloch-Wall`() {
        val routePlanner = DBRoutePlanner()
        val stationName = "Wiesloch-Wall"
        val result = routePlanner.deriveValidStationNames(stationName)
        assert(result.contains("Wiesloch-Walldorf")) { "Expected 'Wiesloch-Walldorf' in the result" }
    }

    @Test
    fun `deriveValidStationNames with Wiesloch-Wahldoof`() {
        val routePlanner = DBRoutePlanner()
        val stationName = "Wiesloch-Wahldoof"
        val result = routePlanner.deriveValidStationNames(stationName)
        assert(result.contains("Wiesloch-Walldorf")) { "Expected 'Wiesloch-Walldorf' in the result" }
    }

    @Test
    fun `deriveValidStationNames with Duale Hochschule Karlsruhe`() {
        val routePlanner = DBRoutePlanner()
        val stationName = "Duale Hochschule Karlsr"
        val result = routePlanner.deriveValidStationNames(stationName)
        assert(result.contains("Duale Hochschule, Karlsruhe")) { "Expected 'Duale Hochschule, Karlsruhe' in the result" }
    }

    @Test
    fun `deriveValidStationNames with Europaplatz`() {
        val routePlanner = DBRoutePlanner()
        val stationName = "Karlsruhe Europapl"
        val result = routePlanner.deriveValidStationNames(stationName)
        assert(result.contains("Karlsruhe Europaplatz")) { "Expected 'Karlsruhe Europaplatz' in the result" }
    }

    @Test
    fun `planRoute from Walldorf to Duale Hochschule`() {
        try {
            val routePlanner = DBRoutePlanner()
            val result = routePlanner.planRoute(
                "Wiesloch-Walldorf", "Duale Hochschule Karlsruhe", LocalDateTime.now()
            )
            assert(result.isNotEmpty()) { "Expected at least one route" }
            assert(result.all { it.startStation == "Wiesloch-Walldorf" }) { "Expected all routes to start from 'Wiesloch-Walldorf'" }
            assert(result.all { it.endStation == "Duale Hochschule, Karlsruhe" }) { "Expected all routes to end at 'Duale Hochschule Karlsruhe'" }
            assert(result.all {
                val duration = Duration.between(it.startTime, it.endTime)
                (duration.toMinutes() >= 20) && (duration.toMinutes() <= 60 * 3)
            }) { "Expected all routes to be between 20 minutes and 3 hours" }
        } catch (ignored: RoutePlannerException.NetworkException) {
            // Gitlab CI raises an IOException. No idea why
        }
    }

    @Test
    fun `temporary test pls modify later`() {
        try {
            val routePlanner = DBRoutePlanner()
            val result = routePlanner.planRoute(
                "Berlin Hbf", "Frankfurt(Main) Hbf", LocalDateTime.parse("2025-05-09T23:59:00")
            )
            result.forEach {
                route ->
                run {
                    println("Route: ${route.startStation} to ${route.endStation} ${route.startTime} to ${route.endTime}")
                }
            }
        } catch (ignored: RoutePlannerException.NetworkException) {
            // Gitlab CI cannot post data to the DB API
        }
    }


}