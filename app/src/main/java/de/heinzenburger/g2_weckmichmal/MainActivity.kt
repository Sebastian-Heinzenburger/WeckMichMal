package de.heinzenburger.g2_weckmichmal

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockOverviewScreen
import de.heinzenburger.g2_weckmichmal.ui.screens.WelcomeScreen
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.logging.Logger
import kotlin.concurrent.thread
import android.app.AlarmManager
import android.net.Uri
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val core = Core(context = applicationContext)
        core.log(Logger.Level.INFO, "Starting Application")

        core.scheduleAndroidAlarm(LocalDateTime.now().plusSeconds(20))

        //val serviceIntent = Intent(this, ForegroundService::class.java)
        //startService(serviceIntent)

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