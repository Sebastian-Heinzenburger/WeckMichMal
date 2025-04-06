package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import java.util.BitSet

interface Persistence {
    fun saveOrUpdate(config: AlarmConfiguration.ConfigurationEntity): Boolean
    fun saveOrUpdate(event: Event.EventEntity): Boolean
    fun getAlarmConfiguration(id: Long): AlarmConfiguration.ConfigurationEntity
    fun getAllAlarmConfigurations(): List<AlarmConfiguration.ConfigurationEntity>
    fun getAllEvents(): List<Event.EventEntity>
    fun removeAlarmConfiguration(id: Long): Boolean
    fun removeEvent(configID: Long, days: String): Boolean
}