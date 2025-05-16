package de.heinzenburger.g2_weckmichmal.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import de.heinzenburger.g2_weckmichmal.backend.AlarmEvent
import de.heinzenburger.g2_weckmichmal.backend.AlarmUpdater
import de.heinzenburger.g2_weckmichmal.api.db.RoutePlanner
import de.heinzenburger.g2_weckmichmal.api.courses.RaplaFetcher
import de.heinzenburger.g2_weckmichmal.calculation.WakeUpCalculator
import de.heinzenburger.g2_weckmichmal.persistence.ConfigurationHandler
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettingsHandler
import de.heinzenburger.g2_weckmichmal.persistence.EventHandler
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.CourseFetcherException
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import de.heinzenburger.g2_weckmichmal.specifications.WakeUpCalculatorException
import java.net.MalformedURLException
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        val url = getRaplaURL()
        val wakeUpCalculator = WakeUpCalculator(
            routePlanner = RoutePlanner(),
            courseFetcher = RaplaFetcher(
                URL(
                    if(url == ""){
                        "http://example.com"
                    }else{
                        url
                    }
                )
            )
        )

        var configurationsWithEvents = getAllConfigurationAndEvent()?.toMutableList()

        configurationsWithEvents?.forEachIndexed {
            index, it ->
            //Strict and Lazy are missing
            var configurationWithEvent : ConfigurationWithEvent
            try {
                log(Logger.Level.INFO, "Updating Event. Before:")
                it.event?.log(this)
                configurationWithEvent = ConfigurationWithEvent(
                    it.configuration,
                    wakeUpCalculator.calculateNextEvent(it.configuration)
                )
                log(Logger.Level.INFO, "Updating Event. After:")
                configurationWithEvent.event?.log(this)
            }
            //If course suddenly disappeared -> No school? -> Do not wake up and delete Event
            catch (e: WakeUpCalculatorException.NoCoursesFound){
                configurationWithEvent = ConfigurationWithEvent(
                    it.configuration, null
                )
                log(Logger.Level.SEVERE, e.message.toString())
                log(Logger.Level.SEVERE, e.stackTraceToString())
            }
            //Course URL suddenly not valid anymore? -> Continue with previously assumed wake up time
            catch (e: WakeUpCalculatorException.CoursesInvalidDataFormatError){
                configurationWithEvent = it
                log(Logger.Level.SEVERE, e.message.toString())
                log(Logger.Level.SEVERE, e.stackTraceToString())
            }
            //Internet connection error -> Continue with previously assumed wake up time
            catch (e: WakeUpCalculatorException.CoursesConnectionError){
                configurationWithEvent = it
                log(Logger.Level.SEVERE, e.message.toString())
                log(Logger.Level.SEVERE, e.stackTraceToString())
            }
            //Something bad happened -> Continue with previously assumed wake up time
            catch (e: Exception){
                configurationWithEvent = it
                log(Logger.Level.SEVERE, e.message.toString())
                log(Logger.Level.SEVERE, e.stackTraceToString())
            }
            configurationsWithEvents[index] = configurationWithEvent
            val eventHandler = EventHandler(context)
            //Save updated event to database
            if(configurationWithEvent.event != null){
                try {
                    eventHandler.saveOrUpdate(configurationWithEvent.event)
                }
                catch (e: PersistenceException){
                    log(Logger.Level.SEVERE, e.message.toString())
                    log(Logger.Level.SEVERE, e.stackTraceToString())
                }
            }
        }

        var earliestEventDate = LocalDateTime.MAX
        var earliestEvent: Event? = null
        configurationsWithEvents?.forEach {
            val eventDateTime = it.event?.date?.atTime(it.event.wakeUpTime)
            if(eventDateTime?.isBefore(LocalDateTime.now()) == false && eventDateTime.isBefore(earliestEventDate) == true){
                earliestEventDate = eventDateTime
                earliestEvent = it.event
            }
        }
        log(Logger.Level.INFO, "Earliest Event is at " + earliestEventDate.format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")))
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
        }
        else if (Duration.between(
                LocalDateTime.now(),
                earliestEventDate
            ).seconds > 1800 // 30 minutes
        ) {
            startUpdateScheduler(900) // 15 minutes
        }
        else if (Duration.between(
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
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmEvent::class.java)
        alarmIntent.putExtra("configID", earliestEvent.configID)
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
        log(Logger.Level.INFO, "Alarm ringing at ${earliestEvent.wakeUpTime}!")
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    override fun startUpdateScheduler(delay: Int) {
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

        log(Logger.Level.INFO, "Alarm updating in ${delay/60} minutes")
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    override fun generateOrUpdateAlarmConfiguration(configuration: Configuration) {
        try {
            val configurationHandler = ConfigurationHandler(context)
            if (validateConfiguration(configuration)){
                val eventHandler = EventHandler(context)
                configurationHandler.saveOrUpdate(configuration)
                var url = getRaplaURL()
                val event = WakeUpCalculator(
                    routePlanner = RoutePlanner(),
                    courseFetcher = RaplaFetcher(
                        URL(
                            if(url == ""){
                                "http://example.com"
                            }else{
                                url
                            }
                        )
                    )
                ).calculateNextEvent(configuration)
                eventHandler.saveOrUpdate(event)

                configuration.log(this)
                event.log(this)
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
        catch (e: WakeUpCalculatorException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Error calculating next Event.")
        }
        catch (e: MalformedURLException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Course URL is not valid.")
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
            if(urlString == "" || isValidCourseURL(urlString)){
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
        var isValid = URLUtil.isValidUrl(urlString)
        try {
            RaplaFetcher(URL(urlString)).throwIfInvalidCourseURL()
        }
        catch (e: CourseFetcherException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            isValid = false
        }
        return isValid
    }

    override fun validateConfiguration(configuration: Configuration): Boolean {
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

