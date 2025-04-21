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
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity
import de.heinzenburger.g2_weckmichmal.ui.components.UIActions
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
        UIActions(context = applicationContext).setWelcomeScreen()
    }
    companion object{
        val log: Logger = Logger.getLogger(AlarmConfiguration::class.java.name)
    }
}

data class Core(
    val context: Context
){
    fun saveRaplaURL(url : String){
        val applicationSettings = ApplicationSettings(context)
        val settingsEntity = applicationSettings.getApplicationSettings()
        settingsEntity.raplaURL = url
        if(!applicationSettings.saveOrUpdateApplicationSettings(settingsEntity)){
            Toast.makeText(context, "Etwas ist schiefgelaufen (saveRaplaURL)", Toast.LENGTH_LONG).show()
        }
    }
}