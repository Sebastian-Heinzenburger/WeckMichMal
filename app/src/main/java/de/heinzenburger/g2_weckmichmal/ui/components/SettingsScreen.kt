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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
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
            G2_WeckMichMalTheme {
                SettingsComposable(modifier = Modifier, core)
            }
        }
    }
    companion object{
        private var url = mutableStateOf("https://") //At the moment, only possible configuration

        //Main component
        val innerSettingsComposable : @Composable (PaddingValues, I_Core) -> Unit = { innerPadding: PaddingValues, core: I_Core ->
            Column(
                Modifier
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)) {
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = "Einstellungen",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
                Column(Modifier
                    .background(MaterialTheme.colorScheme.background).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    TextField(
                        shape = RoundedCornerShape(8.dp),
                        value = url.value,
                        onValueChange = {url.value = it},
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = { core.saveRaplaURL(url.value) },
                        colors = ButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            disabledContainerColor = MaterialTheme.colorScheme.error,
                            disabledContentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Vorlesungsplan speichern",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsComposable(modifier: Modifier, uiActions: I_Core) {
    NavBar.NavigationBar(modifier, uiActions, SettingsScreen.innerSettingsComposable, SettingsScreen::class)
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    G2_WeckMichMalTheme {
        SettingsComposable(modifier = Modifier, uiActions = MockupCore())
    }
}
