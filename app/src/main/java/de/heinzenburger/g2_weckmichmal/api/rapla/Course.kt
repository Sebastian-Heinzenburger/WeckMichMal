package de.heinzenburger.g2_weckmichmal.api.rapla

import java.time.LocalDateTime

data class Course(
    val name: String,
    val lecturer: String,
    val room: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)
