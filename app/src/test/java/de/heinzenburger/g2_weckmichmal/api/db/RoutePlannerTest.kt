package de.heinzenburger.g2_weckmichmal.api.db

import androidx.compose.ui.util.fastAny
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period

class RoutePlannerTest {
    @Test
    fun `deriveValidStationNames with Wiesloch-Wall`() {
        val routePlanner = RoutePlanner()
        val stationName = "Wiesloch-Wall"
        val result = routePlanner.deriveValidStationNames(stationName)
        assert(result.contains("Wiesloch-Walldorf")) { "Expected 'Wiesloch-Walldorf' in the result" }
    }

    @Test
    fun `deriveValidStationNames with Duale Hochschule Karlsruhe`() {
        val routePlanner = RoutePlanner()
        val stationName = "Duale Hochschule Karlsr"
        val result = routePlanner.deriveValidStationNames(stationName)
        assert(result.contains("Duale Hochschule, Karlsruhe")) { "Expected 'Duale Hochschule, Karlsruhe' in the result" }
    }

    @Test
    fun `deriveValidStationNames with Europaplatz`() {
        val routePlanner = RoutePlanner()
        val stationName = "Karlsruhe Europapl"
        val result = routePlanner.deriveValidStationNames(stationName)
        assert(result.contains("Karlsruhe Europaplatz")) { "Expected 'Karlsruhe Europaplatz' in the result" }
    }

    @Test
    fun `planRoute from Walldorf to Duale Hochschule`() {
        val routePlanner = RoutePlanner()
        val result = routePlanner.planRoute("Wiesloch-Walldorf", "Duale Hochschule Karlsruhe", LocalDateTime.now())
        assert(result.isNotEmpty()) { "Expected at least one route" }
        assert(result.all { it.startStation == "Wiesloch-Walldorf" }) { "Expected all routes to start from 'Wiesloch-Walldorf'" }
        assert(result.all { it.endStation == "Duale Hochschule, Karlsruhe" }) { "Expected all routes to end at 'Duale Hochschule Karlsruhe'" }
        assert(result.all {
            val duration = Duration.between(it.startTime, it.endTime)
            (duration.toHours() >= 0.1) && (duration.toHours() <= 3)
        }) { "Expected all routes to be between 6 minutes and 3 hours" }
    }
}