package de.heinzenburger.g2_weckmichmal.specifications

import java.time.LocalTime

interface I_Core{
    /*
    To be implemented: Does not have any state, needs context as parameter
     */
    fun runUpdateLogic()
    fun runWakeUpLogic()
    fun startUpdateScheduler()

    fun saveRaplaURL(url : String)
    fun getRaplaURL(): String?
    fun isApplicationOpenedFirstTime() : Boolean?
    fun generateOrUpdateAlarmConfiguration(configurationEntity: ConfigurationEntity)
    fun getAllConfigurationAndEvent() : List<ConfigurationAndEventEntity>?
    fun deleteAlarmConfiguration(uid: Long)

    fun setWelcomeScreen()
    fun setSettingsScreen()
    fun setAlarmClockOverviewScreen()
    fun setInformationScreen()
    fun setAlarmClockEditScreen()
}