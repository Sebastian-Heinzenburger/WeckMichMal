package de.heinzenburger.g2_weckmichmal.core

import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import android.widget.Toast
import de.heinzenburger.g2_weckmichmal.MainActivity
import de.heinzenburger.g2_weckmichmal.api.db.RoutePlanner
import de.heinzenburger.g2_weckmichmal.api.rapla.CoursesFetcher
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettings
import de.heinzenburger.g2_weckmichmal.persistence.Event
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationAndEventEntity
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.components.AlarmClockEditScreen
import de.heinzenburger.g2_weckmichmal.ui.components.AlarmClockOverviewScreen
import de.heinzenburger.g2_weckmichmal.ui.components.InformationScreen
import de.heinzenburger.g2_weckmichmal.ui.components.SettingsScreen
import de.heinzenburger.g2_weckmichmal.ui.components.WelcomeScreen
import java.net.URL
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

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
) : I_Core {
    //For description of each method, see I_Core in specifications
    override fun deriveStationName(input: String) : List<String>{
        val routePlanner = RoutePlanner()
        return routePlanner.deriveValidStationNames(input)
    }
    override fun runUpdateLogic() {
    }

    override fun runWakeUpLogic() {
    }

    override fun startUpdateScheduler() {
    }

    override fun generateOrUpdateAlarmConfiguration(configurationEntity: ConfigurationEntity) {
        val alarmConfiguration = AlarmConfiguration(context)
        val event = Event(context)
        alarmConfiguration.saveOrUpdate(configurationEntity)
        /*event.saveOrUpdate(WakeUpCalculator(
            routePlanner = I_RoutePlannerSpecification,
            courseFetcher = CoursesFetcher(URL(getRaplaURL()))
        ).calculateNextEvent(configurationEntity))*/
        var eventEntity = EventEntity(
            configID = configurationEntity.uid,
            wakeUpTime = LocalTime.NOON,
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
            date = LocalDate.now(),
            courses = null,
            routes = null
        )
        event.saveOrUpdate(eventEntity)
    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationAndEventEntity>? {
        val alarmConfiguration = AlarmConfiguration(context)
        return alarmConfiguration.getAllConfigurationAndEvent()
    }

    override fun saveRaplaURL(urlString : String){
        val applicationSettings = ApplicationSettings(context)
        val settingsEntity = applicationSettings.getApplicationSettings()
        settingsEntity.raplaURL = urlString
        applicationSettings.saveOrUpdateApplicationSettings(settingsEntity)
    }

    override fun isValidCourseURL(urlString : String) : Boolean{
        return  URLUtil.isValidUrl(urlString) && CoursesFetcher(URL(urlString)).hasValidCourseURL()
    }

    override fun getRaplaURL(): String? {
        val applicationSettings = ApplicationSettings(context)
        return applicationSettings.getApplicationSettings().raplaURL
    }

    override fun deleteAlarmConfiguration(uid: Long) {
        val event = Event(context)
        val alarmConfiguration = AlarmConfiguration(context)
        //Always remove all associated events when removing an alarm configuration
        event.removeEvent(uid)
        alarmConfiguration.removeAlarmConfiguration(uid)
    }

    override fun isApplicationOpenedFirstTime(): Boolean? {
        val settings = ApplicationSettings(context)
        return settings.isApplicationOpenedFirstTime()
    }

    override fun setWelcomeScreen() {
        val intent = Intent(context, WelcomeScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }

    override fun setSettingsScreen() {
        val intent = Intent(context, SettingsScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }

    override fun setAlarmClockOverviewScreen() {
        val intent = Intent(context, AlarmClockOverviewScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }

    override fun setInformationScreen() {
        val intent = Intent(context, InformationScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }

    override fun setAlarmClockEditScreen() {
        val intent = Intent(context, AlarmClockEditScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }

    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

