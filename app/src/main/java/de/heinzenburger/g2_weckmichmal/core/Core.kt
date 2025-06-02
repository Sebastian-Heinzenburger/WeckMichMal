package de.heinzenburger.g2_weckmichmal.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import de.heinzenburger.g2_weckmichmal.background.AlarmEvent
import de.heinzenburger.g2_weckmichmal.background.AlarmUpdater
import de.heinzenburger.g2_weckmichmal.api.routes.DBRoutePlanner
import de.heinzenburger.g2_weckmichmal.api.courses.RaplaFetcher
import de.heinzenburger.g2_weckmichmal.calculation.WakeUpCalculator
import de.heinzenburger.g2_weckmichmal.persistence.ConfigurationHandler
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettingsHandler
import de.heinzenburger.g2_weckmichmal.persistence.EventHandler
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.CourseFetcherException
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import de.heinzenburger.g2_weckmichmal.specifications.WakeUpCalculatorException
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import de.heinzenburger.g2_weckmichmal.api.courses.deriveValidCourseURL
import de.heinzenburger.g2_weckmichmal.api.mensa.StudierendenWerkKarlsruhe
import de.heinzenburger.g2_weckmichmal.specifications.MensaMeal
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class Core(
    val context: Context,
) : CoreSpecification {
    val logger = Logger(context)

    //For description of each method, see I_Core in specifications
    override fun deriveStationName(input: String): List<String> {
        log(Logger.Level.INFO, "deriveStationName called with input: $input")
        val routePlanner = DBRoutePlanner()
        val result = routePlanner.deriveValidStationNames(input)
        log(Logger.Level.INFO, "deriveStationName result: $result")
        return result
    }

    override fun nextMensaMeals(): List<MensaMeal> {
        return StudierendenWerkKarlsruhe().nextMeals()
    }

    fun scheduleNextAction(configurationsWithEvents: List<ConfigurationWithEvent>?){
        var earliestEventDate = LocalDateTime.MAX
        var earliestConfigurationWithEvent: ConfigurationWithEvent? = null
        configurationsWithEvents?.forEach {
            val eventDateTime = it.event?.getLocalDateTime()
            log(Logger.Level.INFO, "Checking eventDateTime: $eventDateTime for configuration: ${it.configuration}")
            if(it.configuration.isActive
                && it.configuration.ichHabGeringt.isBefore(LocalDate.now())
                && eventDateTime?.isBefore(earliestEventDate) == true){

                earliestEventDate = eventDateTime
                earliestConfigurationWithEvent = it
                log(Logger.Level.INFO, "New earliestEventDate: $earliestEventDate, earliestEvent: $earliestConfigurationWithEvent")
            }
        }
        log(Logger.Level.INFO, "Earliest Event is at " + earliestEventDate.format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")))
        val secondsUntilEarliestEvent = Duration.between(LocalDateTime.now(), earliestEventDate).seconds
        log(Logger.Level.INFO, "Seconds until earliest event: $secondsUntilEarliestEvent")
        if (secondsUntilEarliestEvent > 28800 // 8 hours
        ) {
            log(Logger.Level.INFO, "Scheduling update in 7 hours")
            startUpdateScheduler(25200) // 7 hours
        }else if (secondsUntilEarliestEvent > 14400 //4 hours
        ) {
            log(Logger.Level.INFO, "Scheduling update in 3.5 hours")
            startUpdateScheduler(12600) // 3.5 hours
        }
        else if (secondsUntilEarliestEvent > 3600 // 1 hour
        ) {
            log(Logger.Level.INFO, "Scheduling update in 30 minutes")
            startUpdateScheduler(1800) // 30 minutes
        }
        else if (secondsUntilEarliestEvent > 1800 // 30 minutes
        ) {
            log(Logger.Level.INFO, "Scheduling update in 15 minutes")
            startUpdateScheduler(900) // 15 minutes
        }
        else if (secondsUntilEarliestEvent > 600 // 10 minutes
        ) {
            log(Logger.Level.INFO, "Scheduling update in 4 minutes")
            startUpdateScheduler(240) // 4 minutes
        } else {
            log(Logger.Level.INFO, "Running wake up logic for earliestEvent: $earliestConfigurationWithEvent")
            runWakeUpLogic(earliestConfigurationWithEvent!!)
        }
    }

    fun calculateNextEventForConfiguration(it: ConfigurationWithEvent, wakeUpCalculator: WakeUpCalculator) : ConfigurationWithEvent{
        log(Logger.Level.INFO, "Processing configurationWithEvent with index ${it.configuration.uid}: $it")
        //Skip updating configuration if inactive
        if(!it.configuration.isActive){
            log(Logger.Level.INFO, "Configuration with index ${it.configuration.uid} is inactive, skipping.")
            return it
        }

        var configurationWithEvent = it
        try {
            log(Logger.Level.INFO, "Updating Event. Before:")
            it.event?.log(this)
            configurationWithEvent = ConfigurationWithEvent(
                it.configuration,
                wakeUpCalculator.calculateNextEvent(it.configuration, strict = true)
            )
            log(Logger.Level.INFO, "Updating Event. After:")
            configurationWithEvent.event?.log(this)
        }
        catch (e: WakeUpCalculatorException.NoRoutesFound){
            log(Logger.Level.INFO, "NoRoutesFound exception caught for configuration with index ${it.configuration.uid}")
            if(it.configuration.enforceStartBuffer){
                try {
                    configurationWithEvent = ConfigurationWithEvent(
                        it.configuration,
                        wakeUpCalculator.calculateNextEvent(it.configuration, strict = false)
                    )
                    log(Logger.Level.SEVERE, "No Routes found and enforcing start buffer.")
                }
                catch (e: Exception){
                    configurationWithEvent = it
                    log(Logger.Level.SEVERE, "No Routes found and enforcing start buffer but another exception.")
                    log(Logger.Level.SEVERE, e.message.toString())
                    log(Logger.Level.SEVERE, e.stackTraceToString())
                }
            }
            else{
                log(Logger.Level.SEVERE, "No Routes found and not enforcing start buffer. Wake up now!")
                log(Logger.Level.SEVERE, e.message.toString())
                log(Logger.Level.SEVERE, e.stackTraceToString())
                if(it.event != null){
                    configurationWithEvent = it
                    configurationWithEvent.event.wakeUpTime = LocalTime.now()
                }
            }
        }
        //If course suddenly disappeared -> No school? -> Do not wake up and delete Event
        catch (e: WakeUpCalculatorException.EventDoesNotYetExist){
            log(Logger.Level.INFO, "NoCoursesFound exception caught for configuration with index ${it.configuration.uid}")
            configurationWithEvent = ConfigurationWithEvent(
                it.configuration, null
            )
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        //Course URL suddenly not valid anymore? -> Continue with previously assumed wake up time
        catch (e: WakeUpCalculatorException.CoursesInvalidDataFormatError){
            log(Logger.Level.INFO, "CoursesInvalidDataFormatError exception caught for configuration with index ${it.configuration.uid}")
            configurationWithEvent = it
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        //Internet connection error -> Continue with previously assumed wake up time
        catch (e: WakeUpCalculatorException.CoursesConnectionError){
            log(Logger.Level.INFO, "CoursesConnectionError exception caught for configuration with index ${it.configuration.uid}")
            configurationWithEvent = it
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        //Something bad happened -> Continue with previously assumed wake up time
        catch (e: Exception){
            log(Logger.Level.INFO, "Generic exception caught for configuration with index ${it.configuration.uid}: ${e.message}")
            configurationWithEvent = it
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        return configurationWithEvent
    }

    override fun runUpdateLogic() {
        log(Logger.Level.INFO, "runUpdateLogic started")

        var configurationsWithEvents = getAllConfigurationAndEvent()?.toMutableList()
        log(Logger.Level.INFO, "ConfigurationsWithEvents loaded: $configurationsWithEvents")

        log(Logger.Level.INFO, "Starting safety alarm scheduler")
        scheduleNextAction(configurationsWithEvents)


        log(Logger.Level.INFO, "Starting regular alarm scheduler")
        val url = getRaplaURL()
        log(Logger.Level.INFO, "Rapla URL: $url")
        val wakeUpCalculator = WakeUpCalculator(
            routePlanner = DBRoutePlanner(),
            courseFetcher = RaplaFetcher(
                raplaUrl = URL(
                    if(url == ""){
                        "http://example.com"
                    }else{
                        url
                    }
                ),
                excludedCourseNames = getListOfExcludedCourses().toSet()
            )
        )


        configurationsWithEvents?.forEachIndexed {
            index, it ->
            log(Logger.Level.SEVERE, "HALLO0000")
            var configurationWithEvent = calculateNextEventForConfiguration(it, wakeUpCalculator)
            configurationsWithEvents[index] = configurationWithEvent
            val eventHandler = EventHandler(context)
            //Save updated event to database
            if(configurationWithEvent.event != null){
                try {
                    log(Logger.Level.INFO, "Saving or updating event: ${configurationWithEvent.event}")
                    eventHandler.saveOrUpdate(configurationWithEvent.event)
                }
                catch (e: PersistenceException){
                    log(Logger.Level.SEVERE, e.message.toString())
                    log(Logger.Level.SEVERE, e.stackTraceToString())
                }
            }
        }

        scheduleNextAction(configurationsWithEvents)
    }

    override fun runWakeUpLogic(earliestConfigurationWithEvent: ConfigurationWithEvent) {
        try {
            log(Logger.Level.INFO, "runWakeUpLogic called with earliestEvent: $earliestConfigurationWithEvent")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, AlarmEvent::class.java)
            alarmIntent.putExtra("configurationWithEvent", earliestConfigurationWithEvent)
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
                log(Logger.Level.INFO, "Requesting permission to schedule exact alarms")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            val alarmDate = earliestConfigurationWithEvent.event!!.getLocalDateTime()
            log(Logger.Level.INFO, "Setting alarm for date: $alarmDate")

            val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                pendingEditIntent)
            log(Logger.Level.INFO, "Alarm ringing at ${earliestConfigurationWithEvent.event.wakeUpTime}!")
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        }
        catch (e: Exception){
            log(Logger.Level.INFO, "HILFEEEE DER RUN WAKEUPLOGIC IST ABGESTÜRZT!!!")
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            runUpdateLogic()
        }
    }

    override fun startUpdateScheduler(delay: Int) {
        try {
            log(Logger.Level.INFO, "startUpdateScheduler called with delay: $delay")
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
                log(Logger.Level.INFO, "Requesting permission to schedule exact alarms for update scheduler")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            val triggerTime = LocalDateTime.now().plusSeconds(delay.toLong())
            log(Logger.Level.INFO, "Scheduling update alarm for: $triggerTime")

            val alarmClockInfo = AlarmManager.AlarmClockInfo(
                triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                pendingEditIntent)

            log(Logger.Level.INFO, "Alarm updating in ${delay/60} minutes")
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        }
        catch (e : Exception){
            log(Logger.Level.INFO, "HILFEEEE DER START UPDATE SCHEDULER IST ABGESTÜRZT!!!")
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            runUpdateLogic()
        }
    }

    override fun generateOrUpdateAlarmConfiguration(configuration: Configuration) {
        log(Logger.Level.INFO, "generateOrUpdateAlarmConfiguration called with configuration: $configuration")
        try {
            val configurationHandler = ConfigurationHandler(context)
            if (validateConfiguration(configuration)){
                log(Logger.Level.INFO, "Configuration is valid, saving or updating.")
                var url = getRaplaURL()
                val wakeUpCalculator = WakeUpCalculator(
                    routePlanner = DBRoutePlanner(),
                    courseFetcher = RaplaFetcher(
                        raplaUrl = URL(
                            if(url == ""){
                                "http://example.com"
                            }else{
                                url
                            }
                        ),
                        excludedCourseNames = getListOfExcludedCourses().toSet()
                    )
                )

                configuration.ichHabGeringt = LocalDate.MIN
                if(calculateNextEventForConfiguration(ConfigurationWithEvent(configuration,null),wakeUpCalculator).event?.getLocalDateTime()?.minusMinutes(2)
                        ?.isBefore(
                            LocalDateTime.now()) == true
                ){
                    configuration.ichHabGeringt = LocalDate.now()
                    showToast("Alarm für heute ausgesetzt")
                }

                configurationHandler.saveOrUpdate(configuration)

                val eventHandler = EventHandler(context)
                log(Logger.Level.INFO, "Using Rapla URL: $url")
                val event = wakeUpCalculator.calculateNextEvent(configuration)
                log(Logger.Level.INFO, "Calculated event: $event")
                eventHandler.saveOrUpdate(event)

                configuration.log(this)
                event.log(this)
            }
            else{
                log(Logger.Level.INFO, "Configuration is not valid")
                showToast("Configuration is not valid")
            }
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Error communicating with database. Try reinstalling the app.")
        }
        catch (e: WakeUpCalculatorException.EventDoesNotYetExist){
            showToast("No Courses found for this configuration.")
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
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
        catch (e: Exception) {
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
        }
    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationWithEvent>? {
        log(Logger.Level.INFO, "getAllConfigurationAndEvent called")
        try {
            val configurationHandler = ConfigurationHandler(context)
            val result = configurationHandler.getAllConfigurationAndEvent()
            log(Logger.Level.INFO, "getAllConfigurationAndEvent result: $result")
            return result
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Could not load alarms from database. Try reinstalling the app.")
            return null
        }
    }

    override fun saveRaplaURL(urlString : String){
        log(Logger.Level.INFO, "saveRaplaURL called with urlString: $urlString")
        try {
            if(urlString == "" || isValidCourseURL(urlString)){
                val applicationSettingsHandler = ApplicationSettingsHandler(context)
                val settingsEntity = applicationSettingsHandler.getApplicationSettings()
                settingsEntity.raplaURL = urlString
                applicationSettingsHandler.saveOrUpdateApplicationSettings(settingsEntity)
                log(Logger.Level.INFO, "Rapla URL saved: $urlString")
            }
            else{
                log(Logger.Level.INFO, "Invalid URL provided: $urlString")
                showToast("Invalid URL")
            }
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Error saving URL to database. Try reinstalling the app.")
        }
    }

    override fun saveRaplaURL(director: String, course: String) {
        saveRaplaURL(deriveValidCourseURL(director, course).toString())
    }

    override fun isValidCourseURL(urlString: String): Boolean {
        log(Logger.Level.INFO, "isValidCourseURL called with urlString: $urlString")
        if (!URLUtil.isValidUrl(urlString)) {
            log(Logger.Level.SEVERE, "Invalid URL format: $urlString")
            return false
        }
        try {
            RaplaFetcher(URL(urlString)).throwIfInvalidCourseURL()
        } catch (e: CourseFetcherException) {
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            return false
        }
        log(Logger.Level.INFO, "Valid Course URL: $urlString")
        return true
    }

    override fun isValidCourseURL(director: String, course: String): Boolean {
        return isValidCourseURL(deriveValidCourseURL(director, course).toString())
    }

    override fun getListOfNameOfCourses(): List<String> {
        try {
            val url = getRaplaURL()
            if(url != ""){
                val courseFetcher = RaplaFetcher(
                    raplaUrl = URL(
                        url
                    ),
                    excludedCourseNames = getListOfExcludedCourses().toSet()
                )
                return courseFetcher.getAllCourseNames()
            }
        }
        catch (e : CourseFetcherException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        return emptyList()
    }

    override fun getListOfExcludedCourses(): List<String> {
        log(Logger.Level.INFO, "getListOfExcludedCourses called")
        try {
            val applicationSettingsHandler = ApplicationSettingsHandler(context)
            val excludedCoursesList = applicationSettingsHandler.getApplicationSettings().excludeCourses
            return excludedCoursesList
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Error retrieving URL from database. Try reinstalling the app.")
            return emptyList()
        }
    }

    override fun updateListOfExcludedCourses(excludedCoursesList: List<String>) {
        val applicationSettingsHandler = ApplicationSettingsHandler(context)
        applicationSettingsHandler.updateExcludedCoursesList(excludedCoursesList)
    }

    override fun getDefaultAlarmValues(): SettingsEntity.DefaultAlarmValues {
        try{
            val applicationSettingsHandler = ApplicationSettingsHandler(context)
            return applicationSettingsHandler.getApplicationSettings().defaultValues
        }
        catch (e: PersistenceException.ReadSettingsException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            return SettingsEntity.DefaultAlarmValues()
        }
    }

    override fun updateDefaultAlarmValues(defaultAlarmValues: SettingsEntity.DefaultAlarmValues) {
        try {
            val applicationSettingsHandler = ApplicationSettingsHandler(context)
            applicationSettingsHandler.updateDefaultAlarmValues(defaultAlarmValues)
        }
        catch (e: PersistenceException.UpdateSettingsException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
        }
    }

    override fun validateConfiguration(configuration: Configuration): Boolean {
        log(Logger.Level.INFO, "validateConfiguration called with configuration: $configuration")
        var validation = true
        // Should contain a name
        // Days should not be empty
        if(configuration.days.isEmpty()){
            log(Logger.Level.INFO, "Configuration days are empty")
            validation = false
        }
        if(configuration.startStation != null || configuration.endStation != null){
            // If one station is set but one is null, error
            if(configuration.startStation == null || configuration.endStation == null){
                log(Logger.Level.INFO, "One of the stations is null")
                validation = false
            }
            // Station exists if can be found in List of DB similar station names
            else if (!DBRoutePlanner().deriveValidStationNames(configuration.startStation!!).contains(configuration.startStation!!)
                || !DBRoutePlanner().deriveValidStationNames(configuration.endStation!!).contains(configuration.endStation!!)){
                log(Logger.Level.INFO, "Station names are not valid")
                validation = false
            }
        }
        log(Logger.Level.INFO, "validateConfiguration result: $validation")
        return validation
    }

    override fun getRaplaURL(): String? {
        log(Logger.Level.INFO, "getRaplaURL called")
        try {
            val applicationSettingsHandler = ApplicationSettingsHandler(context)
            val url = applicationSettingsHandler.getApplicationSettings().raplaURL
            log(Logger.Level.INFO, "getRaplaURL result: $url")
            return url
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Error retrieving URL from database. Try reinstalling the app.")
            return null
        }
    }

    override fun isInternetAvailable(): Boolean {
        log(Logger.Level.INFO, "isInternetAvailable called")
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: run {
            log(Logger.Level.INFO, "No active network")
            return false
        }
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: run {
            log(Logger.Level.INFO, "No network capabilities")
            return false
        }
        val result = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        log(Logger.Level.INFO, "isInternetAvailable result: $result")
        return result
    }

    override fun log(
        level: Logger.Level,
        text: String
    ) {
        // Always log to Android logcat for debug as well
        android.util.Log.d("CoreLog", "[$level] $text")
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
        log(Logger.Level.INFO, "getLog called")
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
        log(Logger.Level.INFO, "updateConfigurationActive called with isActive: $isActive, configuration: $configuration")
        try {
            val configurationHandler = ConfigurationHandler(context)
            configurationHandler.updateConfigurationActive(isActive, configuration.uid)
            log(Logger.Level.INFO, "Configuration active state updated")
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Could not update active state of configuration.")
        }
    }

    override fun updateConfigurationIchHabGeringt(
        date: LocalDate,
        uid: Long
    ) {
        log(Logger.Level.INFO, "updateConfigurationIchHabGeringt called with date: $date, configuration: $uid")
        try {
            val configurationHandler = ConfigurationHandler(context)
            configurationHandler.updateConfigurationIchHabGeringt(date, uid)
            log(Logger.Level.INFO, "Configuration active state updated")
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Could not update ichhabgeringt state of configuration.")
        }
    }

    override fun deleteAlarmConfiguration(uid: Long) {
        log(Logger.Level.INFO, "deleteAlarmConfiguration called with uid: $uid")
        try {
            val eventHandler = EventHandler(context)
            val configurationHandler = ConfigurationHandler(context)
            //Always remove all associated events when removing an alarm configuration
            eventHandler.removeEvent(uid)
            configurationHandler.removeAlarmConfiguration(uid)
            log(Logger.Level.INFO, "Alarm configuration and associated events deleted for uid: $uid")
        }
        catch (e: PersistenceException){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
            showToast("Could not delete from database. Try reinstalling the app.")
        }
    }

    override fun isApplicationOpenedFirstTime(): Boolean {
        log(Logger.Level.INFO, "isApplicationOpenedFirstTime called")
        val settings = ApplicationSettingsHandler(context)
        val result = settings.isApplicationOpenedFirstTime()
        log(Logger.Level.INFO, "isApplicationOpenedFirstTime result: $result")
        return result
    }

    override fun showToast(message: String) {
        log(Logger.Level.INFO, "showToast called with message: $message")
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
