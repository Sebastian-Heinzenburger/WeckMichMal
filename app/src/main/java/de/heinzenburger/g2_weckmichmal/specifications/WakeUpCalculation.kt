package de.heinzenburger.g2_weckmichmal.specifications

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Interface defining the behavior for the wake-up time calculation.
 */
interface I_WakeUpCalculationSpecification {

    /**
     * Computes an [EventEntity] for the next [DayOfWeek] based on the provided [ConfigurationEntity].
     *
     * @param configuration The [ConfigurationEntity] that contains the settings for the alarm.
     * This includes various time buffers, stations, and other parameters that determine the event's calculation.
     * @return The calculated next [EventEntity] .
     */
    @Throws(Exception::class)
    fun calculateNextEvent(configuration: ConfigurationEntity): EventEntity

    /**
     * Computes an [EventEntity] for a specific [DayOfWeek] based on the provided [ConfigurationEntity].
     *
     * @param configuration The [ConfigurationEntity] that contains the settings for the alarm,
     * including time buffers, stations, and other parameters influencing the event's calculation.
     * @param day The [DayOfWeek] for which the event should be computed.
     * @return The calculated [EventEntity] for the specified day.
     * @throws Exception if the calculation fails due to invalid configuration or other issues.
     */
    @Throws(Exception::class)
    fun calculateEventForDay(configuration: ConfigurationEntity, day: DayOfWeek): EventEntity

    /**
     * Computes an [EventEntity] for a specific calendar date based on the provided [ConfigurationEntity].
     *
     * @param configuration The [ConfigurationEntity] that contains the settings for the alarm,
     * including time buffers, stations, and other parameters influencing the event's calculation.
     * @param date The [LocalDate] for which the event should be computed.
     * @return The calculated [EventEntity] for the specified date.
     * @throws Exception if the calculation fails due to invalid configuration or other issues.
     */
    @Throws(Exception::class)
    fun calculateEventForDate(configuration: ConfigurationEntity, date: LocalDate): EventEntity
}