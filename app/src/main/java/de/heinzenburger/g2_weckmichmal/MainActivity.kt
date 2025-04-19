package de.heinzenburger.g2_weckmichmal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettings
import de.heinzenburger.g2_weckmichmal.persistence.Event
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity
import de.heinzenburger.g2_weckmichmal.ui.components.WelcomeScreen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thread {
            val applicationSettings = ApplicationSettings(this)
            log.severe(applicationSettings.isApplicationOpenedFirstTime().toString())
            var settings = SettingsEntity(
                raplaURL = "https://ulululu"
            )
            applicationSettings.saveOrUpdateApplicationSettings(settings)
            log.severe(applicationSettings.isApplicationOpenedFirstTime().toString())


            //Lazy Testing Persistence Functionality
            val alarmConfiguration = AlarmConfiguration(this)
            for(config in alarmConfiguration.getAllAlarmConfigurations()!!){
                config.log()
            }

            val fixedArrivalTime = LocalTime.parse("20:00:00")
            val testConfiguration = ConfigurationEntity(
                name = "!!pineapple",
                days = setOf<DayOfWeek>(DayOfWeek.MONDAY, DayOfWeek.FRIDAY, DayOfWeek.TUESDAY),
                fixedArrivalTime = fixedArrivalTime,
                fixedTravelBuffer = 20,
                startBuffer = 30,
                endBuffer = 0,
                startStation = "Durlach",
                endStation = "Exmatrikulation"
            )
            alarmConfiguration.saveOrUpdate(testConfiguration)
            alarmConfiguration.getAlarmConfiguration(testConfiguration.uid)?.log()
            testConfiguration.days = setOf<DayOfWeek>(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
            alarmConfiguration.saveOrUpdate(testConfiguration)
            alarmConfiguration.getAlarmConfiguration(testConfiguration.uid)?.log()
            for(config in alarmConfiguration.getAllAlarmConfigurations()!!){
                config.log()
            }
            alarmConfiguration.removeAlarmConfiguration(testConfiguration.uid)
            for(config in alarmConfiguration.getAllAlarmConfigurations()!!){
                config.log()
            }

            val event = Event(this)
            for(config in event.getAllEvents()!!){
                config.log()
            }

            val testEntity = EventEntity(
                configID = testConfiguration.uid,
                days = testConfiguration.days,
                wakeUpTime = LocalTime.now(),
                date = LocalDate.now()
            )
            event.saveOrUpdate(testEntity)
            for(config in event.getAllEvents()!!){
                config.log()
            }
            event.removeEvent(testConfiguration.uid, testConfiguration.days)
            for(config in event.getAllEvents()!!){
                config.log()
            }
        }

        startActivity(Intent(this, WelcomeScreen::class.java))
    }
    companion object{
        val log: Logger = Logger.getLogger(AlarmConfiguration::class.java.name)
    }
}
