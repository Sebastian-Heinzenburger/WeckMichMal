package de.heinzenburger.g2_weckmichmal.specifications

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.heinzenburger.g2_weckmichmal.MainActivity
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.Event
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Interface defining the behavior for the persistence layer.
 */
interface I_PersistenceSpecification {
    /**
     * Saves or updates the alarm configuration in the persistence layer.
     *
     * @param config The [ConfigurationEntity] object that contains the data to be saved or updated.
     */
    fun saveOrUpdate(config: ConfigurationEntity): Boolean

    /**
     * Saves or updates an event in the persistence layer.
     *
     * @param event The [EventEntity] object that contains the data to be saved or updated.
     */
    fun saveOrUpdate(event: EventEntity): Boolean

    /**
     * Returns the alarm configuration from the persistence layer.
     *
     * @return A ConfigurationEntity object representing the saved alarm configuration.
     */
    fun getAlarmConfiguration(id: Long): ConfigurationEntity?

    /**
     * Returns all alarm configurations listed in the persistence layer.
     *
     * @return A list of [ConfigurationEntity] objects representing all the saved alarm configurations.
     */
    fun getAllAlarmConfigurations(): List<ConfigurationEntity>?

    /**
     * Returns all events listed in the persistence layer.
     *
     * @return A list of [EventEntity] objects representing all the saved events.
     */
    fun getAllEvents(): List<EventEntity>?

    /**
     * Removes the alarm configuration from the persistence layer.
     *
     * @param id The id of the [ConfigurationEntity] object to be removed from the persistence layer.
     */
    fun removeAlarmConfiguration(id: Long): Boolean

    /**
     * Removes an event from the persistence layer.
     *
     * @param configID The id of the [ConfigurationEntity] object corresponding to the Event.
     * @param days The days of the [ConfigurationEntity] object corresponding to the Event.
     */
    fun removeEvent(configID: Long, days: Set<DayOfWeek>): Boolean

    /**
     * Removes an event from the persistence layer.
     *
     * @param configID The id of the [ConfigurationEntity] object corresponding to the Event.
     */
    fun removeEvent(configID: Long): Boolean

    /**
     * Overrides the application settings in the persistence layer with the given parameter.
     */
    fun saveOrUpdateApplicationSettings(settings: SettingsEntity) : Boolean

    /**
     * Returns the application settings entity from the persistence layer.
     *
     * @return A [SettingsEntity] object.
     */
    fun getApplicationSettings() : SettingsEntity?

    /**
     * @return A boolean that is true, when the application is freshly installed and opened for the first time
     */
    fun isApplicationOpenedFirstTime() : Boolean?
}

/**
 * Represents an alarm configuration, defining the static configuration for an alarm across up to 7 days of the week.
 *
 * @property uid A unique identifier to uniquely identify this configuration.
 * @property name The name specified by the user for the configuration.
 * @property days A set of [DayOfWeek] elements representing the days selected for the configuration.
 * @property fixedArrivalTime The fixed arrival time, if specified by the user.
 * @property fixedTravelBuffer The fixed time buffer (in minutes) for travel when not using this app's route planning, if specified by the user.
 * @property startBuffer The time buffer (in minutes) before the travel begins.
 * @property endBuffer The time buffer (in minutes) after travel and before the journey ends.
 * @property startStation The DB Navigator conform name of the start station for the journey.
 * @property endStation The DB Navigator conform name of the end station for the journey.
 */
@Entity(tableName = "configurationentity")
data class ConfigurationEntity(
    /** A unique identifier to uniquely identify this configuration. */
    @PrimaryKey val uid: Long = System.currentTimeMillis(),
    /** The name specified by the user for the configuration. */
    @ColumnInfo(name = "name") var name : String,
    /** A set of [DayOfWeek] elements representing the days selected for the configuration. */
    @ColumnInfo(name = "days") var days: Set<DayOfWeek>,
    /** The fixed arrival time, if specified by the user. */
    @ColumnInfo(name = "fixedArrivalTime") var fixedArrivalTime: LocalTime?,
    /** The fixed time buffer (in minutes) for travel when not using this app's route planning, if specified by the user. */
    @ColumnInfo(name = "fixedTravelBuffer") var fixedTravelBuffer: Int?,
    /** The time buffer (in minutes) before the travel begins. */
    @ColumnInfo(name = "startBuffer") var startBuffer: Int,
    /** The time buffer (in minutes) after travel and before the arrival time. */
    @ColumnInfo(name = "endBuffer") var endBuffer: Int,
    /** The DB Navigator conform name of the start station for the journey. */
    @ColumnInfo(name = "startStation") var startStation: String?,
    /** The DB Navigator conform name of the end station for the journey. */
    @ColumnInfo(name = "endStation") var endStation: String?
){
    fun log(){
        MainActivity.log.info("Logging Alarm configuration with id $uid:\n$name\n$days\n$fixedArrivalTime\n$fixedTravelBuffer\n$startBuffer\n$endBuffer\n$startStation\n$endStation")
    }
}

/**
 * Represents a singular event for the alarm configuration at a specific time.
 * @property configID The unique identifier of the corresponding alarm configuration.
 * @property wakeUpTime The time to ring the alarm, calculated based on the alarm configuration.
 * @property days A set of [DayOfWeek] elements representing the days selected for the configuration.
 * @property date The specific date this event references.
 */
@Entity(tableName = "evententity", primaryKeys = ["configID","days"])
data class EventEntity(
    /** The unique identifier of the corresponding alarm configuration. */
    @ColumnInfo(name = "configID") var configID: Long,
    /** The time to ring the alarm, calculated based on the alarm configuration. */
    @ColumnInfo(name = "wakeuptime") var wakeUpTime: LocalTime,
    /** A set of [DayOfWeek] elements representing the days selected for the configuration. */
    @ColumnInfo(name = "days") var days: Set<DayOfWeek>,
    /** The specific date this event references. */
    @ColumnInfo(name = "date") var date: LocalDate
){
    fun log(){
        MainActivity.log.info("Logging Event with id $configID:\n$wakeUpTime\n$days\n$date")
    }
}

/**
 * @property raplaURL The WebLink leading to the RAPLA schedule.
 */
data class SettingsEntity(
    /** The WebLink leading to the RAPLA schedule. */
    val raplaURL: String
){
    fun log(){
        MainActivity.log.info("Logging SettingsEntity:\n$raplaURL")
    }
}

