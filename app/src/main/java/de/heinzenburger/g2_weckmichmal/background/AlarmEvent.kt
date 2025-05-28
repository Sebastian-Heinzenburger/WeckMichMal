package de.heinzenburger.g2_weckmichmal.background

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import de.heinzenburger.g2_weckmichmal.R
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import java.time.LocalDate
import kotlin.concurrent.thread


class AlarmEvent : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notification = NotificationCompat.Builder(context!!, "alarm_channel")
            .setContentTitle("Alarm")
            .setContentText("Wake up!")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)

        val uid = intent?.getLongExtra("configID",-1)

        val serviceIntent = Intent(context, ForegroundService::class.java)
        serviceIntent.putExtra("configID", uid)
        context.startService(serviceIntent)

        val core = Core(context)
        core.log(Logger.Level.SEVERE, "I rang hehe!")

        thread {
            if(uid != null && uid > -1){
                core.updateConfigurationIchHabGeringt(LocalDate.now(), uid)
            }
            core.runUpdateLogic()
        }
    }
}