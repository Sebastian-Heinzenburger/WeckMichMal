package de.heinzenburger.g2_weckmichmal.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockOverviewScreen.Companion.configurationAndEventEntities
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import kotlin.concurrent.thread

class AlarmClockOverviewScreen : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        thread {
            configurationAndEventEntities.value = core.getAllConfigurationAndEvent()!!
        }


        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        if(!alarmManager.canScheduleExactAlarms()){
            core.showToast("Bitte erlaube Alarm Erstellung in den Systemeinstellungen")
        }
        if(!powerManager.isIgnoringBatteryOptimizations(packageName)){
            core.showToast("Bitte erlaube uneingeschränkte Hintergrundnutzung in den battery optimization Einstellungen")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                core.showToast("Bitte erlaube Notifications in den Systemeinstellungen")
            }
        }

        setContent {
            G2_WeckMichMalTheme {
                val context = LocalContext.current
                BackHandler {
                    //Finish all and close the app
                    finishAffinity(context as ComponentActivity)
                }
                AlarmClockOverviewComposable(modifier = Modifier, core)
            }
        }
    }

    companion object{
        private fun deleteConfiguration(
            core: CoreSpecification,
            properties: ConfigurationWithEvent,
            context: Context
        ){
            thread {
                core.deleteAlarmConfiguration(properties.configuration.uid)

                val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            }
        }

        private fun setEditScreen(context: Context, configuration: Configuration?){
            val core = Core(context)
            AlarmClockEditScreen.defaultAlarmValues = core.getDefaultAlarmValues()
            AlarmClockEditScreen.reset(configuration)

            val intent = Intent(context, AlarmClockEditScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(intent)
            (context as ComponentActivity).finish()
        }

        private fun setRingingScreen(context: Context, event: Event?){
            if (event != null) {
                val intent = Intent(context, AlarmRingingScreen::class.java)
                intent.putExtra("configID", event.configID)
                intent.putExtra("isPreview", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            }
        }

        var aPlatypus = false //Static variable to set Platypus mode

        //List of all configurationAndEvent Entities existant in database
        internal var configurationAndEventEntities = mutableStateOf(emptyList<ConfigurationWithEvent>())

        //Elements in Configuration Component that stay the same, regardless of Platypus
        private val innerSingleAlarmConfiguration:
                @Composable (PaddingValues, CoreSpecification, ConfigurationWithEvent)
                -> Unit = { innerPadding: PaddingValues, core: CoreSpecification, properties: ConfigurationWithEvent ->
            var userActivated by remember { mutableStateOf(properties.configuration.isActive) }
            val context = LocalContext.current

            Column(
                Modifier
                    .padding(0.dp, 16.dp)
                    .clip(RoundedCornerShape(15))
                    .background(MaterialTheme.colorScheme.onBackground)
                    .fillMaxWidth(0.8f)
            ){
                Row(
                    Modifier
                        .padding()
                        .background(Color.Transparent)
                        .fillMaxWidth(1f)
                ) {
                    Switch(
                        checked = userActivated,
                        //Configuration will always be reset to active when edited in AlarmClockEdit
                        onCheckedChange = {
                            userActivated = it
                            thread {
                                core.updateConfigurationActive(userActivated, properties.configuration)
                                core.runUpdateLogic()
                            }
                        },
                        enabled = true,
                        colors = SwitchDefaults.colors(
                            checkedBorderColor = MaterialTheme.colorScheme.background,
                            uncheckedBorderColor = MaterialTheme.colorScheme.background,
                            checkedIconColor = MaterialTheme.colorScheme.primary,
                            uncheckedIconColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp)
                    )
                    OurText(
                        text = properties.configuration.name,
                        modifier = Modifier.padding(16.dp, 24.dp, 0.dp, 0.dp)
                    )
                }
                Row(
                    Modifier
                        .background(Color.Transparent)
                        .fillMaxWidth(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp, 0.dp),
                    ){
                        properties.configuration.days.forEach {
                            OurText(
                                text = it.name[0] +""+ it.name[1].lowercase(),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 3.dp),
                            )
                        }
                    }

                    Button(
                        modifier = Modifier,
                        onClick = {
                            setRingingScreen(context, properties.event)
                        },
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        OurText(
                            text = "Geplant um: ${properties.event?.wakeUpTime} \uD83D\uDCC4",
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 3.dp).fillMaxWidth(1f)
                        )
                    }
                }
            }
        }

        //UI Arrangement for Components when Platypus mode is off
        private val SingleAlarmConfiguration :
                @Composable (PaddingValues, CoreSpecification, ConfigurationWithEvent)
                -> Unit = { innerPadding: PaddingValues, core: CoreSpecification, properties: ConfigurationWithEvent ->
            val context = LocalContext.current
            Box(
                contentAlignment = Alignment.TopEnd
            ){
                innerSingleAlarmConfiguration(innerPadding, core, properties)
                Button(
                    onClick = {
                        deleteConfiguration(core, properties, context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.background,
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .border(2.dp, Color.Transparent,
                            RoundedCornerShape(50))
                        .size(40.dp)

                ){
                    Text(
                        text = "⨯",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.background,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        //UI Arrangement for Components when Platypus mode is activated
        private val APlatypus : @Composable (PaddingValues, CoreSpecification, ConfigurationWithEvent)
                -> Unit = { innerPadding: PaddingValues, core: CoreSpecification, properties: ConfigurationWithEvent ->
            val context = LocalContext.current
            Column(
                verticalArrangement = Arrangement.spacedBy((-18).dp),
            ){
                Box(
                    modifier = Modifier
                        .align(BiasAlignment.Horizontal(-0.6f))
                        .zIndex(2f)
                        .size(50.dp)
                        .clip(GenericShape { size, _ ->
                            moveTo(size.width*0.3f, size.height*0.5f)
                            lineTo(size.width*0.7f, size.height*0.5f)
                            lineTo(size.width*0.7f, size.height*0.9f)
                            lineTo(size.width, size.height*0.9f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            lineTo(0f, size.height*0.9f)
                            lineTo(size.width*0.3f, size.height*0.9f)
                        })
                        .background(color = Color(66, 48, 11))
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((-10).dp)
                ){
                    Button(
                        onClick = {
                            deleteConfiguration(core, properties, context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.background,
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .zIndex(1f)
                            .border(2.dp, Color.Transparent,
                                RoundedCornerShape(50))

                    ){
                        Text(
                            text = "⨯",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.background,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    innerSingleAlarmConfiguration(innerPadding, core, properties)

                    Button(
                        onClick = {
                            core.showToast("Perry the Platypus!?")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(150,100,0),
                        ),
                        modifier = Modifier
                            .zIndex(-1f)
                            .size(50.dp, 40.dp)
                            .padding(0.dp,20.dp,0.dp,0.dp)
                    ){

                    }
                }
                Box(
                    modifier = Modifier
                        .align(BiasAlignment.Horizontal(0f))
                        .zIndex(2f)
                        .width(200.dp)
                        .height(30.dp)
                        .clip(GenericShape { size, _ ->
                            moveTo(size.width*0.17f, 0f)
                            lineTo(size.width*0.17f, size.height)
                            lineTo(size.width*0.05f, size.height)
                            lineTo(size.width*0.05f, size.height*0.9f)
                            lineTo(size.width*0.13f, size.height*0.9f)
                            lineTo(size.width*0.13f, size.height*0f)

                            moveTo(size.width*0.87f, 0f)
                            lineTo(size.width*0.87f, size.height)
                            lineTo(size.width*0.75f, size.height)
                            lineTo(size.width*0.75f, size.height*0.9f)
                            lineTo(size.width*0.83f, size.height*0.9f)
                            lineTo(size.width*0.83f, size.height*0f)
                        })
                        .background(color = Color(251, 176, 51))
                )
            }
        }

        //Main Component, is passed to Navbar. Contains all configuration components and plus icon
        val innerAlarmClockOverviewComposable : @Composable (PaddingValues, CoreSpecification) -> Unit = { innerPadding: PaddingValues, core: CoreSpecification ->
            val context = LocalContext.current

            Box {
                Column(
                    Modifier
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    OurText(
                        text = "Wecker",
                        modifier = Modifier.padding(16.dp)
                    )
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(weight = 1f, fill = true)
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        configurationAndEventEntities.value.forEach {
                            configurationAndEvent ->
                            Button(
                                onClick = {
                                    setEditScreen(context, configurationAndEvent.configuration)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(0.dp)
                            ){
                                if (aPlatypus) {
                                    APlatypus(
                                        innerPadding, core, configurationAndEvent
                                    )
                                } else {
                                    SingleAlarmConfiguration(
                                        innerPadding, core, configurationAndEvent
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = {
                                setEditScreen(context, null)
                            },
                            colors = ButtonDefaults.buttonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.secondary,
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(69.dp)
                                .border(2.dp, Color.Transparent,
                                    RoundedCornerShape(50))

                        ) {
                            Text(text = "+", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun AlarmClockOverviewComposable(modifier: Modifier, core: CoreSpecification) {
    NavBar.Companion.NavigationBar(modifier, core, AlarmClockOverviewScreen.innerAlarmClockOverviewComposable,
        AlarmClockOverviewScreen::class)
}

@Preview(showBackground = true)
@Composable
fun AlarmClockOverviewScreenPreview() {
    val core = MockupCore()
    configurationAndEventEntities.value = core.getAllConfigurationAndEvent()!!
    G2_WeckMichMalTheme {
        AlarmClockOverviewComposable(modifier = Modifier, core = core)
    }
}