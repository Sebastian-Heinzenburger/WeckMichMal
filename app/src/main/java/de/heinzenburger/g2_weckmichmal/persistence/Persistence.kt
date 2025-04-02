package de.heinzenburger.g2_weckmichmal.persistence

import java.util.BitSet

interface Persistence {
    fun saveOrUpdate(config: AlarmConfiguration) // possibly returns an exception
    fun saveOrUpdate(event: Event) // possibly returns an exception
    fun getAlarmConfiguration(id: String): AlarmConfiguration
    fun getAllAlarmConfigurations(): List<AlarmConfiguration>
    fun getAllEvents(): List<Event>
    fun removeAlarmConfiguration(id: String)
    fun removeEvent(configID: String, day: BitSet)
}