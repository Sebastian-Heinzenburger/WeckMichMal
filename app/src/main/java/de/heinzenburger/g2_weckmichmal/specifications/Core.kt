package de.heinzenburger.g2_weckmichmal.specifications

import java.time.LocalTime

interface I_Core{
    fun saveRaplaURL(url : String)
    fun getAllAlarmConfigurations(): List<ConfigurationEntity>?
    fun getPlannedTimeForAlarmEntity(configurationEntity: ConfigurationEntity) : LocalTime?
    fun setWelcomeScreen()
    fun setSettingsScreen()
    fun setAlarmClockOverviewScreen()
    fun setInformationScreen()
}