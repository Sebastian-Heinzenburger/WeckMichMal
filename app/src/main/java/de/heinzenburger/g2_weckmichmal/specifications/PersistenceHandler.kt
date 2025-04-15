package de.heinzenburger.g2_weckmichmal.specifications

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Interface defining the behavior for the persistence layer.
 */
interface I_PersistenceSpecification {

    /**
     * Returns all alarm configurations listed in the persistence layer.
     *
     * @return A list of [AlarmConfiguration] objects representing all the saved alarm configurations.
     */
    fun getAllAlarmConfigurations(): List<AlarmConfiguration>

    /**
     * Saves or updates the alarm configuration in the persistence layer.
     *
     * @param configuration The [AlarmConfiguration] object that contains the data to be saved or updated.
     */
    fun saveOrUpdate(configuration: AlarmConfiguration)

    /**
     * Removes the alarm configuration from the persistence layer.
     *
     * @param configuration The [AlarmConfiguration] object to be removed from the persistence layer.
     */
    fun removeAlarmConfiguration(configuration: AlarmConfiguration)
}

/**
 * Represents an alarm configuration, defining the static configuration for an alarm across up to 7 days of the week.
 *
 * @property id A unique identifier to uniquely identify this configuration.
 * @property name The name specified by the user for the configuration.
 * @property selectedDays A set of [Day] elements representing the days selected for the configuration.
 * @property fixedArrivalTime The fixed arrival time, if specified by the user.
 * @property fixedTravelBuffer The fixed time buffer (in minutes) for travel when not using this app's route planning, if specified by the user.
 * @property startBuffer The time buffer (in minutes) before the travel begins.
 * @property endBuffer The time buffer (in minutes) after travel and before the journey ends.
 * @property startStation The DB Navigator conform name of the start station for the journey.
 * @property endStation The DB Navigator conform name of the end station for the journey.
 * @property nextEvent The next [Event] derived from this configuration.
 */
data class AlarmConfiguration (
    /** A unique identifier to uniquely identify this configuration. */
    val id: String,

    /** The name specified by the user for the configuration. */
    val name: String,

    /** A set of [DayOfWeek] elements representing the days selected for the configuration. */
    val selectedDays: Set<DayOfWeek>,

    /** The fixed arrival time, if specified by the user. */
    val fixedArrivalTime: LocalTime?,

    /** The fixed time buffer (in minutes) for travel when not using this app's route planning, if specified by the user. */
    val fixedTravelBuffer: Int?,

    /** The time buffer (in minutes) before the travel begins. */
    val startBuffer: Int,

    /** The time buffer (in minutes) after travel and before the arrival time. */
    val endBuffer: Int,

    /** The DB Navigator conform name of the start station for the journey. */
    val startStation: String?,

    /** The DB Navigator conform name of the end station for the journey. */
    val endStation: String?,

    /** The next [Event] derived from this configuration. */
    @Deprecated("Hallo hier ist Flo. Warum nextEvent? Das kommt doch nicht in die Datenbank")
    val nextEvent: Event?
)


/**
 * Represents a singular event for the alarm configuration at a specific time.
 *
 * @property wakeUpTime The time to ring the alarm, calculated based on the alarm configuration.
 * @property representedDay The [DayOfWeek] element of the alarm configuration represented by this event.
 * @property date The specific date this event references.
 * @property routes The routes for this event.
 * @property courses The courses scheduled for this day.
 */
data class Event (
    /** The time to ring the alarm, calculated based on the alarm configuration. */
    val wakeUpTime: LocalTime,

    /** The specific date this event references. */
    val date: LocalDate,

    /** The routes for this event. */
    @Deprecated("Hallo hier ist Flo. Warum stehen hier sachen die nicht in die Datenbank kommen...")
    val routes: List<Route>?,

    /** The courses scheduled for this day. */
    val courses: List<Course>?
)

