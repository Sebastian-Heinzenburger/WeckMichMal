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

class NavBar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    companion object{
        @Composable
        fun NavigationBar(modifier: Modifier, core: I_Core, callback: @Composable ((PaddingValues, I_Core) -> Unit)) {
            val iconSize = 35.dp
            val iconColor = MaterialTheme.colorScheme.primary
            Scaffold(
                bottomBar = {
                    BottomAppBar(containerColor = MaterialTheme.colorScheme.onPrimary) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = { /* do something */ }, modifier.size(90.dp)) {
                                Icon(Icons.Filled.Settings,
                                    contentDescription = "Localized description",
                                    modifier.size(iconSize),
                                    tint = iconColor,

                                    )
                            }
                            IconButton(onClick = { /* do something */ }, modifier.size(90.dp)) {
                                Icon(
                                    Icons.Filled.AccessAlarm,
                                    contentDescription = "Localized description",
                                    modifier.size(iconSize),
                                    tint = iconColor,
                                )
                            }
                            IconButton(onClick = { /* do something */ }, modifier.size(90.dp)) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = "Localized description",
                                    modifier.size(iconSize),
                                    tint = iconColor,

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

