package de.heinzenburger.g2_weckmichmal.core

import android.content.Context
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.Event
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.components.SingleAlarmConfigurationProperties
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class MockupCore : I_Core {
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
        val mockupConfigurations = listOf(
            ConfigurationEntity(
                uid = 12345,
                name = "Alarm 1",
                days = setOf(DayOfWeek.MONDAY, DayOfWeek.SATURDAY),
                fixedArrivalTime = null,
                fixedTravelBuffer = null,
                startBuffer = 20,
                endBuffer = 10,
                startStation = "Euro",
                endStation = "Hochschule"
            ),
            ConfigurationEntity(
                uid = 12346,
                name = "Alarm 2",
                days = setOf(
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                ),
                fixedArrivalTime = null,
                fixedTravelBuffer = null,
                startBuffer = 20,
                endBuffer = 10,
                startStation = "Euro",
                endStation = "Hochschule"
            )
        )
        val mockupEvents = listOf(
            EventEntity(
                configID = 12345,
                wakeUpTime = LocalTime.NOON,
                days = setOf(DayOfWeek.MONDAY, DayOfWeek.SATURDAY),
                date = LocalDate.of(2025, 4, 20)
            ),
            EventEntity(
                configID = 12346,
                wakeUpTime = LocalTime.of(8, 0),
                days = setOf(
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                ),
                date = LocalDate.of(2025, 4, 20)
            )
        )
    }
    override fun saveRaplaURL(url : String){

    }
    override fun getAllAlarmConfigurations(): List<ConfigurationEntity>?{
        return mockupConfigurations
    }

    override fun getAllEvents(): List<EventEntity>? {
        return mockupEvents
    }

    override fun deleteAlarmConfiguration(uid: Long) {
    }

    override fun saveOrUpdateAlarmConfiguration(configuration: ConfigurationEntity) {
    }

    override fun saveOrUpdateEvent(event: EventEntity) {
    }

    override fun getAlarmConfigurationProperties(): List<SingleAlarmConfigurationProperties>? {
        var result = ArrayList<SingleAlarmConfigurationProperties>()
        mockupConfigurations.forEach {
            val config = it
            mockupEvents.forEach {
                if(config.days == it.days && config.uid == it.configID){
                    result.add(
                        SingleAlarmConfigurationProperties(
                            wakeUpTime = it.wakeUpTime,
                            name = config.name,
                            days = config.days,
                            uid = config.uid,
                            active = true
                        )
                    )
                }
            }
        }
        return result
    }

    override fun getPlannedTimeForAlarmEntity(configurationEntity: ConfigurationEntity) : LocalTime?{
        mockupEvents.forEach {
            if(it.configID == configurationEntity.uid && it.days == configurationEntity.days){
                return it.wakeUpTime
            }
        }
        return null
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
        TODO("Not yet implemented")
    }
}