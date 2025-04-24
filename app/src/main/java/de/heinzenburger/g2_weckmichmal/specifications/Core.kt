package de.heinzenburger.g2_weckmichmal.specifications

import de.heinzenburger.g2_weckmichmal.ui.components.SingleAlarmConfigurationProperties
import java.time.LocalTime

interface I_Core{
    fun saveRaplaURL(url : String)
    fun getRaplaURL(): String?
    fun getAllAlarmConfigurations(): List<ConfigurationEntity>?
    fun getAllEvents(): List<EventEntity>?
    fun deleteAlarmConfiguration(uid: Long)
    fun saveOrUpdateAlarmConfiguration(configuration: ConfigurationEntity)
    fun saveOrUpdateEvent(event: EventEntity)
    fun getAlarmConfigurationProperties(): List<SingleAlarmConfigurationProperties>?
    fun getPlannedTimeForAlarmEntity(configurationEntity: ConfigurationEntity) : LocalTime?
    fun setWelcomeScreen()
    fun setSettingsScreen()
    fun setAlarmClockOverviewScreen()
    fun setInformationScreen()
    fun setAlarmClockEditScreen()
}