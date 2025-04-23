package de.heinzenburger.g2_weckmichmal.core

import android.content.Context
import android.content.Intent
import android.widget.Toast
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettings
import de.heinzenburger.g2_weckmichmal.persistence.Event
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.components.AlarmClockEditScreen
import de.heinzenburger.g2_weckmichmal.ui.components.AlarmClockOverviewScreen
import de.heinzenburger.g2_weckmichmal.ui.components.InformationScreen
import de.heinzenburger.g2_weckmichmal.ui.components.SettingsScreen
import de.heinzenburger.g2_weckmichmal.ui.components.SingleAlarmConfigurationProperties
import de.heinzenburger.g2_weckmichmal.ui.components.WelcomeScreen
import java.time.DayOfWeek
import java.time.LocalTime

data class Core(
    val context: Context
) : I_Core {
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

    override fun deleteAlarmConfiguration(uid: Long) {
        val event = Event(context)
        val alarmConfiguration = AlarmConfiguration(context)
        event.removeEvent(uid)
        alarmConfiguration.removeAlarmConfiguration(uid)
    }

    override fun saveOrUpdateAlarmConfiguration(configurationEntity: ConfigurationEntity) {
        val alarmConfiguration = AlarmConfiguration(context)
        alarmConfiguration.saveOrUpdate(configurationEntity)
    }

    override fun saveOrUpdateEvent(eventEntity: EventEntity) {
        val event = Event(context)
        event.saveOrUpdate(eventEntity)
    }

    override fun getAlarmConfigurationProperties(): List<SingleAlarmConfigurationProperties>? {
        var result = ArrayList<SingleAlarmConfigurationProperties>()
        getAllAlarmConfigurations()?.forEach {
            result.add(
                SingleAlarmConfigurationProperties(
                    wakeUpTime = getPlannedTimeForAlarmEntity(it),
                    name = it.name,
                    days = it.days,
                    uid = it.uid,
                    active = true
                )
            )
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

    override fun setAlarmClockEditScreen() {
        val intent = Intent(context, AlarmClockEditScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context.startActivity(intent)
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