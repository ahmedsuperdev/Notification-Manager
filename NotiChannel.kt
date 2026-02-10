package com.example.utilities

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.app.Service
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.utilities.notificationManager.IntentFunctions
import com.example.utilities.notificationManager.NotiData
import com.example.utilities.notificationManager.NotiImportance
import kotlin.random.Random





@SuppressLint("InlinedApi")
enum class ForegroundServiceType(
    val value: Int
) {
    // FOREGROUND_SERVICE_TYPE_NONE(ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE), // Deprecated
    FOREGROUND_SERVICE_TYPE_HEALTH(ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH),
    FOREGROUND_SERVICE_TYPE_CAMERA(ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA),
    FOREGROUND_SERVICE_TYPE_LOCATION(ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION),
    FOREGROUND_SERVICE_TYPE_MANIFEST(ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST),
    FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE(ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE),
    FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION),
    FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING),
    FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK),
    FOREGROUND_SERVICE_TYPE_PHONE_CALL(ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL),
    FOREGROUND_SERVICE_TYPE_SHORT_SERVICE(ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE),
    FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING(ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING),
    FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED(ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED),
    FOREGROUND_SERVICE_TYPE_MICROPHONE(ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE),
    FOREGROUND_SERVICE_TYPE_DATA_SYNC(ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC),
    FOREGROUND_SERVICE_TYPE_SPECIAL_USE(ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE),
}

data class NotiChannelData(
    val channelId: String,
    val displayName: String = channelId,
    val description: String = "",
    val importance: NotiImportance = NotiImportance.DEFAULT,
    val notiData: NotiData = NotiData.default
) {
    fun createChannel(context: Context) = NotiChannel.createChannel(context, this@NotiChannelData)
    fun Context.createChannel() = NotiChannel.createChannel(this@createChannel, this@NotiChannelData)
}


class NotiChannel private constructor(
    val channelStrId: String,
    val displayName: String,
    val description: String,
    val importance: NotiImportance,
    val notiData: NotiData
) {

    companion object {
        fun createChannel(
            context: Context,
            notiChannelData: NotiChannelData
        ): NotiChannel {
            return NotiChannel(
                channelStrId = notiChannelData.channelId,
                displayName = notiChannelData.displayName,
                description = notiChannelData.description,
                importance = notiChannelData.importance,
                notiData = notiChannelData.notiData
            ).apply { registerChannel(context) }
        }
    }




    private fun registerChannel(
        context: Context
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelStrId,
                displayName,
                importance.importance
            ).apply {
                description = this@NotiChannel.description
                setSound(notiData.soundData.uri, notiData.soundData.audioAttributes)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    fun createNotification(
        notiId: Int = Random.nextInt(),
        instanceNotiData: NotiData? = null
    ): Noti = Noti(notiId, instanceNotiData)


    inner class Noti(
        val notiId: Int = Random.nextInt(),
        instanceNotiData: NotiData? = null
    ) {
        private var _instanceNotiData = instanceNotiData ?: notiData



        constructor(nameId: String, instanceNotiData: NotiData? = null):
                this(nameId.hashCode(), instanceNotiData)


        private fun build(
            context: Context
        ): android.app.Notification = _instanceNotiData.run {
            val finalIntent = contentIntent ?: IntentFunctions.getOpenAppPendingIntent(context)
            return NotificationCompat.Builder(
                context,
                channelStrId
            )
                .setSmallIcon(iconData.resId)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(finalIntent)
                .setAutoCancel(autoCancel)
                .setPriority(importance.priority)
                .setOngoing(onGoing)
                .setOnlyAlertOnce(alertOnce)
                .setForegroundServiceBehavior(foregroundBehavior.value)
                .apply(specialType)
                .build()
        }
        fun Context.show() = show(this)
        fun show(
            context: Context
        ): Noti = apply {
            if (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat
                    .checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notiId, build(context))
            }
        }

        fun showForeground(
            service: Service,
            foregroundServiceType: ForegroundServiceType
        ): Noti = apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                service.startForeground(notiId, build(service), foregroundServiceType.value)
            } else {
                service.startForeground(notiId, build(service))
            }
        }

        fun updateData(
            notiData: NotiData
        ): Noti = apply {
            _instanceNotiData = notiData
        }


        fun Context.dismiss() = dismiss(this)
        fun dismiss(
            context: Context
        ): Noti = apply {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notiId)
        }
    }

}




