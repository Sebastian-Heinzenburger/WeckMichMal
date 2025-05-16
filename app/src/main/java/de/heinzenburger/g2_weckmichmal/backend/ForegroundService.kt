package de.heinzenburger.g2_weckmichmal.backend

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.Vibrator
import androidx.compose.ui.util.fastForEachReversed
import androidx.core.app.NotificationCompat
import de.heinzenburger.g2_weckmichmal.R
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmRingingScreen
import kotlin.concurrent.thread
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ForegroundService : Service() {

    companion object{
        lateinit var event : Event
    }

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
        /*vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 500, 950), // Pattern: wait 0ms, vibrate 500ms, pause 1000ms
                intArrayOf(0, 255, 0),
                1 // Repeat indefinitely
            )
        )
         */
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val configID = intent?.getLongExtra("configID",-1)
        Logger(null).log(Logger.Level.INFO, "aaaaa Config ID $configID")

        val core = Core(applicationContext)



        thread {
            var configurationWithEvent: ConfigurationWithEvent? = null
            core.getAllConfigurationAndEvent()?.forEach {
                if(it.event?.configID == configID){
                    configurationWithEvent = it
                }
            }

            val notification = createNotification(configurationWithEvent)
            startForeground(1, notification)

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
        val name = "g2_weckmichmal"
        val descriptionText = "g2_weckmichmal"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("g2_weckmichmal", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }



    private fun createNotification(configurationWithEvent: ConfigurationWithEvent?): Notification {
        val intent = Intent(this, AlarmRingingScreen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val title = configurationWithEvent?.configuration?.name

        var course : Course? = null
        var timeBetweenCourseAndWakeUp = Long.MAX_VALUE
        configurationWithEvent?.event?.courses?.forEach {
            if(Duration.between(LocalDateTime.now(),it.startDate).seconds < timeBetweenCourseAndWakeUp){
                timeBetweenCourseAndWakeUp = Duration.between(LocalDateTime.now(),it.startDate).seconds
                course = it
            }
        }
        val text = if(course != null){
            "Bereit machen für ${course.name} um ${course.startDate.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        } else{
            "Aufwachen!"
        }

        var bigText = text

        configurationWithEvent?.event?.routes?.fastForEachReversed {
            bigText += "\nAbfahrt von ${it.startStation} um ${it.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        }


        return NotificationCompat.Builder(this, "g2_weckmichmal")
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(bigText))
            .setSmallIcon(R.mipmap.ic_launcher_foreground) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }
}