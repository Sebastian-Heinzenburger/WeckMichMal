package de.heinzenburger.g2_weckmichmal.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.Vibrator
import androidx.compose.ui.util.fastForEachReversed
import androidx.core.app.NotificationCompat
import de.heinzenburger.g2_weckmichmal.R
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmRingingScreen
import kotlin.concurrent.thread
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class ForegroundService : Service() {
    companion object{
        lateinit var event : Event
    }

    private var binder = MyBinder()

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        vibrator = getSystemService("vibrator") as Vibrator
    }

    fun playWithPerry() {

        val audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
        mediaPlayer?.apply {
            setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())

            isLooping = true

            setOnPreparedListener {
                start()
            }

            setOnCompletionListener {
                release()
            }

            setOnErrorListener { mp, what, extra ->
                mp.release()
                true
            }
        }


        /*vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 500, 950), // Pattern: wait 0ms, vibra,ute 500ms, pause 1000ms
                intArrayOf(0, 255, 0),
                1 // Repeat indefinitely
            )
        )

         */
    }

    fun sleepWithPerry(isForeverSleep : Boolean){
        thread {
            vibrator?.cancel()
            mediaPlayer?.stop()
            if(!isForeverSleep){
                Thread.sleep(300000)
                playWithPerry()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val configID = intent?.getLongExtra("configID",-1)
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
        vibrator?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    inner class MyBinder : Binder() {
        fun getService(): ForegroundService = this@ForegroundService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
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

        var deleteConfigurationReceiver = Intent(this, DeleteConfigurationReceiver::class.java)
        deleteConfigurationReceiver.action = "NOTIFICATION_DELETED"

        val pendingDeleteConfigurationReceiver = PendingIntent.getBroadcast(
                this, 0, deleteConfigurationReceiver, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "g2_weckmichmal")
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(bigText))
            .setSmallIcon(R.mipmap.ic_launcher_foreground) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .addAction(R.mipmap.ic_launcher_foreground, "Aufhören!",
                pendingDeleteConfigurationReceiver)
            .setDeleteIntent(pendingDeleteConfigurationReceiver)
            .setAutoCancel(true)
            .build()
    }
}

class DeleteConfigurationReceiver : BroadcastReceiver() {
    @Override
    override fun onReceive(context:Context, intent: Intent) {
        val core = Core(context)
        core.showToast("Alarm gestoppt")
        if ("NOTIFICATION_DELETED" == intent.action) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
            exitProcess(-1)
        }
    }
}