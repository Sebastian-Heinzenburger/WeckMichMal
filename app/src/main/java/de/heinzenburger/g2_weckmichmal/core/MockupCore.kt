package de.heinzenburger.g2_weckmichmal.core

import android.content.Context
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.Event
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationAndEventEntity
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_Core

class MockupCore : I_Core {
    override fun runUpdateLogic() {
    }

    override fun runWakeUpLogic() {
    }

    override fun startUpdateScheduler() {
    }

    companion object{
        fun insertMockupData(context: Context){
            val event = Event(context)
            val configuration = AlarmConfiguration(context)
            mockupConfigurations.forEach {
                configuration.saveOrUpdate(it)
            }
            mockupEvents.forEach {
                event.saveOrUpdate(it)
            }
        }
        val mockupConfigurations = listOf(ConfigurationEntity.emptyConfiguration)
        val mockupEvents = listOf(EventEntity.emptyEvent)
    }
    override fun saveRaplaURL(url : String){

    }

    override fun getRaplaURL(): String? {
        return "https://"
    }

    override fun isApplicationOpenedFirstTime(): Boolean? {
        return true
    }

    override fun generateOrUpdateAlarmConfiguration(configurationEntity: ConfigurationEntity) {
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
}