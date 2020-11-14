package mctesterson.testy.testapp.notifications

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mctesterson.testy.testapp.NotificationsActivity

private const val TAG = NotificationsActivity.TAG

class NotificationActionService : IntentService(TAG) {

    companion object {
        private const val ACTION = "com.yahoo.mail.flux.push.action"

        // map of notification Id to list of replies
        val replies: MutableMap<Int, List<String>> = mutableMapOf()

        fun getPendingIntent(appContext: Context, actionId: String, notificationData: NotificationData): PendingIntent {
            val intent = Intent(appContext, NotificationActionService::class.java).apply {
                action = ACTION
                data = Uri.parse("testapp://notificationaction")
                putExtras(notificationData.getCommonExtras())
                putExtra(EXTRA_NOTIFICATION_ACTION_ID, actionId)
            }
            return PendingIntent.getService(appContext, actionId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent intent=$intent")

        if (intent?.action != ACTION) {
            return
        }

        val actionId = intent.getStringExtra(EXTRA_NOTIFICATION_ACTION_ID)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val action = NotificationBuilderAction.fromId(actionId)

        if (action != null) {
            when (action) {
                is ReplyAction -> {
                    val remoteInput = RemoteInput.getResultsFromIntent(intent)
                    if (remoteInput?.isEmpty == false && notificationId != 0) {
                        val input = remoteInput.getCharSequence(NOTIFICATION_MESSAGE_REPLY).toString()
                        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)!!
                        val repliesForNotif = (replies[notificationId] as? MutableList) ?: mutableListOf()
                        repliesForNotif.add(0, input)

                        Log.i(TAG, "Notification reply input: $input, allReplies=$repliesForNotif")

//                        NotificationUtil.cancelNotification(applicationContext, notificationId, "")
                        NotificationUtil.addReplyToNotificationHistoryAndSend(applicationContext, channelId, notificationId, repliesForNotif)

                        GlobalScope.launch {
                            delay(5000)
                            NotificationUtil.cancelNotification(applicationContext, notificationId, "")
                        }
//                        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                        notificationManager.activeNotifications
                    }
                }
            }
        }
    }
}