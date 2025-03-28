package de.heinzenburger.g2_weckmichmal.api.db

import java.time.LocalDateTime

interface RoutePlanner {
    fun getPlannedRoute(startStation: String, endStation: String, timeOfArrival: LocalDateTime): List<PotentialRoute>
}