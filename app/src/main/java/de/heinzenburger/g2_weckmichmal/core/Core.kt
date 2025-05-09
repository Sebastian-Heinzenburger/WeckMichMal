package de.heinzenburger.g2_weckmichmal.core

import android.content.Context
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import de.heinzenburger.g2_weckmichmal.api.db.RoutePlanner
import de.heinzenburger.g2_weckmichmal.api.rapla.CoursesFetcher
import de.heinzenburger.g2_weckmichmal.calculation.WakeUpCalculator
import de.heinzenburger.g2_weckmichmal.persistence.ConfigurationHandler
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettingsHandler
import de.heinzenburger.g2_weckmichmal.persistence.EventHandler
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.CourseFetcherException
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.specifications.WakeUpCalculationException
import java.net.URL

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

    override fun generateOrUpdateAlarmConfiguration(configuration: Configuration) {
        val configurationHandler = ConfigurationHandler(context)
        try {

        }
        catch (e: WakeUpCalculationException){
            when(e.reason){
                WakeUpCalculationException.Reason.ConnectionError -> logger.log(Logger.Level.SEVERE, e.message)
                WakeUpCalculationException.Reason.ParserError -> logger.log(Logger.Level.SEVERE, e.message)
            }
        }
        if (validateConfigurationEntity(configuration)){
            val eventHandler = EventHandler(context)
            configurationHandler.saveOrUpdate(configuration)
            val event = WakeUpCalculator(
                routePlanner = RoutePlanner(),
                courseFetcher = CoursesFetcher(URL(getRaplaURL()))
            ).calculateNextEvent(configuration)
            eventHandler.saveOrUpdate(event)

            configuration.log()
            event.log()
        }
        else{
            showToast("Configuration is not valid")
        }
    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationWithEvent>? {
        val configurationHandler = ConfigurationHandler(context)
        return configurationHandler.getAllConfigurationAndEvent()
    }

    override fun saveRaplaURL(urlString : String){
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

    override fun isValidCourseURL(urlString : String) : Boolean{
        return  URLUtil.isValidUrl(urlString) && CoursesFetcher(URL(urlString)).hasValidCourseURL()
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
        val applicationSettingsHandler = ApplicationSettingsHandler(context)
        return applicationSettingsHandler.getApplicationSettings().raplaURL
    }

    override fun log(
        level: Logger.Level,
        text: String
    ) {
        logger.log(level, text)
    }

    override fun getLog(): String {
        return logger.getLogs()
    }

    override fun updateConfigurationActive(
        isActive: Boolean,
        configuration: Configuration
    ) {
        val configurationHandler = ConfigurationHandler(context)
        configurationHandler.updateConfigurationActive(isActive, configuration.uid)
    }

    override fun deleteAlarmConfiguration(uid: Long) {
        val eventHandler = EventHandler(context)
        val configurationHandler = ConfigurationHandler(context)
        //Always remove all associated events when removing an alarm configuration
        eventHandler.removeEvent(uid)
        configurationHandler.removeAlarmConfiguration(uid)
    }

    override fun isApplicationOpenedFirstTime(): Boolean {
        val settings = ApplicationSettingsHandler(context)
        return settings.isApplicationOpenedFirstTime()
    }

    override fun showToast(message: String) {
        ContextCompat.getMainExecutor(context).execute( { ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        )
    }
}

