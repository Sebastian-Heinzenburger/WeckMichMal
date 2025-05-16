package de.heinzenburger.g2_weckmichmal.backend

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import de.heinzenburger.g2_weckmichmal.R
import de.heinzenburger.g2_weckmichmal.core.Core
import kotlin.concurrent.thread

class AlarmUpdater : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notification = NotificationCompat.Builder(context!!, "alarm_channel")
            .setContentTitle("Alarm")
            .setContentText("Sleeping")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)


        val core = Core(context)
        thread {
            core.runUpdateLogic()
        }
    }
}