package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import java.util.BitSet

interface Persistence {
    fun saveOrUpdate(config: AlarmConfiguration.ConfigurationEntity): Boolean
    fun saveOrUpdate(event: Event): Boolean
    fun getAlarmConfiguration(id: Long): AlarmConfiguration.ConfigurationEntity
    fun getAllAlarmConfigurations(): List<AlarmConfiguration.ConfigurationEntity>
    fun getAllEvents(): List<Event>
    fun removeAlarmConfiguration(id: Long): Boolean
    fun removeEvent(configID: String, day: BitSet): Boolean
}