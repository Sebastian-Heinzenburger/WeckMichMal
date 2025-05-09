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

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notification = NotificationCompat.Builder(context!!, "alarm_channel")
            .setContentTitle("Alarm")
            .setContentText("Wake up!")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(1, notification)

        // play music
        val mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
        val vibrator = getSystemService(context, Vibrator::class.java)

        MainActivity.log.severe("Vibrator: $vibrator")

        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            vibrator?.cancel()
        }
        vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 500, 950), // Pattern: wait 0ms, vibrate 500ms, pause 1000ms
                intArrayOf(0, 255, 0),
                1 // Repeat indefinitely
            )
        )

        val core = Core(context)

        val secondIntent = Intent(context, AlarmClockOverviewScreen::class.java)
        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(secondIntent)

        core.scheduleAndroidAlarm(LocalDateTime.now().plusMinutes(1))

    }
}