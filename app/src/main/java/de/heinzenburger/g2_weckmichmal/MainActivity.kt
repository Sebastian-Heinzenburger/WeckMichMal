package de.heinzenburger.g2_weckmichmal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration.AppDatabase
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
        enableEdgeToEdge()
        setContent {
            G2_WeckMichMalTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        thread {
            //Lazy Testing Persistence Functionality
            val alarmConfiguration = AlarmConfiguration(this)

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
            alarmConfiguration.getAlarmConfiguration(testConfiguration.uid).log()
            testConfiguration.days = "1110000"
            alarmConfiguration.saveOrUpdate(testConfiguration)
            alarmConfiguration.getAlarmConfiguration(testConfiguration.uid).log()
            for(config in alarmConfiguration.getAllAlarmConfigurations()){
                config.log()
            }
            alarmConfiguration.removeAlarmConfiguration(testConfiguration.uid)
            for(config in alarmConfiguration.getAllAlarmConfigurations()){
                config.log()
            }
        }
    }
    companion object{
        val log: Logger = Logger.getLogger(AlarmConfiguration::class.java.name)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    G2_WeckMichMalTheme {
        Greeting("Android")
    }
}