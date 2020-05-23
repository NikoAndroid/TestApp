package mctesterson.testy.testapp.notifications

import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.RemoteInput
import mctesterson.testy.testapp.NotificationsActivity

private const val TAG = NotificationsActivity.TAG

class NotificationActionService : IntentService(TAG) {

    companion object {
        private const val ACTION = "com.yahoo.mail.flux.push.action"

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
        val action = NotificationBuilderAction.fromId(actionId)

        if (action != null) {
            when (action) {
                is ReplyAction -> {
                    val remoteInput = RemoteInput.getResultsFromIntent(intent)
                    if (remoteInput?.isEmpty == false) {
                        val input = remoteInput.getCharSequence(NOTIFICATION_MESSAGE_REPLY).toString()
                        Log.i(TAG, "Notification reply input: $input")
                    }
                }
            }
        }
    }
}