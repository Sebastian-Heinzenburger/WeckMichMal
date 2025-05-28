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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.components.SaveURL
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme

class SettingsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        val locUrl = core.getRaplaURL()
        if(locUrl != null){
            url.value = locUrl
        }
        setContent {
            val context = LocalContext.current
            BackHandler {
                //Go to Overview Screen without animation
                val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            }
            G2_WeckMichMalTheme {
                SettingsComposable(modifier = Modifier, core)
            }
        }
    }

    @Composable
    fun SettingsComposable(modifier: Modifier, uiActions: CoreSpecification) {
        NavBar.Companion.NavigationBar(modifier, uiActions, innerSettingsComposable, SettingsScreen::class)
    }

    private var url = mutableStateOf("https://") //At the moment, only possible configuration

    //Main component
    val innerSettingsComposable : @Composable (PaddingValues, CoreSpecification) -> Unit = { innerPadding: PaddingValues, core: CoreSpecification ->
        val context = LocalContext.current

        Column(
            Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)) {
            OurText(
                text = "Einstellungen",
                modifier = Modifier.padding(16.dp)
            )
            Column(Modifier
                .background(MaterialTheme.colorScheme.background).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){

                Button(
                    onClick = {
                        val intent = Intent(context, LogScreen::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    OurText(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier,
                        text = "Logs ansehen"
                    )
                }

                SaveURL.innerSettingsComposable(innerPadding, core,
                    fun () {
                        val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        context.startActivity(intent)
                        (context as ComponentActivity).finish()
                    }
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val settingsScreen = SettingsScreen()
    G2_WeckMichMalTheme {
        settingsScreen.SettingsComposable(modifier = Modifier, uiActions = MockupCore())
    }
}
