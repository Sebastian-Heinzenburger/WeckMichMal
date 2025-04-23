package de.heinzenburger.g2_weckmichmal

import android.os.Bundle
import androidx.activity.ComponentActivity
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import java.util.logging.Logger
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thread {
            MockupCore.insertMockupData(applicationContext)
        }
        Core(context = applicationContext).setWelcomeScreen()
    }
    companion object{
        val log: Logger = Logger.getLogger(AlarmConfiguration::class.java.name)
    }
}


