package de.heinzenburger.g2_weckmichmal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.NumberField
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurTextField
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockEditScreen
import de.heinzenburger.g2_weckmichmal.ui.screens.EditComposable
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.util.Calendar
import kotlin.concurrent.thread

class PickerDialogs {
    companion object{
        //Dialogs for Picking LocalTime and Minutes
        @Composable
        fun MinutePickerDialog(
            onConfirm: (Int) -> Unit,
            onDismiss: () -> Unit,
            default: Int
        ) {
            var selectedNumber = remember { mutableIntStateOf(default) }
            Dialog(onDismissRequest = { onDismiss() }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(375.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Zeit in Minuten:",
                            modifier = Modifier.padding(16.dp),
                        )

                        Box(
                            Modifier.fillMaxWidth(0.5f)
                        ){
                            NumberField(
                                text = selectedNumber,
                                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.2f)
                            )
                            Column(
                                Modifier.align(BiasAlignment(1f,0f))
                            )
                            {
                                IconButton(
                                    onClick = {selectedNumber.intValue++},
                                    modifier = Modifier.fillMaxHeight(0.1f)
                                ) {
                                    Icon(Icons.Filled.ArrowDropUp,
                                        contentDescription = "Add 1",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                IconButton(
                                    onClick = {selectedNumber.intValue--},
                                    modifier = Modifier.fillMaxHeight(0.12f)
                                )
                                {
                                    Icon(Icons.Filled.ArrowDropDown,
                                        contentDescription = "Substract 1",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextButton(
                                onClick = { onDismiss() },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(
                                onClick = {
                                    onConfirm(selectedNumber.intValue)
                                          },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("Confirm", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun StationPickerDialog(
            onConfirm: (String) -> Unit,
            onDismiss: () -> Unit,
            core: CoreSpecification
        ) {
            Dialog(onDismissRequest = { onDismiss() }) {
                var station = remember { mutableStateOf("") }
                var stationPrediction = remember { mutableStateOf("") }
                var stationPredictions = remember { mutableStateOf(listOf("--")) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(375.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OurTextField(
                            value = station.value,
                            onValueChange = {
                                station.value = it
                                thread{
                                    if(station.value.length > 2){
                                        try {
                                            stationPredictions.value = core.deriveStationName(it)
                                        }
                                        catch (e: Exception){
                                            core.log(Logger.Level.SEVERE, e.message.toString())
                                            core.log(Logger.Level.SEVERE, e.stackTraceToString())
                                            core.showToast("Da hat etwas nicht geklappt")
                                        }
                                    }
                                } },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            placeholderText = "Start eingeben"
                        )
                        Text(
                            text = "Vorschläge:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )

                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ){
                            stationPredictions.value.forEachIndexed {
                                    index, it ->
                                Button(
                                    onClick = {
                                        stationPrediction.value = it
                                        if(stationPrediction.value != "" && stationPrediction.value != "--"){
                                            onConfirm(stationPrediction.value)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                ){
                                    OurText(
                                        text = it,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun TimePickerDialogContainer(
            onConfirm: (TimePickerState) -> Unit,
            onDismiss: () -> Unit,
        ) {
            val currentTime = Calendar.getInstance()

            val timePickerState = rememberTimePickerState(
                initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
                initialMinute = currentTime.get(Calendar.MINUTE),
                is24Hour = true,
            )

            AlertDialog(
                containerColor = MaterialTheme.colorScheme.background,
                onDismissRequest = onDismiss,
                dismissButton = {
                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text("Abbrechen", color = MaterialTheme.colorScheme.secondary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm(timePickerState) }) {
                        Text("Bestätigen", color = MaterialTheme.colorScheme.secondary)
                    }
                },
                text = { TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.onBackground,
                        selectorColor = MaterialTheme.colorScheme.secondary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.primary,
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.onBackground,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.onBackground,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) }
            )
        }
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun ConfirmDialog(
            onConfirm: () -> Unit,
            onDismiss: () -> Unit,
        ) {
            AlertDialog(
                containerColor = MaterialTheme.colorScheme.background,
                onDismissRequest = onDismiss,
                dismissButton = {
                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text("Abbrechen", color = MaterialTheme.colorScheme.secondary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm() }) {
                        Text("Bestätigen", color = MaterialTheme.colorScheme.secondary)
                    }
                },
                text = {
                    OurText(
                        text = "Bist du sicher, dass du diese Daten mit den Entwicklern teilen möchtest? Sie könnten sensible Daten enthalten, z.B. deine Startstation, deine Kurse und Aufwachzeiten. Es werden keine Daten weitergegeben, jedoch kann kein vollständiger Schutz vor unbefugtem Zugriff gewährt werden.",
                        modifier = Modifier
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditPreview() {
    AlarmClockEditScreen.reset(null)
    G2_WeckMichMalTheme {
        EditComposable(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}