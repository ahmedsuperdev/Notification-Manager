package com.example.utilities.notificationManager

import android.app.NotificationManager
import android.app.PendingIntent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat


typealias NotiFunType = NotificationCompat.Builder.() -> Unit




enum class NotiImportance(
    val importance: Int,
    val priority: Int
) {
    UNSPECIFIED(NotificationManager.IMPORTANCE_UNSPECIFIED, NotificationCompat.PRIORITY_MIN),
    NONE(NotificationManager.IMPORTANCE_NONE, NotificationCompat.PRIORITY_MIN),
    MIN(NotificationManager.IMPORTANCE_MIN, NotificationCompat.PRIORITY_MIN),
    LOW(NotificationManager.IMPORTANCE_LOW, NotificationCompat.PRIORITY_LOW),
    DEFAULT(NotificationManager.IMPORTANCE_DEFAULT, NotificationCompat.PRIORITY_DEFAULT),
    MAX(NotificationManager.IMPORTANCE_MAX, NotificationCompat.PRIORITY_MAX),
}


data class IconData(
    val resId: Int,
    val description: String = ""
)

data class SoundData(
    val uri: Uri?,
    val audioAttributes: AudioAttributes?,
    val description: String = ""
)



enum class ForegroundBehavior(val value: Int) {
    FOREGROUND_SERVICE_DEFAULT(NotificationCompat.FOREGROUND_SERVICE_DEFAULT),
    FOREGROUND_SERVICE_IMMEDIATE(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE),
    FOREGROUND_SERVICE_DEFERRED(NotificationCompat.FOREGROUND_SERVICE_DEFERRED),
}


object NotiTypes {
    fun countDown(
        endTime: Long
    ): NotiFunType = {
        setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(endTime)
    }

    fun countUp(
        startTime: Long = System.currentTimeMillis()
    ): NotiFunType = {
        setUsesChronometer(true)
            .setWhen(startTime)
    }

    fun pipeline(transformations: List<NotiFunType>): NotiFunType = {
        transformations.fold(this) { currResult, function -> currResult.apply(function) }
    }

}


data class NotiData(
    val title: String = "",
    val content: String = "",
    val iconData: IconData = IconData(android.R.drawable.ic_dialog_info),
    val contentIntent: PendingIntent? = null,
    val soundData: SoundData = SoundData(null, null),
    val autoCancel: Boolean = true, // Usually desired when clicking
    val onGoing: Boolean = false,
    val alertOnce: Boolean = true,
    val foregroundBehavior: ForegroundBehavior = ForegroundBehavior.FOREGROUND_SERVICE_DEFAULT,
    val specialType: NotiFunType = { this }
)
