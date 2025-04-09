package de.heinzenburger.g2_weckmichmal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.ui.components.WelcomeScreen
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thread {
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
        }

        startActivity(Intent(this, WelcomeScreen::class.java))
    }
    companion object{
        val log: Logger = Logger.getLogger(AlarmConfiguration::class.java.name)
    }
}
