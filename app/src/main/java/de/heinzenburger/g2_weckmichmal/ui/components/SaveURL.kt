package de.heinzenburger.g2_weckmichmal.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurTextField
import kotlin.concurrent.thread


class SaveURL {
    companion object{
        val innerSettingsComposable : @Composable (PaddingValues, CoreSpecification, () -> Unit) -> Unit = { innerPadding, core, onSave ->
            var tempURL = core.getRaplaURL()
            var url = remember { mutableStateOf(tempURL ?: "") }

            OurTextField(
                value = url.value,
                onValueChange = {url.value = it},
                modifier = Modifier.padding(16.dp),
                placeholderText = "URL beginnend mit https:// oder http://",
            )

            Button(
                onClick = {
                    onclickSaveButton(core, onSave, url.value)
                },
                colors = ButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = MaterialTheme.colorScheme.error,
                    disabledContentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                OurText(
                    text = "Vorlesungsplan speichern",
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        fun onclickSaveButton(core: CoreSpecification, onSave: () -> Unit, url: String){
            if(core.isInternetAvailable()){
                thread {
                    //very very dirty aber was soll man machen...
                    var updatedUrl = url.replace("page=calendar","page=ical")
                    updatedUrl = updatedUrl.replace("TINF23B2","TINF23BN2")
                    updatedUrl = updatedUrl.replace("TINF23B1","TINF23BN1")
                    updatedUrl = updatedUrl.replace("TINF23B3","TINF23BN3")
                    updatedUrl = updatedUrl.replace("TINF23B4","TINF23BN4")
                    updatedUrl = updatedUrl.replace("TINF23B5","TINF23BN5")
                    updatedUrl = updatedUrl.replace("TINF23B6","TINF23BN6")
                    if (updatedUrl == "" || core.isValidCourseURL(updatedUrl)) {
                        core.saveRaplaURL(updatedUrl)
                        onSave()
                        core.showToast("Passt")

                    } else {
                        core.showToast("Immer Schulfrei??? Da stimmt doch was mit der URL nicht...")
                    }
                }
            }
            else{
                core.showToast("Aufgrund der Validierung ist eine Internetverbindung nötig")
            }
        }
    }
}