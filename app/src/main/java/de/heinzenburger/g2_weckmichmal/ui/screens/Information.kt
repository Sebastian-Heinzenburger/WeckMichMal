package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

class InformationScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
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
                InformationComposable(modifier = Modifier, core)
            }
        }
    }

    companion object {
        //When text is clicked, platypus mode in AlarmClockOverviewScreen is activated hehe
        val innerInformationComposable: @Composable (PaddingValues, I_Core) -> Unit =
            { innerPadding: PaddingValues, core: I_Core ->
                val context = LocalContext.current
                Column {
                    Button(
                        onClick = {
                            val intent = Intent(context, AlarmRingingScreen::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            context.startActivity(intent)
                            (context as ComponentActivity).finish()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        contentPadding = PaddingValues(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text("\uD83D\uDCC4")
                    }
                    Button(
                        onClick = {
                            thread {
                                val file = File(context.filesDir, "log")

                                val requestBody = MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("logFile", "file", file.asRequestBody())
                                    .build()

                                val request = Request.Builder()
                                    .url("https://log.heinzenburger.de/submit")
                                    .post(requestBody)
                                    .build()

                                OkHttpClient().newCall(request).execute().use { response ->
                                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                    val responseText = response.body?.string()
                                    core.showToast(responseText!!)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        contentPadding = PaddingValues(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text("\uD83D\uDD25")
                    }
                    Button(
                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp),
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
                    innerLogComposable(innerPadding, core)
                }
            }
        val innerLogComposable: @Composable (PaddingValues, I_Core) -> Unit =
            { innerPadding: PaddingValues, core: I_Core ->
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    OurText(
                        text = core.getLog(),
                        modifier = Modifier,
                    )
                }
            }
    }
}

@Composable
fun InformationComposable(modifier: Modifier, core: I_Core) {
    NavBar.Companion.NavigationBar(
        modifier,
        core,
        InformationScreen.innerInformationComposable,
        caller = InformationScreen::class
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