package de.heinzenburger.g2_weckmichmal.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import de.heinzenburger.g2_weckmichmal.AlarmEvent
import de.heinzenburger.g2_weckmichmal.AlarmUpdater
import de.heinzenburger.g2_weckmichmal.api.db.RoutePlanner
import de.heinzenburger.g2_weckmichmal.api.courses.RaplaFetcher
import de.heinzenburger.g2_weckmichmal.calculation.WakeUpCalculator
import de.heinzenburger.g2_weckmichmal.persistence.ConfigurationHandler
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettingsHandler
import de.heinzenburger.g2_weckmichmal.persistence.EventHandler
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

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
    val context: Context,
) : I_Core {
    val logger = Logger(context)

    //For description of each method, see I_Core in specifications
    override fun deriveStationName(input: String): List<String> {
        val routePlanner = RoutePlanner()
        return routePlanner.deriveValidStationNames(input)
    }

    override fun runUpdateLogic() {
        var configurationsWithEventsBeforeUpdate = getAllConfigurationAndEvent()
        configurationsWithEventsBeforeUpdate?.forEach {
            generateOrUpdateAlarmConfiguration(configuration = it.configuration)
        }
        var configurationsWithEventsAfterUpdate = getAllConfigurationAndEvent()
        var earliestEventDate = LocalDateTime.of(9999,1,1,0,0)
        var earliestEvent: Event? = null
        configurationsWithEventsAfterUpdate?.forEach {
            val eventDateTime = it.event?.date?.atTime(it.event.wakeUpTime)
            if(eventDateTime?.isBefore(LocalDateTime.now()) == false && eventDateTime.isBefore(earliestEventDate) == true){
                earliestEventDate = eventDateTime
                earliestEvent = it.event
            }
        }
        if (Duration.between(
                LocalDateTime.now(),
                earliestEventDate
            ).seconds > 28800 // 8 hours
        ) {
            startUpdateScheduler(25200) // 7 hours
        }else if (Duration.between(
                LocalDateTime.now(),
                earliestEventDate
            ).seconds > 14400 //4 hours
        ) {
            startUpdateScheduler(12600) // 3.5 hours
        }
        else if (Duration.between(
                LocalDateTime.now(),
                earliestEventDate
            ).seconds > 3600 // 1 hour
        ) {
            startUpdateScheduler(1800) // 30 minutes
        } else if (Duration.between(
                LocalDateTime.now(),
                earliestEventDate
            ).seconds > 600 // 10 minutes
        ) {
            startUpdateScheduler(240) // 4 minutes
        } else {
            runWakeUpLogic(earliestEvent!!)
        }
    }

    override fun runWakeUpLogic(earliestEvent: Event) {
        log(Logger.Level.SEVERE, "Alarm ringing at ${earliestEvent.wakeUpTime}!")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmEvent::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pendingEditIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        val alarmDate = earliestEvent.date.atTime(earliestEvent.wakeUpTime)
        val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingEditIntent)
        log(Logger.Level.SEVERE, alarmDate.toString())
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    override fun startUpdateScheduler(delay: Int) {
        log(Logger.Level.SEVERE, "Alarm updating in $delay seconds")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmUpdater::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pendingEditIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            LocalDateTime.now().plusSeconds(delay.toLong()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingEditIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    override fun generateOrUpdateAlarmConfiguration(configuration: Configuration) {
        try {
            val configurationHandler = ConfigurationHandler(context)

            if (validateConfigurationEntity(configuration)){
                val eventHandler = EventHandler(context)
                configurationHandler.saveOrUpdate(configuration)
                val event = WakeUpCalculator(
                    routePlanner = RoutePlanner(),
                    courseFetcher = RaplaFetcher(URL(getRaplaURL()))
                ).calculateNextEvent(configuration)
                eventHandler.saveOrUpdate(event)

                configuration.log()
                event.log()
            }
            else{
                showToast("Configuration is not valid")
            }
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Error communicating with database. Try reinstalling the app.")
        }
    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationWithEvent>? {
        try {
            val configurationHandler = ConfigurationHandler(context)
            return configurationHandler.getAllConfigurationAndEvent()
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Could not load alarms from database. Try reinstalling the app.")
            return null
        }
    }

    override fun saveRaplaURL(urlString : String){
        try {
            if(isValidCourseURL(urlString)){
                val applicationSettingsHandler = ApplicationSettingsHandler(context)
                val settingsEntity = applicationSettingsHandler.getApplicationSettings()
                settingsEntity.raplaURL = urlString
                applicationSettingsHandler.saveOrUpdateApplicationSettings(settingsEntity)
            }
            else{
                showToast("Invalid URL")
            }
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Error saving URL to database. Try reinstalling the app.")
        }
    }

    override fun isValidCourseURL(urlString : String) : Boolean{
        return  URLUtil.isValidUrl(urlString) && RaplaFetcher(URL(urlString)).hasValidCourseURL()
    }

    override fun validateConfigurationEntity(configuration: Configuration): Boolean {
        var validation = true
        // Should contain a name
        if(configuration.name == ""){
            validation = false
        }
        // Days should not be empty
        else if(configuration.days.isEmpty()){
            validation = false
        }
        if(configuration.startStation != null || configuration.endStation != null){
            // If one station is set but one is null, error
            if(configuration.startStation == null || configuration.endStation == null){
                validation = false
            }
            // Station exists if can be found in List of DB similar station names
            else if (!RoutePlanner().deriveValidStationNames(configuration.startStation!!).contains(configuration.startStation!!)
                || !RoutePlanner().deriveValidStationNames(configuration.endStation!!).contains(configuration.endStation!!)){
                validation = false
            }
        }
        return validation
    }

    override fun getRaplaURL(): String? {
        try {
            val applicationSettingsHandler = ApplicationSettingsHandler(context)
            return applicationSettingsHandler.getApplicationSettings().raplaURL
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Error retrieving URL from database. Try reinstalling the app.")
            return null
        }
    }

    override fun log(
        level: Logger.Level,
        text: String
    ) {
        try {
            logger.log(level, text)
        }
        catch (e: PersistenceException){
            val logger = Logger(null)
            logger.log(Logger.Level.SEVERE, "Can't write to log " + e.message)
        }
        catch (e: Exception){
            showToast("Error with Log writing " + e.message)
        }
    }

    override fun getLog(): String {
        return try {
            logger.getLogs()
        } catch (e: PersistenceException){
            e.message.toString()
        }
    }

    override fun updateConfigurationActive(
        isActive: Boolean,
        configuration: Configuration
    ) {
        try {
            val configurationHandler = ConfigurationHandler(context)
            configurationHandler.updateConfigurationActive(isActive, configuration.uid)
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Could not update active state of configuration.")
        }
    }

    override fun deleteAlarmConfiguration(uid: Long) {
        try {
            val eventHandler = EventHandler(context)
            val configurationHandler = ConfigurationHandler(context)
            //Always remove all associated events when removing an alarm configuration
            eventHandler.removeEvent(uid)
            configurationHandler.removeAlarmConfiguration(uid)
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Could not delete from database. Try reinstalling the app.")
        }
    }

    override fun isApplicationOpenedFirstTime(): Boolean {
        val settings = ApplicationSettingsHandler(context)
        return settings.isApplicationOpenedFirstTime()
    }

    override fun showToast(message: String) {
        try {
            ContextCompat.getMainExecutor(context).execute( { ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            })
        }
        catch (e: Exception){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
        }
    }
}

