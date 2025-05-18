package de.heinzenburger.g2_weckmichmal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import de.heinzenburger.g2_weckmichmal.background.ForegroundService
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockOverviewScreen
import de.heinzenburger.g2_weckmichmal.ui.screens.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val core = Core(context = applicationContext)
        core.log(Logger.Level.INFO, "Starting Application")

        val serviceIntent = Intent(applicationContext, ForegroundService::class.java)
        serviceIntent.putExtra("configID",intent?.getLongExtra("configID",-1))
        applicationContext.startService(serviceIntent)


        if (core.isApplicationOpenedFirstTime()) {
            val intent = Intent(applicationContext, WelcomeScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
        } else {
            val intent = Intent(applicationContext, AlarmClockOverviewScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            applicationContext.startActivity(intent)
        }


        //Cancel
        //alarmService.cancelAlarm(requestCode = 123)
    }
}