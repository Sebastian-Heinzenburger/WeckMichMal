package de.heinzenburger.g2_weckmichmal.ui.components

import android.util.Range
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import java.util.Calendar
import kotlin.concurrent.thread

class PickerDialogs {
    companion object{
        //Dialogs for Picking LocalTime and Minutes
        @Composable
        fun MinutePickerDialog(
            onConfirm: (Int) -> Unit,
            onDismiss: () -> Unit,
            range: Range<Int>,
            default: Int
        ) {
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

                        var sliderPosition = remember { mutableFloatStateOf(default.toFloat()) }
                        Column {
                            Slider(
                                valueRange = range.lower.toFloat() .. range.upper.toFloat(),
                                value = sliderPosition.floatValue,
                                onValueChange = { sliderPosition.floatValue = it }
                            )
                            Text(text = sliderPosition.floatValue.toInt().toString())
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
                                onClick = { onConfirm((sliderPosition.floatValue).toInt()) },
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
            core: I_Core
        ) {
            Dialog(onDismissRequest = { onDismiss() }) {
                var station = remember { mutableStateOf("") }
                var stationPrediction = remember { mutableStateOf("") }
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
                            text = "Stationsname:",
                            modifier = Modifier.padding(16.dp),
                        )
                        Text(
                            text = stationPrediction.value
                        )
                        TextField(
                            shape = RoundedCornerShape(8.dp),
                            value = station.value,
                            onValueChange = {
                                station.value = it
                                thread{
                                    if(station.value.length > 2){
                                        stationPrediction.value = core.deriveStationName(it)[0]
                                    }
                                } },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .padding(24.dp, 8.dp)
                                .align(alignment = Alignment.CenterHorizontally)
                        )


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
                                onClick = { onConfirm(stationPrediction.value) },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("Confirm", color = MaterialTheme.colorScheme.primary)
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
    }
}