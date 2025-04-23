package de.heinzenburger.g2_weckmichmal.ui.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.Core
import de.heinzenburger.g2_weckmichmal.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.DayOfWeek
import java.time.LocalTime

class AlarmClockOverviewScreen : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        setContent {
            G2_WeckMichMalTheme {
                AlarmClockOverviewComposable(modifier = Modifier, core)
            }
        }
    }

    companion object{
        var aPlatypus = true
        val SingleAlarmConfiguration :
                @Composable (PaddingValues, I_Core, String, Set<DayOfWeek>, Boolean, LocalTime?)
                -> Unit = { innerPadding: PaddingValues, core: I_Core, name : String,
                            days : Set<DayOfWeek>, isActive : Boolean, time : LocalTime? ->
            var userActivated by remember { mutableStateOf(isActive) }

            Box(
                contentAlignment = Alignment.TopEnd
            ){
                Column(
                    Modifier
                        .padding(0.dp, 16.dp)
                        .clip(RoundedCornerShape(15))
                        .background(MaterialTheme.colorScheme.onBackground)
                        .fillMaxWidth(0.8f)
                ){
                    Row(
                        Modifier
                            .padding()
                            .background(Color.Transparent)
                            .fillMaxWidth(1f)
                    ) {
                        Switch(
                            checked = isActive,
                            onCheckedChange = { userActivated = it },
                            enabled = true,
                            colors = SwitchDefaults.colors(
                                checkedBorderColor = MaterialTheme.colorScheme.background,
                                uncheckedBorderColor = MaterialTheme.colorScheme.background,
                                checkedIconColor = MaterialTheme.colorScheme.primary,
                                uncheckedIconColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp)
                        )
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = name,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp, 24.dp, 0.dp, 0.dp)
                        )
                    }
                    Row(
                        Modifier
                            .background(Color.Transparent)
                            .fillMaxWidth(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp, 0.dp),
                        ){
                            days.forEach {
                                Text(
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = it.name[0] +""+ it.name[1].lowercase(),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 3.dp),
                                )
                            }
                        }

                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = "Geplant um: $time",
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 3.dp).fillMaxWidth(1f)
                        )
                    }
                }
                Button(
                    onClick = {
                        core.setAlarmClockOverviewScreen()
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.background,
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .border(2.dp, Color.Transparent,
                            RoundedCornerShape(50))
                        .size(40.dp)

                ){
                    Text(
                        text = "⨯",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.background,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        val APlatypus :
                @Composable (PaddingValues, I_Core, String, Set<DayOfWeek>, Boolean, LocalTime?)
                -> Unit = { innerPadding: PaddingValues, core: I_Core, name : String,
                            days : Set<DayOfWeek>, isActive : Boolean, time : LocalTime? ->
            var userActivated by remember { mutableStateOf(isActive) }

            Column(
                verticalArrangement = Arrangement.spacedBy((-18).dp),
            ){
                Box(
                    modifier = Modifier
                        .align(BiasAlignment.Horizontal(-0.6f))
                        .zIndex(2f)
                        .size(50.dp)
                        .clip(GenericShape { size, _ ->
                            moveTo(size.width*0.3f, size.height*0.5f)
                            lineTo(size.width*0.7f, size.height*0.5f)
                            lineTo(size.width*0.7f, size.height*0.9f)
                            lineTo(size.width, size.height*0.9f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            lineTo(0f, size.height*0.9f)
                            lineTo(size.width*0.3f, size.height*0.9f)
                        })
                        .background(color = Color(66, 48, 11))
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((-10).dp)
                ){
                    Button(
                        onClick = {
                            core.setAlarmClockOverviewScreen()
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.background,
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .zIndex(1f)
                            .border(2.dp, Color.Transparent,
                                RoundedCornerShape(50))

                    ){
                        Text(
                            text = "⨯",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.background,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Column(
                        Modifier
                            .padding(0.dp, 16.dp)
                            .clip(RoundedCornerShape(15))
                            .background(MaterialTheme.colorScheme.onBackground)
                            .fillMaxWidth(0.8f)
                    ){
                        Row(
                            Modifier
                                .padding()
                                .background(Color.Transparent)
                                .fillMaxWidth(1f)
                        ) {
                            Switch(
                                checked = isActive,
                                onCheckedChange = { userActivated = it },
                                enabled = true,
                                colors = SwitchDefaults.colors(
                                    checkedBorderColor = MaterialTheme.colorScheme.background,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.background,
                                    checkedIconColor = MaterialTheme.colorScheme.primary,
                                    uncheckedIconColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp)
                            )
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                text = name,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp, 24.dp, 0.dp, 0.dp)
                            )
                        }
                        Row(
                            Modifier
                                .background(Color.Transparent)
                                .fillMaxWidth(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(2.dp, 0.dp),
                            ){
                                days.forEach {
                                    Text(
                                        style = MaterialTheme.typography.bodyMedium,
                                        text = it.name[0] +""+ it.name[1].lowercase(),
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 3.dp),
                                    )
                                }
                            }

                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                text = "Geplant um: $time",
                                textAlign = TextAlign.Right,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 3.dp).fillMaxWidth(1f)
                            )
                        }
                    }
                    Button(
                        onClick = {
                            core.setAlarmClockOverviewScreen()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(150,100,0),
                        ),
                        modifier = Modifier
                            .zIndex(-1f)
                            .size(50.dp, 40.dp)
                            .padding(0.dp,20.dp,0.dp,0.dp)

                    ){

                    }
                }
                Box(
                    modifier = Modifier
                        .align(BiasAlignment.Horizontal(0f))
                        .zIndex(2f)
                        .width(200.dp)
                        .height(30.dp)
                        .clip(GenericShape { size, _ ->
                            moveTo(size.width*0.17f, 0f)
                            lineTo(size.width*0.17f, size.height)
                            lineTo(size.width*0.05f, size.height)
                            lineTo(size.width*0.05f, size.height*0.9f)
                            lineTo(size.width*0.13f, size.height*0.9f)
                            lineTo(size.width*0.13f, size.height*0f)

                            moveTo(size.width*0.87f, 0f)
                            lineTo(size.width*0.87f, size.height)
                            lineTo(size.width*0.75f, size.height)
                            lineTo(size.width*0.75f, size.height*0.9f)
                            lineTo(size.width*0.83f, size.height*0.9f)
                            lineTo(size.width*0.83f, size.height*0f)
                        })
                        .background(color = Color(251, 176, 51))
                )
            }
        }
    }
}

@Composable
fun AlarmClockOverviewComposable(modifier: Modifier, core: I_Core) {
    NavBar.NavigationBar(modifier, core, innerAlarmClockOverviewComposable,
        AlarmClockOverviewScreen::class)
}

val innerAlarmClockOverviewComposable : @Composable (PaddingValues, I_Core) -> Unit = { innerPadding: PaddingValues, core: I_Core ->
    Column(
        Modifier
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)) {
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "Wecker",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        Column(Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            core.getAllAlarmConfigurations()?.forEach {
                if(AlarmClockOverviewScreen.aPlatypus){
                    AlarmClockOverviewScreen.APlatypus(
                        innerPadding, core, it.name,
                        it.days, true, core.getPlannedTimeForAlarmEntity(it)
                    )
                }
                else{
                    AlarmClockOverviewScreen.SingleAlarmConfiguration(
                        innerPadding, core, it.name,
                        it.days, true, core.getPlannedTimeForAlarmEntity(it)
                    )
                }

            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AlarmClockOverviewScreenPreview() {
    val core = MockupCore()
    G2_WeckMichMalTheme {
        AlarmClockOverviewComposable(modifier = Modifier, core = core)
    }
}