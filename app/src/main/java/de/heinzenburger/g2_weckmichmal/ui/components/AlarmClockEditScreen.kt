package de.heinzenburger.g2_weckmichmal.ui.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.EventEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlin.concurrent.thread

class AlarmClockEditScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        setContent {
            G2_WeckMichMalTheme {
                EditComposable(modifier = Modifier, core)
            }
        }
    }
    companion object{
        var configurationEntity = ConfigurationEntity(
            name = "Wecker 1",
            days = setOf(),
            fixedArrivalTime = null,
            fixedTravelBuffer = null,
            startBuffer = 30,
            endBuffer = 10,
            startStation = "",
            endStation = "",
            isActive = true
        )
        val innerEditComposable : @Composable (PaddingValues, I_Core) -> Unit =
        { innerPadding: PaddingValues, core: I_Core ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Wecker bearbeiten",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = {
                            thread{
                                core.saveOrUpdateAlarmConfiguration(configurationEntity)
                                core.saveOrUpdateEvent(EventEntity(
                                    configID = configurationEntity.uid,
                                    wakeUpTime = LocalTime.NOON,
                                    days = configurationEntity.days,
                                    date = LocalDate.now(),
                                    courses = EventEntity.emptyEvent.courses,
                                    routes = EventEntity.emptyEvent.routes
                                ))
                                core.setAlarmClockOverviewScreen()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = "Speichern",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                TextField(
                    shape = RoundedCornerShape(8.dp),
                    value = configurationEntity.name,
                    onValueChange = {configurationEntity.name = it},
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(24.dp, 8.dp).align(alignment = Alignment.CenterHorizontally)
                )
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = "Datengrundlage",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

@Composable
fun EditComposable(modifier: Modifier, core: I_Core) {
    NavBar.NavigationBar(modifier, core, AlarmClockEditScreen.innerEditComposable, caller = AlarmClockEditScreen::class)
}



@Preview(showBackground = true)
@Composable
fun EditPreview() {
    G2_WeckMichMalTheme {
        EditComposable(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}