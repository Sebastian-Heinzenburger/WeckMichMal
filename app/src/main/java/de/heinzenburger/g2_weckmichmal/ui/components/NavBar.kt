package de.heinzenburger.g2_weckmichmal.ui.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme

class NavBar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            G2_WeckMichMalTheme {
                NavigationBar(modifier = Modifier)
            }
        }
    }
}

@Composable
fun NavigationBar(modifier: Modifier) {
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
        Text(
            modifier = Modifier.padding(innerPadding),
            text = "Example of a scaffold with a bottom app bar."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationBarPriev() {
    G2_WeckMichMalTheme {
        NavigationBar(modifier = Modifier)
    }
}

