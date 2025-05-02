package de.heinzenburger.g2_weckmichmal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.persistence.AlarmConfiguration
import java.util.logging.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val core = Core(context = applicationContext)

        val serviceIntent = Intent(this, ForegroundService::class.java)
        startService(serviceIntent)

        if (core.isApplicationOpenedFirstTime()) {
            core.setWelcomeScreen()
        } else {
            core.setAlarmClockOverviewScreen()
        }
    }

    companion object {
        val log: Logger = Logger.getLogger(AlarmConfiguration::class.java.name)
    }
}
