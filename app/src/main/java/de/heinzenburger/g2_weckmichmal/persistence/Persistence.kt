package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import androidx.room.TypeConverter
import java.util.BitSet
import java.util.Date

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
    fun toDate(dateLong : Long?) : Date?{
        return if(dateLong == null){
            null
        } else{
            Date(dateLong)
        }
    }

    @TypeConverter
    fun toLocalTime(dateLong : Long?) : Date?{
        return if(dateLong == null){
            null
        } else{
            Date(dateLong)
        }
    }

    @TypeConverter
    fun fromDate(date: Date?) : Long?{
        return date?.time
    }
}