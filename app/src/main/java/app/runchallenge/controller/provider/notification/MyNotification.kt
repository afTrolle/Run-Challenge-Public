package app.runchallenge.controller.provider.notification

import android.app.Notification

interface MyNotification {
    val notificationId: Int
    val notification: Notification
    val channelId: String

    fun enableChannel()
    fun disableChannel()
}