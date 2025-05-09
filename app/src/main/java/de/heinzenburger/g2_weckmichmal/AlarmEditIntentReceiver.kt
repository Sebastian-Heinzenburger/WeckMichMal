package de.heinzenburger.g2_weckmichmal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlarmEditIntentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        MainActivity.log.severe("Alarm Edit Receiver triggered")
        val notification = NotificationCompat.Builder(context!!, "alarm_channel")
            .setContentTitle("Alarm Edit Receiver")
            .setContentText("Alarm Edit Receiver here :)")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(1, notification)
    }
}