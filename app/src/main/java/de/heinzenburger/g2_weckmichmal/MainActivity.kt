package de.heinzenburger.g2_weckmichmal

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockOverviewScreen
import de.heinzenburger.g2_weckmichmal.ui.screens.WelcomeScreen

class MainActivity : ComponentActivity() {
    lateinit var core: Core
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        core = Core(context = applicationContext)
        core.log(Logger.Level.INFO, "Starting Application")

        /*val serviceIntent = Intent(applicationContext, ForegroundService::class.java)
        serviceIntent.putExtra("configID",intent?.getLongExtra("configID",-1))
        applicationContext.startService(serviceIntent)*/

        if (core.isApplicationOpenedFirstTime()) {
            val intent = Intent(applicationContext, WelcomeScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            checkForPermissions()
            val intent = Intent(applicationContext, AlarmClockOverviewScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
    }

    fun checkForPermissions(){
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        if(!alarmManager.canScheduleExactAlarms()){
            core.showToast("Bitte erlaube Alarm Erstellung in den Systemeinstellungen")
        }
        if(!powerManager.isIgnoringBatteryOptimizations(packageName)){
            core.showToast("Bitte erlaube uneingeschränkte Hintergrundnutzung in den 'battery optimization' Einstellungen")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                core.showToast("Bitte erlaube Notifications in den Systemeinstellungen")
            }
        }
    }


    /*
    fun checkForPermissions() : Boolean{
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        if(!alarmManager.canScheduleExactAlarms()){
            core.showToast("Bitte erlaube Alarm Erstellung in den Systemeinstellungen")
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                core.showToast("Bitte erlaube Notifications in den Systemeinstellungen")
            }
            return false
        }
        if(!powerManager.isIgnoringBatteryOptimizations(packageName)){
            core.showToast("Bitte deaktiviere battery optimization für diese App")
        }
        return true
    }
     */

}