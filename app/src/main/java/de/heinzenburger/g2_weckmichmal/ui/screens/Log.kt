package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.ConfirmDialog
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

class LogScreen : ComponentActivity() {

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
                LogComposable(modifier = Modifier, core)
            }
        }
    }
    private val showConfirmDialog = mutableStateOf(false)

    val innerLogComposable: @Composable (PaddingValues, CoreSpecification) -> Unit =
        { innerPadding: PaddingValues, core: CoreSpecification ->
            val context = LocalContext.current
            when {
                showConfirmDialog.value -> {
                    ConfirmDialog(
                        onConfirm = {
                            if(core.isInternetAvailable()){
                                thread {
                                    try {
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
                                    catch (e: Exception){
                                        core.log(Logger.Level.SEVERE, e.message.toString())
                                        core.showToast("Das hat nicht geklappt")
                                    }
                                }
                            }
                            else{
                                core.showToast("Dafür ist eine Internetverbindung nötig")
                            }
                            showConfirmDialog.value = false
                        },
                        onDismiss = {
                            showConfirmDialog.value = false
                        }
                    )
                }
            }

            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
            ) {


                // Datenschutzerklärungsbutton
                Button(
                    modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = {
                        AlarmClockOverviewScreen.aPlatypus = !AlarmClockOverviewScreen.aPlatypus
                    }
                ) {
                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        text = "Datenschutzerklärung unter fzuerner.com/privacy",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                // Log Analysis Button
                Button(
                    onClick = {
                        showConfirmDialog.value = true
                    },
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp)
                        .align(Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    OurText(text="Logs zur Analyse an Server senden", modifier = Modifier.padding(16.dp))
                }


                OurText(
                    text = core.getLog(),
                    modifier = Modifier,
                )
            }
        }
    @Composable
    fun LogComposable(modifier: Modifier, core: CoreSpecification) {
        NavBar.Companion.NavigationBar(
            modifier,
            core,
            innerLogComposable,
            caller = SettingsScreen::class
        )
    }
}



@Preview(showBackground = true)
@Composable
fun LogPreview() {
    val logScreen = LogScreen()
    G2_WeckMichMalTheme {
        logScreen.LogComposable(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}
