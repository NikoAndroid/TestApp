package mctesterson.testy.testapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import mctesterson.testy.testapp.NotificationsActivity


private const val TAG = NotificationsActivity.TAG

enum class ChannelImportance(val value: Int) {
    DEFAULT(NotificationManager.IMPORTANCE_DEFAULT),
    NONE(NotificationManager.IMPORTANCE_NONE),
    MIN(NotificationManager.IMPORTANCE_MIN),
    LOW(NotificationManager.IMPORTANCE_LOW),
    HIGH(NotificationManager.IMPORTANCE_HIGH)
}


object ChannelUtil {
    fun getChannelId(channelName: String): String {
        return channelName.replace(" ", "")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(appContext: Context, channelName: String, channelImportance: ChannelImportance, vibrate: Boolean) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = getChannelId(channelName)
        val notificationChannel = NotificationChannel(channelId, channelName, channelImportance.value).apply {
            enableLights(true)
            lightColor = ContextCompat.getColor(appContext, android.R.color.holo_red_dark)
            enableVibration(vibrate)
            description = "$channelName description"
//        channel.sound.getResourceUri(context)?.let {
//            setSound(it, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
//        }

        }

        notificationManager.createNotificationChannel(notificationChannel)

        Log.d(TAG, "Created notification channelId=$channelId name=$channelName importance=$channelImportance vibrate=$vibrate")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteChannel(appContext: Context, channelName: String) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getChannelId(channelName)
        notificationManager.deleteNotificationChannel(channelId)

        Log.d(TAG, "Deleted notification channelId=$channelId name=$channelName")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteAllChannels(appContext: Context) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notificationChannels.forEach {
            notificationManager.deleteNotificationChannel(it.id)
        }

        Log.d(TAG, "Deleted all channels")
    }


    fun getChannels(appContext: Context): List<NotificationChannel> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notificationChannels
        } else emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getChannelNames(appContext: Context): List<String> {
        return getChannels(appContext).map { it.name.toString() }
    }
}