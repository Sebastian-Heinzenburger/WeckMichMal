package de.heinzenburger.g2_weckmichmal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import kotlin.concurrent.thread


class SaveURLComponent {
    companion object{
        val innerSettingsComposable : @Composable (PaddingValues, I_Core, () -> Unit) -> Unit = { innerPadding, core, onSave ->
            var tempURL = core.getRaplaURL()
            var url = remember { mutableStateOf(tempURL ?: "") }
            TextField(
                shape = RoundedCornerShape(8.dp),
                value = url.value,
                onValueChange = {url.value = it},
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.background,
                    unfocusedTextColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    selectionColors = TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.secondary,
                        backgroundColor = MaterialTheme.colorScheme.onBackground
                    ),
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(16.dp),
                placeholder = {
                    Text(
                        text = "URL beginnend mit https:// oder http://",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        )
                }
            )
            Button(
                onClick = {
                    thread {
                        if (core.isValidCourseURL(url.value)) {
                            core.saveRaplaURL(url.value)
                            onSave()
                            core.showToast("Passt")

                        } else {
                            core.showToast("Immer Schulfrei??? Da stimmt doch was mit der URL nicht...")
                        }
                    }
                },
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