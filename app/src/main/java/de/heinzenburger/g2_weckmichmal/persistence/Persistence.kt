package de.heinzenburger.g2_weckmichmal.persistence

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


interface Persistence {
    fun saveOrUpdate(config: AlarmConfiguration.ConfigurationEntity): Boolean
    fun saveOrUpdate(event: Event.EventEntity): Boolean
    fun getAlarmConfiguration(id: Long): AlarmConfiguration.ConfigurationEntity?
    fun getAllAlarmConfigurations(): List<AlarmConfiguration.ConfigurationEntity>?
    fun getAllEvents(): List<Event.EventEntity>?
    fun removeAlarmConfiguration(id: Long): Boolean
    fun removeEvent(configID: Long, days: String): Boolean
    fun removeEvent(configID: Long): Boolean

}
abstract class PersistenceClass : Persistence{
    override fun saveOrUpdate(config: AlarmConfiguration.ConfigurationEntity): Boolean {
        return false
    }
    override fun saveOrUpdate(event: Event.EventEntity): Boolean{
        return false
    }
    override fun getAlarmConfiguration(id: Long): AlarmConfiguration.ConfigurationEntity? {
        return null
    }
    override fun getAllAlarmConfigurations(): List<AlarmConfiguration.ConfigurationEntity>?{
        return null
    }
    override fun getAllEvents(): List<Event.EventEntity>?{
        return null
    }
    override fun removeAlarmConfiguration(id: Long): Boolean{
        return false
    }
    override fun removeEvent(configID: Long, days: String): Boolean{
        return false
    }
    override fun removeEvent(configID: Long): Boolean{
        return false
    }
}

public class DateConverter {
    @TypeConverter
    fun toLocalDate(dateLong: Long?): LocalDate? {
        return if (dateLong == null) {
            null
        } else {
            LocalDate.ofEpochDay(dateLong)
        }
    }
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalTime(timeLong: Long?): LocalTime? {
        return if (timeLong == null) {
            null
        } else {
            LocalTime.ofNanoOfDay(timeLong)
        }
    }
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Long? {
        return time?.toNanoOfDay()
    }

}