package de.heinzenburger.g2_weckmichmal.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import de.heinzenburger.g2_weckmichmal.api.courses.RaplaFetcher
import de.heinzenburger.g2_weckmichmal.api.routes.DBRoutePlanner
import de.heinzenburger.g2_weckmichmal.background.AlarmEvent
import de.heinzenburger.g2_weckmichmal.background.AlarmUpdater
import de.heinzenburger.g2_weckmichmal.calculation.WakeUpCalculator
import de.heinzenburger.g2_weckmichmal.persistence.EventHandler
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import de.heinzenburger.g2_weckmichmal.specifications.WakeUpCalculatorException
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.forEach

class BackgroundOperations(var core: Core) {
    fun calculateNextEventForConfiguration(it: ConfigurationWithEvent, wakeUpCalculator: WakeUpCalculator) : ConfigurationWithEvent{
        core.log(Logger.Level.INFO, "Processing configurationWithEvent with index ${it.configuration.uid}: $it")
        //Skip updating configuration if inactive
        if(!it.configuration.isActive){
            core.log(Logger.Level.INFO, "Configuration with index ${it.configuration.uid} is inactive, skipping.")
            return it
        }

        var configurationWithEvent = it
        try {
            core.log(Logger.Level.INFO, "Updating Event. Before:")
            it.event?.log(core)
            configurationWithEvent = ConfigurationWithEvent(
                it.configuration,
                wakeUpCalculator.calculateNextEvent(it.configuration, strict = true)
            )
            core.log(Logger.Level.INFO, "Updating Event. After:")
            configurationWithEvent.event?.log(core)
        }
        catch (e: WakeUpCalculatorException.NoRoutesFound){
            core.log(Logger.Level.INFO, "NoRoutesFound exception caught for configuration with index ${it.configuration.uid}")
            if(it.configuration.enforceStartBuffer){
                try {
                    configurationWithEvent = ConfigurationWithEvent(
                        it.configuration,
                        wakeUpCalculator.calculateNextEvent(it.configuration, strict = false)
                    )
                    core.log(Logger.Level.SEVERE, "No Routes found and enforcing start buffer.")
                }
                catch (e: Exception){
                    configurationWithEvent = it
                    core.log(Logger.Level.SEVERE, "No Routes found and enforcing start buffer but another exception.")
                    core.log(Logger.Level.SEVERE, e.message.toString())
                    core.log(Logger.Level.SEVERE, e.stackTraceToString())
                }
            }
            else{
                core.log(Logger.Level.SEVERE, "No Routes found and not enforcing start buffer. Wake up now!")
                core.log(Logger.Level.SEVERE, e.message.toString())
                core.log(Logger.Level.SEVERE, e.stackTraceToString())
                if(it.event != null){
                    configurationWithEvent = it
                    configurationWithEvent.event.wakeUpTime = LocalTime.now()
                }
            }
        }
        //If course suddenly disappeared -> No school? -> Do not wake up and delete Event
        catch (e: WakeUpCalculatorException.EventDoesNotYetExist){
            core.log(Logger.Level.INFO, "NoCoursesFound exception caught for configuration with index ${it.configuration.uid}")
            configurationWithEvent = ConfigurationWithEvent(
                it.configuration, null
            )
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        //Course URL suddenly not valid anymore? -> Continue with previously assumed wake up time
        catch (e: WakeUpCalculatorException.CoursesInvalidDataFormatError){
            core.log(Logger.Level.INFO, "CoursesInvalidDataFormatError exception caught for configuration with index ${it.configuration.uid}")
            configurationWithEvent = it
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        //Internet connection error -> Continue with previously assumed wake up time
        catch (e: WakeUpCalculatorException.CoursesConnectionError){
            core.log(Logger.Level.INFO, "CoursesConnectionError exception caught for configuration with index ${it.configuration.uid}")
            configurationWithEvent = it
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        //Something bad happened -> Continue with previously assumed wake up time
        catch (e: Exception){
            core.log(Logger.Level.INFO, "Generic exception caught for configuration with index ${it.configuration.uid}: ${e.message}")
            configurationWithEvent = it
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        return configurationWithEvent
    }
    
    fun scheduleNextAction(configurationsWithEvents: List<ConfigurationWithEvent>?){
        var earliestEventDate = LocalDateTime.MAX
        var earliestConfigurationWithEvent: ConfigurationWithEvent? = null
        configurationsWithEvents?.forEach {
            val eventDateTime = it.event?.getLocalDateTime()
            core.log(Logger.Level.INFO, "Checking eventDateTime: $eventDateTime for configuration: ${it.configuration}")
            if(it.configuration.isActive
                && it.configuration.ichHabGeringt.isBefore(LocalDate.now())
                && eventDateTime?.isBefore(earliestEventDate) == true){

                earliestEventDate = eventDateTime
                earliestConfigurationWithEvent = it
                core.log(Logger.Level.INFO, "New earliestEventDate: $earliestEventDate, earliestEvent: $earliestConfigurationWithEvent")
            }
        }
        core.log(Logger.Level.INFO, "Earliest Event is at " + earliestEventDate.format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")))
        val secondsUntilEarliestEvent = Duration.between(LocalDateTime.now(), earliestEventDate).seconds
        core.log(Logger.Level.INFO, "Seconds until earliest event: $secondsUntilEarliestEvent")
        if (secondsUntilEarliestEvent > 28800 // 8 hours
        ) {
            core.log(Logger.Level.INFO, "Scheduling update in 7 hours")
            startUpdateScheduler(25200) // 7 hours
        }else if (secondsUntilEarliestEvent > 14400 //4 hours
        ) {
            core.log(Logger.Level.INFO, "Scheduling update in 3.5 hours")
            startUpdateScheduler(12600) // 3.5 hours
        }
        else if (secondsUntilEarliestEvent > 3600 // 1 hour
        ) {
            core.log(Logger.Level.INFO, "Scheduling update in 30 minutes")
            startUpdateScheduler(1800) // 30 minutes
        }
        else if (secondsUntilEarliestEvent > 1800 // 30 minutes
        ) {
            core.log(Logger.Level.INFO, "Scheduling update in 15 minutes")
            startUpdateScheduler(900) // 15 minutes
        }
        else if (secondsUntilEarliestEvent > 600 // 10 minutes
        ) {
            core.log(Logger.Level.INFO, "Scheduling update in 4 minutes")
            startUpdateScheduler(240) // 4 minutes
        } else {
            core.log(Logger.Level.INFO, "Running wake up logic for earliestEvent: $earliestConfigurationWithEvent")
            runWakeUpLogic(earliestConfigurationWithEvent!!)
        }
    }
    
    fun runUpdateLogic() {
        core.log(Logger.Level.INFO, "runUpdateLogic started")

        var configurationsWithEvents = core.getAllConfigurationAndEvent()?.toMutableList()
        if(configurationsWithEvents == null ||configurationsWithEvents.isEmpty()){
            return
        }
        core.log(Logger.Level.INFO, "ConfigurationsWithEvents loaded: $configurationsWithEvents")

        core.log(Logger.Level.INFO, "Starting safety alarm scheduler")
        scheduleNextAction(configurationsWithEvents)


        core.log(Logger.Level.INFO, "Starting regular alarm scheduler")
        val url = core.getRaplaURL()
        core.log(Logger.Level.INFO, "Rapla URL: $url")
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
                excludedCourseNames = core.getListOfExcludedCourses()!!.toSet()
            )
        )


        configurationsWithEvents.forEachIndexed {
                index, it ->
            var configurationWithEvent = calculateNextEventForConfiguration(it, wakeUpCalculator)
            configurationsWithEvents[index] = configurationWithEvent
            val eventHandler = EventHandler(core.context)
            //Save updated event to database
            if(configurationWithEvent.event != null){
                try {
                    core.log(Logger.Level.INFO, "Saving or updating event: ${configurationWithEvent.event}")
                    eventHandler.saveOrUpdate(configurationWithEvent.event)
                }
                catch (e: PersistenceException){
                    core.log(Logger.Level.SEVERE, e.message.toString())
                    core.log(Logger.Level.SEVERE, e.stackTraceToString())
                }
            }
        }

        scheduleNextAction(configurationsWithEvents)
    }

    fun runWakeUpLogic(earliestConfigurationWithEvent: ConfigurationWithEvent) {
        core.log(Logger.Level.INFO, "runWakeUpLogic called with earliestEvent: $earliestConfigurationWithEvent")
        val alarmManager = core.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(core.context, AlarmEvent::class.java)
        alarmIntent.putExtra("configurationWithEvent", earliestConfigurationWithEvent)
        val pendingIntent = PendingIntent.getBroadcast(
            core.context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pendingEditIntent = PendingIntent.getBroadcast(
            core.context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (!alarmManager.canScheduleExactAlarms()) {
            core.log(Logger.Level.INFO, "Requesting permission to schedule exact alarms")
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            core.context.startActivity(intent)
        }
        val alarmDate = earliestConfigurationWithEvent.event!!.getLocalDateTime()
        core.log(Logger.Level.INFO, "Setting alarm for date: $alarmDate")

        val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingEditIntent)
        core.log(Logger.Level.INFO, "Alarm ringing at ${earliestConfigurationWithEvent.event.wakeUpTime}!")
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        core.logNextAlarm(alarmDate, "Wake")
    }
    
    fun startUpdateScheduler(delay: Int) {
        core.log(Logger.Level.INFO, "startUpdateScheduler called with delay: $delay")
        val alarmManager = core.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(core.context, AlarmUpdater::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            core.context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pendingEditIntent = PendingIntent.getBroadcast(
            core.context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (!alarmManager.canScheduleExactAlarms()) {
            core.log(Logger.Level.INFO, "Requesting permission to schedule exact alarms for update scheduler")
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            core.context.startActivity(intent)
        }
        val triggerTime = LocalDateTime.now().plusSeconds(delay.toLong())
        core.log(Logger.Level.INFO, "Scheduling update alarm for: $triggerTime")

        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingEditIntent)

        core.log(Logger.Level.INFO, "Alarm updating in ${delay/60} minutes")
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        core.logNextAlarm(triggerTime, "Update")
    }
}