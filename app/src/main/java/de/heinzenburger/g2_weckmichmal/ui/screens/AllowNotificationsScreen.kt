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
        val skipText = remember { mutableStateOf("Überspringen") }
        Column(modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                text = "WeckMichMal benötigt die Berechtigung, Benachrichtigungen zu senden. Nur so kann der Alarm dich zuverlässig wecken. Keine Sorge, es gibt keine Werbung oder störende Nachrichten.",
                color = MaterialTheme.colorScheme.error,
                modifier = modifier.padding(16.dp)
            )

            if(skipText.value != "Weiter"){
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground
                    ),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                skipText.value = "Weiter"
                            }
                        }
                    }
                ) {
                    OurText(
                        text = "Berechtigung erteilen",
                        modifier = Modifier
                    )
                }
            }

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground
                ),
                onClick = {
                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    context.startActivity(intent)
                    (context as ComponentActivity).finish()
                }
            ) {
                OurText(
                    text = skipText.value,
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