package de.heinzenburger.g2_weckmichmal.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
/**
 * Until now, the following Elements have been defined:
 * [OurTextField]: Should be used as a TextField
 * [OurText]: Should be used for text that is medium sized
 *
 * Optional Elements:
 * [OurButtonInEditAlarm]: The Buttons in AlarmEditScreen with primary color as background and Text
 */
class BasicElements {
    companion object{
        @Composable
        fun OurTextField(value: String, onValueChange: (String)->Unit, modifier: Modifier, placeholderText: String) {
            TextField(
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.background,
                    unfocusedTextColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = Color.Companion.Transparent,
                    unfocusedIndicatorColor = Color.Companion.Transparent,
                    disabledIndicatorColor = Color.Companion.Transparent,
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    selectionColors = TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.secondary,
                        backgroundColor = MaterialTheme.colorScheme.onBackground
                    ),
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onBackground
                ),
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = modifier,
                placeholder = {
                    OurText(
                        text = placeholderText,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier,
                    )
                }
            )
        }

        @Composable fun OurButtonInEditAlarm(modifier: Modifier, onClick: ()->Unit, text: String, enabled: Boolean = true){
            Button(
                modifier = modifier,
                enabled = enabled,
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = text,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(0.dp)
                )
            }
        }

        @Composable fun OurText(color: Color = MaterialTheme.colorScheme.primary,
                                text: String,
                                modifier: Modifier,
                                textAlign: TextAlign = TextAlign.Center){
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier,
                textAlign = textAlign
            )
        }
    }
}