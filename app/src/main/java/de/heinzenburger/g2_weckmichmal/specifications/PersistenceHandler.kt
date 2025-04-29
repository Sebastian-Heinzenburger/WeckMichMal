package de.heinzenburger.g2_weckmichmal.specifications

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import de.heinzenburger.g2_weckmichmal.MainActivity
import de.heinzenburger.g2_weckmichmal.persistence.DataConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Interface defining the behavior for the persistence layer.
 */
interface I_AlarmConfiguration{
    /**
     * Saves or updates the alarm configuration in the persistence layer.
     *
     * @param config The [ConfigurationEntity] object that contains the data to be saved or updated.
     */
    fun saveOrUpdate(config: ConfigurationEntity): Boolean
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
     * Removes the alarm configuration from the persistence layer.
     *
     * @param id The id of the [ConfigurationEntity] object to be removed from the persistence layer.
     */
    fun removeAlarmConfiguration(id: Long): Boolean
    /**
     * Returns a [ConfigurationEntity] with corresponding [EventEntity]
     *
     * @param id The id of the [ConfigurationEntity] object.
     */
    fun getConfigurationAndEvent(id: Long): ConfigurationAndEventEntity?
    /**
     * Returns a list of all [ConfigurationEntity] with corresponding [EventEntity]
     */
    fun getAllConfigurationAndEvent(): List<ConfigurationAndEventEntity>?

}
interface I_Event{
    /**
     * Saves or updates an event in the persistence layer.
     *
     * @param event The [EventEntity] object that contains the data to be saved or updated.
     */
    fun saveOrUpdate(event: EventEntity): Boolean
    /**
     * Returns all events listed in the persistence layer.
     *
     * @return A list of [EventEntity] objects representing all the saved events.
     */
    fun getAllEvents(): List<EventEntity>?
    /**
     * Returns the alarm configuration from the persistence layer.
     *
     * @return A ConfigurationEntity object representing the saved alarm configuration.
     */
    fun getEvent(id: Long, days : Set<DayOfWeek>): EventEntity?
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
}
interface I_ApplicationSettings {
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
 * @property isActive Whether the User enabled the Alarm
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
    @ColumnInfo(name = "endStation") var endStation: String?,
    /** The DB Navigator conform name of the end station for the journey. */
    @ColumnInfo(name = "isActive") var isActive: Boolean,
){
    fun log(){
        MainActivity.log.info("Logging Alarm configuration with id $uid:\n$name\n$days\n$fixedArrivalTime\n$fixedTravelBuffer\n$startBuffer\n$endBuffer\n$startStation\n$endStation\n$isActive")
    }
    companion object{
        val emptyConfiguration = ConfigurationEntity(
            uid = 0,
            name = "",
            days = setOf(DayOfWeek.MONDAY),
            fixedArrivalTime = null,
            fixedTravelBuffer = null,
            startBuffer = 0,
            endBuffer = 0,
            startStation = "",
            endStation = "",
            isActive = true
        )
    }
}

/**
 * Represents a singular event for the alarm configuration at a specific time.
 * @property configID The unique identifier of the corresponding alarm configuration.
 * @property wakeUpTime The time to ring the alarm, calculated based on the alarm configuration.
 * @property days A set of [DayOfWeek] elements representing the days selected for the configuration.
 * @property date The specific date this event references.
 * @property courses The courses following this day.
 * @property routes The specific date this event references.
 */
@Entity(tableName = "evententity")
data class EventEntity(
    /** The unique identifier of the corresponding alarm configuration. */
    @PrimaryKey val configID: Long,
    /** The time to ring the alarm, calculated based on the alarm configuration. */
    @ColumnInfo(name = "wakeuptime") var wakeUpTime: LocalTime,
    /** A set of [DayOfWeek] elements representing the days selected for the configuration. */
    @ColumnInfo(name = "days") var days: Set<DayOfWeek>,
    /** The specific date this event references. */
    @ColumnInfo(name = "date") var date: LocalDate,
    /** The courses following this day. */
    @ColumnInfo(name = "courses") var courses: List<Course>?,
    /** The specific date this event references. */
    @ColumnInfo(name = "routes") var routes: List<Route>?
){
    fun log(){
        MainActivity.log.info("Logging Event with id $configID:\n$wakeUpTime\n$days\n$date\n${DataConverter().fromListOfCourses(courses)}\n${
            DataConverter().fromListOfRoutes(routes)}")
    }
    companion object{
        val emptyEvent = EventEntity(
            configID = 0,
            wakeUpTime = LocalTime.NOON,
            days = emptySet(),
            date = LocalDate.of(2025,1,1),
            courses = listOf(Course(
                name = "",
                lecturer = "",
                room = "",
                startDate = LocalDateTime.of(2025,1,1,12,0),
                endDate = LocalDateTime.of(2025,1,1,12,0)
            )),
            routes = listOf(Route(
                startStation = "",
                endStation = "",
                startTime = LocalDateTime.of(2025,1,1,12,0),
                endTime = LocalDateTime.of(2025,1,1,12,0),
                sections = listOf(RouteSection(
                    vehicleName = "",
                    startTime = LocalDateTime.of(2025,1,1,12,0),
                    startStation = "",
                    endTime = LocalDateTime.of(2025,1,1,12,0),
                    endStation = ""
                ))
            ))
        )
    }
}
data class ConfigurationAndEventEntity(
    @Embedded val configurationEntity: ConfigurationEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "configID"
    )
    val eventEntity: EventEntity?
)

/**
 * @property raplaURL The WebLink leading to the RAPLA schedule.
 */
data class SettingsEntity(
    /** The WebLink leading to the RAPLA schedule. */
    var raplaURL: String
){
    fun log(){
        MainActivity.log.info("Logging SettingsEntity:\n$raplaURL")
    }
}

