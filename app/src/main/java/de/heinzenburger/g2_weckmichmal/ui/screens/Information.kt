package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import de.heinzenburger.g2_weckmichmal.specifications.MensaMeal
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

class InformationScreen : ComponentActivity() {

    val showConfirmDialog = mutableStateOf(false)

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

    //When text is clicked, platypus mode in AlarmClockOverviewScreen is activated hehe
    val innerInformationComposable: @Composable (PaddingValues, CoreSpecification) -> Unit =
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
            Column {
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
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    OurText(text="Logs zur Analyse an Server senden", modifier = Modifier.padding(16.dp))
                }
                // innerLogComposable(innerPadding, core)
                innerMensaComposable(innerPadding, core)
            }
        }
    val innerMensaComposable: @Composable (PaddingValues, CoreSpecification) -> Unit =
        { innerPadding: PaddingValues, core: CoreSpecification ->
            val mensaMeals = remember { mutableStateOf(emptyList<MensaMeal>()) }
            thread {
                mensaMeals.value = core.nextMensaMeals()
            }
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    style = MaterialTheme.typography.titleSmall,
                    text = "Mensa Essensplan",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(10.dp)
                )
                Column {
                    mensaMeals.value.forEach { meal ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween) {
                            OurText(
                                text = meal.name,
                                textAlign = TextAlign.Left,
                                modifier = Modifier.padding(16.dp).fillMaxWidth(0.75f),
                            )
                            OurText(
                                text = String.format("%.2f €", meal.price),
                                textAlign = TextAlign.Left,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    val innerLogComposable: @Composable (PaddingValues, CoreSpecification) -> Unit =
        { innerPadding: PaddingValues, core: CoreSpecification ->
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
    @Composable
    fun InformationComposable(modifier: Modifier, core: CoreSpecification) {
        NavBar.Companion.NavigationBar(
            modifier,
            core,
            innerInformationComposable,
            caller = InformationScreen::class
        )
    }
}



@Preview(showBackground = true)
@Composable
fun InformationPreview() {
    val informationScreen = InformationScreen()
    G2_WeckMichMalTheme {
        informationScreen.InformationComposable(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}
