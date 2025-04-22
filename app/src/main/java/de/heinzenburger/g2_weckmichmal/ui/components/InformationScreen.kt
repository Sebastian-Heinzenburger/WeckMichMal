package de.heinzenburger.g2_weckmichmal.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.Core
import de.heinzenburger.g2_weckmichmal.MockupCore
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
        Text(
            style = MaterialTheme.typography.titleMedium,
            text = "Ich fürchte, dass dieser Screen unverändert in der Production landen wird",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
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