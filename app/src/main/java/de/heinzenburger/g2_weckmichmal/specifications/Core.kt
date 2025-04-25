package de.heinzenburger.g2_weckmichmal.specifications

import de.heinzenburger.g2_weckmichmal.ui.components.SingleAlarmConfigurationProperties
import java.time.LocalTime

interface I_Core{
    /*
    Does not have any state, needs context as parameter
     */
    fun runUpdateLogic()
    fun runWakeUpLogic()
    fun startUpdateScheduler()
    fun generateOrUpdateAlarmConfiguration()
    fun saveRaplaURL(url : String)
    fun getRaplaURL(): String?
    fun getAllAlarmConfigurations(): List<ConfigurationEntity>?
    fun getAllEvents(): List<EventEntity>?
    fun deleteAlarmConfiguration(uid: Long)
    fun saveOrUpdateAlarmConfiguration(configuration: ConfigurationEntity)
    fun saveOrUpdateEvent(event: EventEntity)
    fun getAlarmConfigurationProperties(): List<SingleAlarmConfigurationProperties>?
    fun getPlannedTimeForAlarmEntity(configurationEntity: ConfigurationEntity) : LocalTime?
    fun isApplicationOpenedFirstTime() : Boolean?
    fun setWelcomeScreen()
    fun setSettingsScreen()
    fun setAlarmClockOverviewScreen()
    fun setInformationScreen()
    fun setAlarmClockEditScreen()
}