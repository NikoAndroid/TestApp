package mctesterson.testy.testapp.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import mctesterson.testy.testapp.BuildConfig
import mctesterson.testy.testapp.R

data class NotificationData(
        val notificationId: Int,
        val notificationTag: String?,
        val channelId: String,
        val groupId: String?,
        val title: String,
        val message: String,
        val time: Long = System.currentTimeMillis(),
        val shouldGroup: Boolean = false
) {
    fun getCommonExtras(): Bundle {
        return Bundle().apply {
            putInt(EXTRA_NOTIFICATION_ID, notificationId)
            putString(EXTRA_CHANNEL_ID, channelId)
            putBoolean(EXTRA_IS_SUMMARY, false)
        }
    }
}

private const val PACKAGE = "${BuildConfig.APPLICATION_ID}.notification."
const val ACTION_OPEN_NOTIFICATION = PACKAGE + "ACTION_OPEN_NOTIFICATION"
const val ACTION_DISMISS_NOTIFICATION = PACKAGE + "ACTION_DISMISS_NOTIFICATION"
const val NOTIFICATION_MESSAGE_REPLY = PACKAGE + "NOTIFICATION_MESSAGE_REPLY"
const val EXTRA_NOTIFICATION_ID = "notificationId"
const val EXTRA_CHANNEL_ID = "channelId"
const val EXTRA_IS_SUMMARY = "isSummary"
const val EXTRA_NOTIFICATION_ACTION_ID = "actionId"

sealed class NotificationBuilderAction {
    abstract val id: String
    abstract val drawableRes: Int
    abstract val textRes: Int

    companion object {
        fun fromId(id: String?): NotificationBuilderAction? {
            return when (id) {
                ReplyAction.ID -> ReplyAction()
                else -> null
            }
        }
    }
}

data class ReplyAction(
        override val id: String = ID,
        override val drawableRes: Int = R.drawable.ic_reply,
        override val textRes: Int = R.string.reply
) : NotificationBuilderAction() {
    companion object {
        const val ID = "reply"
    }
}

object NotificationUtil {
    fun buildAndSendNotification(appContext: Context, notificationData: NotificationData, f: ((builder: NotificationCompat.Builder) -> Unit)? = null): Notification {
        val builder = getNotificationBuilder(appContext, notificationData)

        // allow caller to add additional things to builder
        f?.invoke(builder)

        val notification = builder.build()
        val notificationManager = NotificationManagerCompat.from(appContext) // using compat for compatibility with some wear devices on 4.4
        notificationManager.notify(notificationData.notificationTag, notificationData.notificationId, notification)
        return notification
    }

    fun buildAndSendGroupNotification(appContext: Context, notificationDatas: List<NotificationData>): Notification {
        val firstData = notificationDatas.first()
        val title = "Notification group (${firstData.groupId})"

        val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(title)

        notificationDatas.forEach {
            inboxStyle.addLine(it.title)
        }

        val groupNotificationData = firstData.copy(
                notificationId = firstData.groupId.hashCode(),
                title = title,
                message = ""
        )

        val notification = getNotificationBuilder(appContext, groupNotificationData)
                .setSubText("subtext")
                .setStyle(inboxStyle)
                .setWhen(notificationDatas.sortedByDescending { it.time }.first().time)
                .setNumber(notificationDatas.size)
                .setGroupSummary(true)
                .setOnlyAlertOnce(true)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .build()

        val notificationManager = NotificationManagerCompat.from(appContext) // using compat for compatibility with some wear devices on 4.4
        notificationManager.notify(groupNotificationData.notificationTag, groupNotificationData.notificationId, notification)
        return notification
    }

    fun addReplyToNotificationHistoryAndSend(appContext: Context, channelId: String, notificationId: Int, replyHistory: List<String>, f: ((builder: NotificationCompat.Builder) -> Unit)? = null): Notification {
//        val builder = getNotificationBuilder(appContext, notificationData)
        val msg = "Message sent"
        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_robber_white)
            .setTicker(msg) // accessibility
            .setContentTitle(msg)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .setOnlyAlertOnce(true)

//            .setRemoteInputHistory(replyHistory.toTypedArray())

        // allow caller to add additional things to builder
        f?.invoke(builder)

        val notification = builder.build()
        val notificationManager = NotificationManagerCompat.from(appContext) // using compat for compatibility with some wear devices on 4.4
        notificationManager.notify("", notificationId, notification)
        return notification
    }

    private fun getNotificationBuilder(appContext: Context, notificationData: NotificationData): NotificationCompat.Builder {
        val resources = appContext.resources
        val builder = NotificationCompat.Builder(appContext, notificationData.channelId)

        val commonExtras = notificationData.getCommonExtras()

        // Setup intent to signal us when user dismisses or clears all notifications
        val dismissIntent = Intent(ACTION_DISMISS_NOTIFICATION).apply {
            putExtras(commonExtras)
        }
        builder.setDeleteIntent(PendingIntent.getBroadcast(appContext, notificationData.notificationId, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT))

        // set up intent to signal us when user opens notification
        val openIntent = Intent(ACTION_OPEN_NOTIFICATION).apply {
            putExtras(commonExtras)
            setData(Uri.parse("testapp://notification"))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Rebuild the activity stack
        val stackBuilder = TaskStackBuilder.create(appContext)
        stackBuilder.addNextIntent(openIntent)
        val openPendingIntent = stackBuilder.getPendingIntent(notificationData.notificationId, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setStyle(NotificationCompat.BigTextStyle().bigText(notificationData.message))
                .setContentIntent(openPendingIntent)
                .setTicker("${notificationData.title}, ${notificationData.message}") // accessibility
                .setContentTitle(notificationData.title)
                .setContentText(notificationData.message)
                .setWhen(notificationData.time)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // before channels
                .setSmallIcon(R.drawable.ic_robber_white)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setLights(ContextCompat.getColor(appContext, R.color.notificationLED), 100, 100)
                .setColor(ContextCompat.getColor(appContext, R.color.notificationIconcolor))
                .setGroupSummary(false)

        notificationData.groupId?.let {
            builder.setGroup(it)
        }

        if (notificationData.shouldGroup) {
            builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY) // you can also always just set this and it disables alerts for individual messages without notifications
        }

        // Notification replies are only supported on N+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val replyAction = ReplyAction()
            val replyActionText = resources.getString(replyAction.textRes)
            val remoteInput = RemoteInput.Builder(NOTIFICATION_MESSAGE_REPLY).setLabel(replyActionText).build()
            val pendingIntent = NotificationActionService.getPendingIntent(appContext, replyAction.id, notificationData)

            val action = NotificationCompat.Action.Builder(R.drawable.ic_reply, replyActionText, pendingIntent)
                    .addRemoteInput(remoteInput).build()

            builder.addAction(action)
        }

        return builder
    }

    fun cancelNotification(appContext: Context, notificationId: Int, notificationTag: String?) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationTag, notificationId)
    }
}