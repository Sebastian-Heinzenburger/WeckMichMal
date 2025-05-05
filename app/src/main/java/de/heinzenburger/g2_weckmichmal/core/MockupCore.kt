package de.heinzenburger.g2_weckmichmal.core

import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationAndEventEntity
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_Core

//Is a mockup version of real core, in order to keep UI preview functionality
//Most methods do nothing, only those that cause something in UI are relevant
//For description of each method, see I_Core in specifications
class MockupCore : I_Core {
    override fun deriveStationName(input: String): List<String> {
        return listOf("Europapapapa")
    }

    companion object{
        val mockupConfigurations = listOf(ConfigurationEntity.emptyConfiguration)
        val mockupEvents = listOf(EventEntity.emptyEvent)
    }

    override fun runUpdateLogic() {}
    override fun runWakeUpLogic() {}
    override fun startUpdateScheduler() {}
    override fun saveRaplaURL(url : String){}
    override fun isApplicationOpenedFirstTime(): Boolean? {return null}
    override fun generateOrUpdateAlarmConfiguration(configurationEntity: ConfigurationEntity) {}

    override fun getRaplaURL(): String? {
        return "https://" //For settings and welcome screen
    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationAndEventEntity>? {
        var result = mutableListOf<ConfigurationAndEventEntity>()
        mockupConfigurations.forEach {
            val configurationEntity = it
            var eventEntity : EventEntity? = null
            mockupEvents.forEach { if(it.configID == configurationEntity.uid){ eventEntity = it } }
            result.add(ConfigurationAndEventEntity(configurationEntity,eventEntity))
        }
        return result
    }

    override fun isValidCourseURL(urlString : String) : Boolean {
        return true
    }
    override fun deleteAlarmConfiguration(uid: Long) {
    }

    override fun setWelcomeScreen() {
    }

    override fun setSettingsScreen() {
    }

    override fun setAlarmClockOverviewScreen() {
    }

    override fun setInformationScreen() {
    }

    override fun setAlarmClockEditScreen() {
    }

    override fun showToast(message: String) {
    }
}