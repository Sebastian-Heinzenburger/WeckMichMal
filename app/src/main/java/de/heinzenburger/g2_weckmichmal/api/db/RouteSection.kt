package de.heinzenburger.g2_weckmichmal.api.db

import java.time.LocalDateTime

data class RouteSection (
    val trainName: String,
    val startTime: LocalDateTime,
    val startStation: String,
    val endTime: LocalDateTime,
    val endStation: String,
)
