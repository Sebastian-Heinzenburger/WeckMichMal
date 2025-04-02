package de.heinzenburger.g2_weckmichmal.persistence

import java.time.LocalDate
import java.time.LocalTime
import java.util.BitSet

data class Event(
    val configID: String,
    val wakeUpTime: LocalTime,
    val day: BitSet,
    val date: LocalDate
)
