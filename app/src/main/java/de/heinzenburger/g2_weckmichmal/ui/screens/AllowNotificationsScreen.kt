package de.heinzenburger.g2_weckmichmal.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import android.provider.Settings
import android.app.AlarmManager
import android.os.PowerManager
import androidx.core.net.toUri

class AllowNotificationsScreen : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted - proceed with notifications
        } else {
            Core(context = applicationContext).showToast("Please allow Notifications!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            BackHandler {
                //Finish all and close the app
                ActivityCompat.finishAffinity(context as ComponentActivity)
            }
            G2_WeckMichMalTheme {
                InnerComposable(modifier = Modifier)
            }
        }
    }

    @Composable
    fun InnerComposable(modifier: Modifier) {
        val context = LocalContext.current
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
        val allowNotifications = remember { mutableStateOf(true) }
        val allowNoBatteryOptimization = remember { mutableStateOf(true) }
        val allowSheduleAlarm = remember { mutableStateOf(true) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            allowNotifications.value = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        allowSheduleAlarm.value = alarmManager.canScheduleExactAlarms()
        allowNoBatteryOptimization.value = powerManager.isIgnoringBatteryOptimizations(packageName)


        Column(modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                text =
                    if(!allowNoBatteryOptimization.value){
                        "Um zu garantieren, dass sich der Wecker in deinem Schlaf aktualisieren kann, muss die App mögliche Batterie-Optimisierungen umgehen. Dein Akku-Verbrauch wird sich dabei nicht wirklich erhöhen. Drücke auf 'Weiter' und durchsuche die Liste nach G2-WeckMichMal um uneingeschränkte Hintergrundnutzung zu erlauben."
                    }
                    else if(!allowNotifications.value){
                        "WeckMichMal benötigt die Berechtigung, Benachrichtigungen zu senden. Nur so kann der Alarm dich zuverlässig wecken.\nKeine Sorge, es gibt keine Werbung oder störende Nachrichten."
                    }
                    else if(!allowSheduleAlarm.value){
                        "Außerdem benötigt WeckMichMal die Berechtigung, Alarme in deinem System zu setzen. Ansonsten verschläfst du jeden morgen."
                    }
                    else{
                        "Danke!"
                    },
                color = MaterialTheme.colorScheme.error,
                modifier = modifier.padding(16.dp)
            )

            if(!(allowNotifications.value && allowNoBatteryOptimization.value && allowSheduleAlarm.value)){
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground
                    ),
                    onClick = {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            allowNotifications.value = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        }
                        allowSheduleAlarm.value = alarmManager.canScheduleExactAlarms()
                        allowNoBatteryOptimization.value = powerManager.isIgnoringBatteryOptimizations(packageName)

                        if(!allowNoBatteryOptimization.value){
                            val intent = Intent()
                            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                            startActivity(intent)
                            allowNoBatteryOptimization.value = true
                        }

                        else if (!allowNotifications.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            allowNotifications.value = true
                        }

                        else if (!allowSheduleAlarm.value){
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = "package:$packageName".toUri()
                            }
                            startActivity(intent)
                            allowSheduleAlarm.value = true
                        }
                    }
                ) {
                    OurText(
                        text = "Weiter",
                        modifier = Modifier
                    )
                }
            }

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if(allowNotifications.value && allowSheduleAlarm.value){
                            MaterialTheme.colorScheme.onBackground
                        }
                        else{
                            MaterialTheme.colorScheme.error
                        },

                ),
                onClick = {
                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    context.startActivity(intent)
                    (context as ComponentActivity).finish()
                }
            ) {
                OurText(
                    text = if(allowNotifications.value && allowSheduleAlarm.value){
                        "Weiter"
                    }
                    else{
                        "Überspringen"
                    },
                    modifier = Modifier
                )
            }
        }
    }
}
//Is only called when json settings file is not found on android device



@Preview(showBackground = true)
@Composable
fun AllowNotificationsPreview() {
    val allowNotificationsScreen = AllowNotificationsScreen()
    G2_WeckMichMalTheme {
        allowNotificationsScreen.InnerComposable(
            modifier = Modifier,
        )
    }
}