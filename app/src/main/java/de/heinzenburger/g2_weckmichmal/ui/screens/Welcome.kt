package de.heinzenburger.g2_weckmichmal.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.SaveURL
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme

class WelcomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        setContent {
            G2_WeckMichMalTheme {
                Greeting(modifier = Modifier, core)
            }
        }
    }
}
//Is only called when json settings file is not found on android device
@Composable
fun Greeting(modifier: Modifier, core: I_Core) {

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
        SaveURL.innerSettingsComposable(
            PaddingValues(0.dp), core,
            fun () {
                core.setAlarmClockOverviewScreen()
            }
        )
        Button(
            onClick = {
                core.saveRaplaURL("")
                core.setAlarmClockOverviewScreen()
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    G2_WeckMichMalTheme {
        Greeting(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}