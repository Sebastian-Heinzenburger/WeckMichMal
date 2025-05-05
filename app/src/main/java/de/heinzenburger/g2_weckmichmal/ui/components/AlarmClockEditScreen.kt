package de.heinzenburger.g2_weckmichmal.ui.components

import android.os.Bundle
import android.util.Range
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationEntity
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
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
        private var openArrivalTimePickerDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openTravelTimePickerDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openStartBufferPickerDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openEndBufferPickerDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openStartStationDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openEndStationDialog : MutableState<Boolean> = mutableStateOf(false)

        //These are static. They are storing all necessary information for an alarm configuration
        //Everytime an alarm is created, the reset function has to be called before calling this view so all configurations are set to default
        //If an alarm is to be updated, the current configuration is passed as parameter to reset function
        private lateinit var alarmName : MutableState<String>
        private lateinit var manuallySetArrivalTime: MutableState<LocalTime> //Set if arrival time shouldnt be dependent on lecture plan
        private lateinit var isManualArrivalTime : MutableState<Boolean>
        private lateinit var manuallySetTravelTime: MutableIntState //Set if travel time shouldnt be dependent on Deutsche Bahn
        private lateinit var isManualTravelTime : MutableState<Boolean>
        private lateinit var setStartBufferTime: MutableIntState //Set if arrival time shouldnt be dependent on lecture plan
        private lateinit var setEndBufferTime: MutableIntState //Time between arrival and lecture start
        private lateinit var startStation: MutableState<String>
        private lateinit var endStation: MutableState<String>
        private lateinit var selectedDays: MutableState<List<Boolean>> //All days where this alarm applies to
        private lateinit var configurationEntity : ConfigurationEntity //The configuration stored in the database

        fun reset(configurationEntity: ConfigurationEntity?){
            if(configurationEntity == null){
                //All default values
                alarmName = mutableStateOf("Wecker 1")
                manuallySetArrivalTime = mutableStateOf(LocalTime.NOON)
                manuallySetTravelTime = mutableIntStateOf(15)
                setStartBufferTime = mutableIntStateOf(30)
                setEndBufferTime = mutableIntStateOf(5)
                startStation = mutableStateOf("Startbahnhof")
                endStation = mutableStateOf("Endbahnhof")
                selectedDays = mutableStateOf(listOf(true,true,true,true,true,false,false))
                isManualArrivalTime = mutableStateOf(false)
                isManualTravelTime = mutableStateOf(false)
                AlarmClockEditScreen.configurationEntity = ConfigurationEntity(
                    name = "Wecker 1",
                    days = setOf(),
                    fixedArrivalTime = null,
                    fixedTravelBuffer = null,
                    startBuffer = 30,
                    endBuffer = 10,
                    startStation = null,
                    endStation = null,
                    isActive = true
                )
            }
            else{
                //Set Alarm Name
                alarmName = mutableStateOf(configurationEntity.name)

                //BufferTimes
                setStartBufferTime = mutableIntStateOf(configurationEntity.startBuffer)
                setEndBufferTime = mutableIntStateOf(configurationEntity.endBuffer)

                //Convert Day of Week to Boolean List
                var days = mutableListOf<Boolean>()
                DayOfWeek.entries.forEach { day ->
                    days.add(configurationEntity.days.contains(day))
                }
                selectedDays = mutableStateOf(days)

                //Manual set Arrival Time
                isManualArrivalTime = mutableStateOf(configurationEntity.fixedArrivalTime != null)
                manuallySetArrivalTime = if(isManualArrivalTime.value){
                    mutableStateOf(configurationEntity.fixedArrivalTime!!)
                } else{
                    mutableStateOf(LocalTime.NOON)
                }

                //Manual set Travel Time
                isManualTravelTime = mutableStateOf(configurationEntity.fixedTravelBuffer != null)
                manuallySetTravelTime = if(isManualTravelTime.value){
                    mutableIntStateOf(configurationEntity.fixedTravelBuffer!!)
                } else{
                    mutableIntStateOf(15)
                }
                //Stations if needed
                if(isManualTravelTime.value){
                    startStation = mutableStateOf("Startbahnhof")
                    endStation = mutableStateOf("Endbahnhof")
                }
                else{
                    startStation = mutableStateOf(configurationEntity.startStation!!)
                    endStation = mutableStateOf(configurationEntity.endStation!!)
                }

                AlarmClockEditScreen.configurationEntity = ConfigurationEntity(
                    uid = configurationEntity.uid, //Use the same uid as the configuration that is changed
                    name = "Wecker 1",
                    days = setOf(),
                    fixedArrivalTime = null,
                    fixedTravelBuffer = null,
                    startBuffer = 30,
                    endBuffer = 10,
                    startStation = null,
                    endStation = null,
                    isActive = true
                )
            }
        }

        //All components are stored as variables so they can be used as callbacks if needed
        private val innerDatengrundlageComposable : @Composable (PaddingValues, I_Core) -> Unit =
        { innerPadding: PaddingValues, core: I_Core ->
            val arrivalOptions = listOf("Manuelle Ankuftszeit", "Ankuftszeit nach Vorlesungsplan")
            //Dont really know what this is doing
            val (arrivalSelectedOption, onArrivalOptionSelected) = remember {
                if(isManualArrivalTime.value){
                    mutableStateOf(arrivalOptions[0])
                }
                else{
                    mutableStateOf(arrivalOptions[1])
                }
            }

            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = "Datengrundlage",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 24.dp, start = 24.dp)
            )
            Row{
                Column(
                    Modifier
                        .fillMaxWidth()
                        .selectableGroup() //All radio buttons in this column correspond to one group
                ) {
                    Row(
                        Modifier
                            .selectable(
                                selected = (arrivalOptions[0] == arrivalSelectedOption),
                                onClick = {
                                    onArrivalOptionSelected(arrivalOptions[0])
                                    isManualArrivalTime.value = true
                                          },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (arrivalOptions[0] == arrivalSelectedOption),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = arrivalOptions[0],
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                        TextButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp),
                            enabled = arrivalSelectedOption == arrivalOptions[0],
                            onClick = {
                                openArrivalTimePickerDialog.value = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                text = manuallySetArrivalTime.value.format(
                                    DateTimeFormatter.ofPattern(
                                        "HH:mm"
                                    )
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.padding(0.dp)
                            )
                        }
                    }
                    Row(
                        Modifier
                            .selectable(
                                selected = (arrivalOptions[1] == arrivalSelectedOption),
                                onClick = {
                                    onArrivalOptionSelected(arrivalOptions[1])
                                    isManualArrivalTime.value = false
                                          },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (arrivalOptions[1] == arrivalSelectedOption),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = arrivalOptions[1],
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                    }
                }

            }
        }
        private val innerFahrtwegComposable : @Composable (PaddingValues, I_Core) -> Unit =
            { innerPadding: PaddingValues, core: I_Core ->
                val rideOptions = listOf("Bahnverbindung", "Manuelle Fahrtzeit")
                val (rideSelectedOption, onRideOptionSelected) = remember {
                    if(isManualTravelTime.value){
                        mutableStateOf(rideOptions[1])
                    }
                    else{
                        mutableStateOf(rideOptions[0])
                    }
                }

                Column (
                    modifier = Modifier.selectableGroup()
                ){
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Fahrtweg",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                    )
                    Row{
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .selectableGroup()
                        ) {
                            Row(
                                Modifier
                                    .selectable(
                                        selected = (rideOptions[0] == rideSelectedOption),
                                        onClick = {
                                            onRideOptionSelected(rideOptions[0])
                                            isManualTravelTime.value = false
                                                  },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (rideOptions[0] == rideSelectedOption),
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    text = rideOptions[0],
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                )
                            }
                            Column{

                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 48.dp),
                                    onClick = {
                                        openStartStationDialog.value = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(0.dp)

                                ) {
                                    Text(
                                        style = MaterialTheme.typography.bodyMedium,
                                        text = startStation.value,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.background,
                                        modifier = Modifier.padding(0.dp)
                                    )
                                }
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 48.dp),
                                    onClick = {
                                        openEndStationDialog.value = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(0.dp)

                                ) {
                                    Text(
                                        style = MaterialTheme.typography.bodyMedium,
                                        text = endStation.value,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.background,
                                        modifier = Modifier.padding(0.dp)
                                    )
                                }
                            }

                            Row(
                                Modifier
                                    .selectable(
                                        selected = (rideOptions[1] == rideSelectedOption),
                                        onClick = {
                                            onRideOptionSelected(rideOptions[1])
                                            isManualTravelTime.value = true
                                                  },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (rideOptions[1] == rideSelectedOption),
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    text = rideOptions[1],
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                )
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 48.dp),
                                    onClick = {
                                        openTravelTimePickerDialog.value = true
                                    },
                                    enabled = rideSelectedOption == rideOptions[1],
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        style = MaterialTheme.typography.bodyMedium,
                                        text = manuallySetTravelTime.intValue.toString() + "min",
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.background,
                                        modifier = Modifier.padding(0.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        private val innerZeitaufwandComposable : @Composable (PaddingValues, I_Core) -> Unit =
            { innerPadding: PaddingValues, core: I_Core ->
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = "Zeitaufwand",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                )
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Puffer vor Fahrt",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp),
                        onClick = {
                            openStartBufferPickerDialog.value = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(0.dp)

                    ) {
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = setStartBufferTime.intValue.toString() + "min",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.padding(0.dp)
                        )
                    }

                }
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp)

                ){
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Puffer nach Ankunft",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp),
                        onClick = {
                            openEndBufferPickerDialog.value = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = setEndBufferTime.intValue.toString() + "min",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }
            }
        private val innerGueltigkeitComposable : @Composable (PaddingValues, I_Core) -> Unit =
            { innerPadding: PaddingValues, core: I_Core ->
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = "Gültig für",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ){
                    selectedDays.value.forEachIndexed { index, active ->

                        TextButton(
                            modifier = Modifier
                                .padding(start = 0.dp)
                                .width(50.dp)
                                .align(
                                    BiasAlignment(
                                        (2 * (index / 6f) - 1),
                                        verticalBias = 0f
                                    )
                                ),
                            onClick = {
                                var days = selectedDays.value.toMutableList()
                                days[index] = !days[index]
                                selectedDays.value = days
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            val day = DayOfWeek.entries[index].name
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                text = day[0]+day[1].lowercase(),
                                textAlign = TextAlign.Center,
                                color = if(active){
                                    MaterialTheme.colorScheme.secondary
                                } else{
                                    MaterialTheme.colorScheme.onBackground
                                },
                                modifier = Modifier.padding(0.dp)
                            )
                        }
                    }
                }
            }

        //Main component that is sent as navbar as callback
        @OptIn(ExperimentalMaterial3Api::class) //Needed because Time Picker is yet experimental
        val innerEditComposable : @Composable (PaddingValues, I_Core) -> Unit =
        { innerPadding: PaddingValues, core: I_Core ->
            //Open time picker dialogs when corresponding boolean set to true
            when {
                openArrivalTimePickerDialog.value -> {
                    DialWithDialogExample(
                        onConfirm =
                        { timePickerState: TimePickerState ->
                            openArrivalTimePickerDialog.value = false
                            manuallySetArrivalTime.value = LocalTime.of(
                                timePickerState.hour, timePickerState.minute
                            )
                        },
                        onDismiss = {
                            openArrivalTimePickerDialog.value = false
                        })
                }
            }
            when {
                openTravelTimePickerDialog.value -> {
                    MinutePickerDialog(
                        onConfirm =
                            { minutes: Int ->
                                openTravelTimePickerDialog.value = false
                                manuallySetTravelTime.intValue = minutes
                            },
                        onDismiss = {
                            openTravelTimePickerDialog.value = false
                        },
                        range = Range(0,120),
                        default = manuallySetTravelTime.intValue
                    )
                }
            }
            when {
                openStartBufferPickerDialog.value -> {
                    MinutePickerDialog(
                        onConfirm =
                            { minutes: Int ->
                                openStartBufferPickerDialog.value = false
                                setStartBufferTime.intValue = minutes
                            },
                        onDismiss = {
                            openStartBufferPickerDialog.value = false
                        },
                        range = Range(0,120),
                        default = setStartBufferTime.intValue
                    )
                }
            }
            when {
                openEndBufferPickerDialog.value -> {
                    MinutePickerDialog(
                        onConfirm =
                            { minutes: Int ->
                                openEndBufferPickerDialog.value = false
                                setEndBufferTime.intValue = minutes
                            },
                        onDismiss = {
                            openTravelTimePickerDialog.value = false
                        },
                        range = Range(-120,60),
                        default = setEndBufferTime.intValue
                    )
                }
            }
            when {
                openStartStationDialog.value -> {
                    StationPickerDialog(
                        onConfirm =
                            { station: String ->
                                openStartStationDialog.value = false
                                startStation.value = station
                            },
                        onDismiss = {
                            openStartStationDialog.value = false
                        },
                        core
                    )
                }
            }
            when {
                openEndStationDialog.value -> {
                    StationPickerDialog(
                        onConfirm =
                            { station: String ->
                                openEndStationDialog.value = false
                                endStation.value = station
                            },
                        onDismiss = {
                            openEndStationDialog.value = false
                        },
                        core
                    )
                }
            }
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
                        //Save to database when clicked
                        onClick = {
                            thread{
                                //Name of Alarm
                                configurationEntity.name = alarmName.value
                                //fixed arrival time if selected, else null
                                if(isManualArrivalTime.value){
                                    configurationEntity.fixedArrivalTime = manuallySetArrivalTime.value
                                }
                                //fixed travel time if selected, else null
                                if(isManualTravelTime.value){
                                    configurationEntity.fixedTravelBuffer = manuallySetTravelTime.intValue
                                }
                                //stations needed, else null
                                else{
                                    configurationEntity.startStation = startStation.value
                                    configurationEntity.endStation = endStation.value
                                }
                                //set required start and endbuffer
                                configurationEntity.startBuffer = setStartBufferTime.intValue
                                configurationEntity.endBuffer = setEndBufferTime.intValue

                                //Setting days parameter
                                var days = mutableSetOf<DayOfWeek>()
                                selectedDays.value.forEachIndexed { index, active ->
                                    if(active) days.add(DayOfWeek.entries[index])
                                }
                                configurationEntity.days = days

                                core.generateOrUpdateAlarmConfiguration(configurationEntity)
                                core.setAlarmClockOverviewScreen()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),

                    ) {
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = "Speichern",
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                TextField(
                    shape = RoundedCornerShape(8.dp),
                    value = alarmName.value,
                    onValueChange = {alarmName.value = it},
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .padding(24.dp, 8.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                )
                innerDatengrundlageComposable(innerPadding,core)
                innerFahrtwegComposable(innerPadding,core)
                innerZeitaufwandComposable(innerPadding,core)
                innerGueltigkeitComposable(innerPadding,core)
            }
        }
    }
}

@Composable
fun EditComposable(modifier: Modifier, core: I_Core) {
    NavBar.NavigationBar(modifier, core, AlarmClockEditScreen.innerEditComposable, caller = AlarmClockEditScreen::class)
}

//Dialogs for Picking LocalTime and Minutes
//Are stored outside of Class because they are public and independent components
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

//I dont know why both functions are needed to display the time picker dialog, but thats okay
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialWithDialogExample(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    TimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(timePickerState) }
    ) {
        TimePicker(
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
            )
    }
}
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
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
        text = { content() }
    )
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