package de.heinzenburger.g2_weckmichmal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.time.Duration
import java.time.LocalDateTime
import kotlin.concurrent.thread

class ForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        // Initialize and start playing sound

        // Simulate a long-running task
        thread {
            while (Duration.between(LocalDateTime.now(), LocalDateTime.of(2025, 5, 3, 8, 0))
                    .toMinutes() > 0
            ) {
                Thread.sleep(30 * 1000)
                MainActivity.log.severe("I am running in the foreground")
            }
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
            mediaPlayer?.start()
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