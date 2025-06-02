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
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.ExceptionHandler
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.LoadingScreen
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.ExcludeCourseDialog
import de.heinzenburger.g2_weckmichmal.ui.components.SaveURL
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import kotlin.concurrent.thread

class SettingsScreen : ComponentActivity() {
    var listOfCourses = mutableStateListOf<String>()
    var listOfExcludedCourses = mutableStateListOf<String>()
    private var openLoadingScreen = mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        ExceptionHandler(core).runWithUnexpectedExceptionHandler("Error displaying Settings",true) {
            val locUrl = core.getRaplaURL()
            if (locUrl != null) {
                url.value = locUrl
            }
            thread {
                core.getListOfNameOfCourses()?.forEach { listOfCourses.add(it) }
                core.getListOfExcludedCourses()?.forEach { listOfExcludedCourses.add(it) }
            }
            setContent {
                val context = LocalContext.current
                BackHandler {
                    //Go to Overview Screen without animation
                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                }
                G2_WeckMichMalTheme {
                    SettingsComposable(modifier = Modifier, core)
                }
            }
        }
    }

    @Composable
    fun SettingsComposable(modifier: Modifier, uiActions: CoreSpecification) {
        NavBar.Companion.NavigationBar(modifier, uiActions, innerSettingsComposable, SettingsScreen::class)
    }

    private var url = mutableStateOf("https://")
    //Main component
    val innerSettingsComposable : @Composable (PaddingValues, CoreSpecification) -> Unit = { innerPadding: PaddingValues, core: CoreSpecification ->
        val context = LocalContext.current
        val openExcludeCourseDialog = remember { mutableStateOf(false) }

        when{
            openExcludeCourseDialog.value ->{
                ExcludeCourseDialog(
                    onDismiss = { excludeCoursesList ->
                        openExcludeCourseDialog.value = false
                        listOfExcludedCourses = mutableStateListOf()
                        excludeCoursesList.forEach {
                            listOfExcludedCourses.add(it)
                        }
                        core.updateListOfExcludedCourses(excludeCoursesList)
                        thread{
                            core.runUpdateLogic()
                        }
                    },
                    listOfCourses = listOfCourses,
                    listOfExcludedCourses = listOfExcludedCourses
                )
            }
        }
        when{
            openLoadingScreen.value ->{
                LoadingScreen()
            }
        }

        Column(
            Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)) {
            OurText(
                text = "Einstellungen",
                modifier = Modifier.padding(16.dp)
            )
            Column(Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){

                Button(
                    onClick = {
                        openLoadingScreen.value = true
                        val intent = Intent(context, LogScreen::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)
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

                Text(
                    text = "Vorlesungsplan",
                    modifier = Modifier.padding(top = 48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )

                if(url.value != ""){
                    Button(
                        onClick = {
                            if(listOfCourses.isEmpty()){
                                core.showToast("Einen Moment, Kurse sind noch nicht geladen")
                            }
                            else {
                                openExcludeCourseDialog.value = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        OurText(
                            text = "Vorlesungen ausschließen",
                            modifier = Modifier
                        )
                    }
                }

                SaveURL().innerSettingsComposable(innerPadding, core,
                    fun () {
                        val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)
                        finish()
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
