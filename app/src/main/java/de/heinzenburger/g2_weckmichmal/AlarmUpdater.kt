package de.heinzenburger.g2_weckmichmal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockOverviewScreen
import java.time.LocalDateTime
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
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(1, notification)


        val core = Core(context)
        thread {
            core.runUpdateLogic()
        }
    }
}