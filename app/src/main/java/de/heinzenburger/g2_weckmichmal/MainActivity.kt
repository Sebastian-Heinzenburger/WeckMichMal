package de.heinzenburger.g2_weckmichmal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.persistence.Event
import de.heinzenburger.g2_weckmichmal.ui.components.WelcomeScreen
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*thread {
            //Lazy Testing Persistence Functionality
            val alarmConfiguration = AlarmConfiguration(this)
            for(config in alarmConfiguration.getAllAlarmConfigurations()!!){
                config.log()
            }

            val fixedArrivalTime = LocalTime.parse("20:00:00")
            val testConfiguration = ConfigurationEntity(
                name = "!!pineapple",
                days = "1010000",
                fixedArrivalTime = fixedArrivalTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                fixedTravelBuffer = 20,
                startBuffer = 30,
                endBuffer = 0,
                startStation = "Durlach",
                endStation = "Exmatrikulation"
            )
            alarmConfiguration.saveOrUpdate(testConfiguration)
            alarmConfiguration.getAlarmConfiguration(testConfiguration.uid)?.log()
            testConfiguration.days = "1110000"
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

            val testEntity = Event.EventEntity(
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
        }*/

        startActivity(Intent(this, WelcomeScreen::class.java))
    }
    companion object{
        val log: Logger = Logger.getLogger(AlarmConfiguration::class.java.name)
    }
}
