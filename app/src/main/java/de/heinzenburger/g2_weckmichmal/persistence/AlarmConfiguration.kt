package de.heinzenburger.g2_weckmichmal.persistence

import java.time.LocalDateTime
import java.time.LocalTime
import java.util.BitSet

data class AlarmConfiguration(
    val id: String,
    val name: String,
    val day: BitSet,
    val fixedArrivalTime: LocalTime,
    val fixedTravelBuffer: Int,
    val startBuffer: Int,
    val endBuffer: Int,
    val startStation: String,
    val endStation: String
)
