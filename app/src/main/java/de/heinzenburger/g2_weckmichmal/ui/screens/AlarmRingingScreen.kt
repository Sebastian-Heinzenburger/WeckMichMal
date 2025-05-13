package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmRingingScreen.Companion.innerAlarmRingingScreenComposable
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class AlarmRingingScreen : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val id = intent.getLongExtra("Event ID", -1)

        if(id < 0){
            thread {
                var allConfigurationWithEvent = core.getAllConfigurationAndEvent()
                allConfigurationWithEvent?.forEach {
                    var wakeUpDate = it.event?.date?.atTime(it.event.wakeUpTime)
                    var endDate = LocalDateTime.now()
                    it.event?.routes?.forEach {
                            route -> if(route.endTime > endDate){
                        endDate = route.endTime
                    }
                    }
                    if(LocalDateTime.now().isAfter(wakeUpDate) && LocalDateTime.now().isBefore(endDate)){
                        if(it.event != null){
                            event.value = it.event
                            configuration.value = it.configuration
                        }
                    }
                }
            }
        }
        else{
            thread {
                core.getAllConfigurationAndEvent()?.forEach {
                    if(it.event?.configID == id){
                        event.value = it.event
                        configuration.value = it.configuration
                    }
                }
            }
        }

        setContent {
            G2_WeckMichMalTheme {
                val context = LocalContext.current
                BackHandler {
                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    context.startActivity(intent)
                    (context as ComponentActivity).finish()
                }
                AlarmRingingScreenComposable(core)
            }
        }
    }
    companion object{
        var event = mutableStateOf(Event.emptyEvent)
        var configuration = mutableStateOf(Configuration.emptyConfiguration)
        val innerAlarmRingingScreenComposable : @Composable (PaddingValues, I_Core) -> Unit = { innerPadding: PaddingValues, core: I_Core ->
            Column (
                modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()
            )
            {
                Text(
                    text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                    )
                var text = "Mental vorbereiten auf: \n"
                event.value.courses?.forEachIndexed {
                    index, it ->
                    text += it.name + " - " + it.startDate.format(DateTimeFormatter.ofPattern("HH:mm"))
                    if(event.value.courses!!.size-1 != index){
                        text += "\n"
                    }
                }
                Text(
                    text = text,
                    modifier = Modifier
                        .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(20))
                        .fillMaxWidth()
                        .padding(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Column(modifier = Modifier.verticalScroll(rememberScrollState())
                ){
                    event.value.routes?.fastForEachReversed {
                        val bestRoute = it.startTime.minusMinutes(configuration.value.startBuffer.toLong())
                            .format(DateTimeFormatter.ofPattern("HH:mm")) == event.value.wakeUpTime
                            .format(DateTimeFormatter.ofPattern(("HH:mm")))
                        Column(
                            modifier = Modifier
                                .padding(top = 20.dp, start = 40.dp, end = 40.dp)
                                .background(
                                    if(bestRoute){
                                        MaterialTheme.colorScheme.onBackground
                                    }
                                    else {
                                        MaterialTheme.colorScheme.onPrimary
                                    }
                                    ,RoundedCornerShape(20))
                                .fillMaxWidth()
                                .padding(10.dp),
                        ){
                            OurText(
                                text = it.startTime.format(DateTimeFormatter.ofPattern("HH:mm ")) + it.startStation,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            for(i in 0..1){
                                i
                                Text(
                                    text = "|",
                                    color =
                                        if(bestRoute){
                                            MaterialTheme.colorScheme.secondary
                                        }else{
                                            MaterialTheme.colorScheme.onBackground
                                        },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OurText(
                                text = it.endTime.format(DateTimeFormatter.ofPattern("HH:mm ")) + it.endStation,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmRingingScreenComposable(core: I_Core) {
    innerAlarmRingingScreenComposable(PaddingValues(), core)
}

@Preview(showBackground = true)
@Composable
fun AlarmRingingScreenPreview() {
    val core = MockupCore()
    AlarmRingingScreen.event.value = MockupCore.mockupEvents[0]
    AlarmRingingScreen.configuration.value = MockupCore.mockupConfigurations[0]
    G2_WeckMichMalTheme {
        AlarmRingingScreenComposable(core = core)
    }
}