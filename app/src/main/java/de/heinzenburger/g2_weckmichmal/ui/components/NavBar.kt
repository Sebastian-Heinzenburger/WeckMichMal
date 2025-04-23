package de.heinzenburger.g2_weckmichmal.ui.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import java.lang.reflect.Type
import kotlin.reflect.KClass

class NavBar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    companion object{
        @Composable
        fun <T> NavigationBar(modifier: Modifier, core: I_Core, callback: @Composable ((PaddingValues, I_Core) -> Unit), caller : T) {
            val iconSize = 35.dp
            val iconColor = MaterialTheme.colorScheme.primary
            val iconSelectedColor = MaterialTheme.colorScheme.secondary
            Scaffold(
                bottomBar = {
                    BottomAppBar(containerColor = MaterialTheme.colorScheme.onPrimary) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = { core.setSettingsScreen() }, modifier.size(90.dp)) {
                                Icon(Icons.Filled.Settings,
                                    contentDescription = "Localized description",
                                    modifier.size(iconSize),
                                    tint = if(caller == SettingsScreen::class){iconSelectedColor}else{iconColor},
                                    )
                            }
                            IconButton(onClick = { core.setAlarmClockOverviewScreen() }, modifier.size(90.dp)) {
                                Icon(
                                    Icons.Filled.AccessAlarm,
                                    contentDescription = "Localized description",
                                    modifier.size(iconSize),
                                    tint = if(caller == AlarmClockOverviewScreen::class || caller == AlarmClockEditScreen::class){iconSelectedColor}else{iconColor},
                                    )
                            }
                            IconButton(onClick = { core.setInformationScreen() }, modifier.size(90.dp)) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = "Localized description",
                                    modifier.size(iconSize),
                                    tint = if(caller == InformationScreen::class){iconSelectedColor}else{iconColor},
                                    )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                callback(innerPadding, core)
            }
        }
    }
}

