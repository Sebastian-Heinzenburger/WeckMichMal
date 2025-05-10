package de.heinzenburger.g2_weckmichmal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import java.time.Duration
import java.time.LocalDateTime
import kotlin.concurrent.thread

class ForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        vibrator = getSystemService("vibrator") as Vibrator
    }

    fun playWithPerry() {
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        thread {
            playWithPerry()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayer resources
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "foreground_service_channel",
            "Foreground Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "foreground_service_channel")
            .setContentTitle("Foreground Service")
            .setContentText("The service is running in the foreground.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .build()
    }
}