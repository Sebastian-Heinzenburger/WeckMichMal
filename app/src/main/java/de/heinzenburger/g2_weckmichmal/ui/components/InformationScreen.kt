package de.heinzenburger.g2_weckmichmal.ui.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme

class InformationScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        setContent {
            G2_WeckMichMalTheme {
                InformationComposable(modifier = Modifier, core)
            }
        }
    }
}

@Composable
fun InformationComposable(modifier: Modifier, core: I_Core) {
    NavBar.NavigationBar(modifier, core, innerInformationComposable, caller = InformationScreen::class)
}

val innerInformationComposable : @Composable (PaddingValues, I_Core) -> Unit =
    { innerPadding: PaddingValues, core: I_Core ->
        Button(
            modifier = Modifier.padding(0.dp,50.dp,0.dp,0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            onClick = {
                AlarmClockOverviewScreen.aPlatypus = !AlarmClockOverviewScreen.aPlatypus
            }
        ) {
            Text(
                style = MaterialTheme.typography.titleMedium,
                text = "Ich fürchte, dass dieser Screen unverändert in der Production landen wird",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }

@Preview(showBackground = true)
@Composable
fun InformationPreview() {
    G2_WeckMichMalTheme {
        InformationComposable(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}