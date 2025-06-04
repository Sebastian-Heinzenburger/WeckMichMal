package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.ExceptionHandler
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.SaveURL
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import kotlin.concurrent.thread

class WelcomeScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        ExceptionHandler(core).runWithUnexpectedExceptionHandler("Error displaying Welcome",true) {
            setContent {
                BackHandler {
                    //Finish all and close the app
                    finishAffinity()
                }
                G2_WeckMichMalTheme {
                    Greeting(modifier = Modifier, core)
                }
            }
        }
    }

    @Composable
    fun Greeting(modifier: Modifier, core: CoreSpecification) {
        val context = LocalContext.current
        Column(modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                style = MaterialTheme.typography.titleMedium,
                text = "Willkommen",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = modifier.padding(16.dp)
            )
            Text(
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                text = "Beginne damit, deinen Vorlesungsplan zu hinterlegen",
                color = MaterialTheme.colorScheme.error,
                modifier = modifier.padding(16.dp)
            )
            SaveURL().innerSettingsComposable(
                PaddingValues(0.dp), core,
                fun () {
                    val intent = Intent(context, AllowNotificationsScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                }
            )
            Button(
                onClick = {
                    thread {
                        core.saveRaplaURL("")
                    }
                    val intent = Intent(context, AllowNotificationsScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                },
                colors = ButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error,
                    disabledContentColor = MaterialTheme.colorScheme.error
                ),
                modifier = modifier.padding(16.dp)
            ) {
                OurText(
                    text = "Ohne Vorlesungsplan fortfahren",
                    modifier = modifier.padding(8.dp),
                )
            }
        }
    }
}
//Is only called when json settings file is not found on android device



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val welcomeScreen = WelcomeScreen()
    G2_WeckMichMalTheme {
        welcomeScreen.Greeting(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}