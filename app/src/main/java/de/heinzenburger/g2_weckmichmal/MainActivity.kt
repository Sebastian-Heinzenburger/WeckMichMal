package de.heinzenburger.g2_weckmichmal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettings
import de.heinzenburger.g2_weckmichmal.persistence.Event
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.components.AlarmClockOverviewScreen
import de.heinzenburger.g2_weckmichmal.ui.components.InformationScreen
import de.heinzenburger.g2_weckmichmal.ui.components.SettingsScreen
import de.heinzenburger.g2_weckmichmal.ui.components.SingleAlarmConfigurationProperties
import de.heinzenburger.g2_weckmichmal.ui.components.WelcomeScreen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.logging.Logger
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thread {
            MockupCore.insertMockupData(applicationContext)
        }
        Core(context = applicationContext).setWelcomeScreen()
    }
    companion object{
        val log: Logger = Logger.getLogger(AlarmConfiguration::class.java.name)
    }
}


/*
Schön wäre es gewesen, wenn man einmalig irgendwo den Core instanziiert und dann dauernd mit dieser Instanz arbeitet, aber
das stellt sich als schwer heraus.
Android UI funktioniert mit Context. Der Context beschreibt die momentan im Vordergrund stehende Komponente.
Soll eine neue Komponente im Vordergrund stehen, wird der Context weiter gereicht. Aus diesem Grund darf der Context
nie irgendwo hängen bleiben, wie zum Beispiel als statische Variable. Das Problem bei unserer Architektur ist, dass sogar die
tiefsten Komponenten wie die Persistenz einen Context brauchen um zu funktionieren. Deswegen braucht unser Core diesen Context.
Man kann den Core also nicht einmalig instanziieren und dann in eine statische variable klatschen.
Die zweite Option wäre die Instanz des Cores einfach immer an die Komponente im Vordergrund weiter zu reichen. Aber auch
das krieg ich nicht hin. Also instanziieren wir momentan den Core jedes Mal neu wenn ein neuer Screen aufgerufen wird. Vielleicht
ist das auch besser so
 */


data class Core(
    val context: Context
) : I_Core{
    override fun saveRaplaURL(url : String){
        val applicationSettings = ApplicationSettings(context)
        val settingsEntity = applicationSettings.getApplicationSettings()
        settingsEntity.raplaURL = url
        if(!applicationSettings.saveOrUpdateApplicationSettings(settingsEntity)){
            Toast.makeText(context, "Etwas ist schiefgelaufen (saveRaplaURL)", Toast.LENGTH_LONG).show()
        }
    }
    override fun getAllAlarmConfigurations(): List<ConfigurationEntity>?{
        val alarmConfiguration = AlarmConfiguration(context)
        return alarmConfiguration.getAllAlarmConfigurations()
    }

    override fun getAllEvents(): List<EventEntity>? {
        val event = Event(context)
        return event.getAllEvents()
    }

    override fun getAlarmConfigurationProperties(): List<SingleAlarmConfigurationProperties>? {
        var result = ArrayList<SingleAlarmConfigurationProperties>()
        getAllAlarmConfigurations()?.forEach {
            it.log()
            result.add(SingleAlarmConfigurationProperties(
                wakeUpTime = getPlannedTimeForAlarmEntity(it),
                name = it.name,
                days = it.days,
                uid = it.uid
            ))
        }
        return result
    }

    override fun getPlannedTimeForAlarmEntity(configurationEntity: ConfigurationEntity) : LocalTime?{
        val event = Event(context)
        return event.getEvent(configurationEntity.uid, configurationEntity.days)?.wakeUpTime
    }

    override fun setWelcomeScreen() {
        val intent = Intent(context, WelcomeScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context.startActivity(intent)
    }

    override fun setSettingsScreen() {
        val intent = Intent(context, SettingsScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context.startActivity(intent)
    }

    override fun setAlarmClockOverviewScreen() {
        val intent = Intent(context, AlarmClockOverviewScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context.startActivity(intent)
    }

    override fun setInformationScreen() {
        val intent = Intent(context, InformationScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context.startActivity(intent)
    }
}


class MockupCore : I_Core{
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
                days = setOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
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
                date = LocalDate.of(2025,4,20)
            ),
            EventEntity(
                configID = 12346,
                wakeUpTime = LocalTime.of(8, 0),
                days = setOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                date = LocalDate.of(2025,4,20)
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

    override fun getAlarmConfigurationProperties(): List<SingleAlarmConfigurationProperties>? {
        var result = ArrayList<SingleAlarmConfigurationProperties>()
        mockupConfigurations.forEach {
            val config = it
            mockupEvents.forEach {
                if(config.days == it.days && config.uid == it.configID){
                    result.add(SingleAlarmConfigurationProperties(
                        wakeUpTime = it.wakeUpTime,
                        name = config.name,
                        days = config.days,
                        uid = config.uid
                    ))
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
}