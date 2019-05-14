package app.runchallenge.controller.provider.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.runchallenge.R
import app.runchallenge.view.activity.MainActivity
import kotlin.random.Random


class MyNotificationImpl(context: Context) : MyNotification {

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    override fun enableChannel() {
        notificationChannel?.let { notificationManagerCompat.createNotificationChannel(it) }
    }

    override fun disableChannel() {
        notificationManagerCompat.deleteNotificationChannel(channelId)
    }


    //Notification channel settings
    override val channelId: String = context.getString(R.string.channel_id)
    private val channelName = context.getString(R.string.channel_name)
    private val channelDescription = context.getString(R.string.channel_description)
    private val importance = NotificationManager.IMPORTANCE_DEFAULT

    //Notification  settings

    //Caution: The integer ID that you give to startForeground() must not be 0.
    override val notificationId = Random.nextInt(2, Int.MAX_VALUE - 1)
    private val notificationTitle = channelName
    private val notificationMessage = channelDescription
    private val notificationTickerText = context.getString(R.string.notification_ticker_text)

    //notification channel
    private val notificationChannel: NotificationChannel? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel(channelId, channelName, importance).apply {
            this.description = channelDescription
        }
    } else {
        null
    }

    //intent started when notification clicked
    private val pendingIntent: PendingIntent =
        Intent(context, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(context, 0, notificationIntent, 0)
        }

    override val notification: Notification = NotificationCompat.Builder(context, channelId)
        .setPriority(importance)
        .setContentTitle(notificationTitle)
        .setContentText(notificationMessage)
        .setContentIntent(pendingIntent)
        .setSmallIcon(R.drawable.ic_run)
        .setTicker(notificationTickerText)
        .setOngoing(true)
        .build()

}