package mctesterson.testy.testapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import mctesterson.testy.testapp.NotificationsActivity

private const val TAG = NotificationsActivity.TAG


class NotificationDismissedReceiver : BroadcastReceiver() {
    companion object {
        private const val BROADCAST_PRIORITY = 100

        fun getIntentFilters(): IntentFilter {
            return IntentFilter().apply {
                priority = BROADCAST_PRIORITY
                addAction(ACTION_DISMISS_NOTIFICATION)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_DISMISS_NOTIFICATION == intent.action) {
            try {
                intent.extras?.let { extras ->
                    val notificationId = extras.getInt(EXTRA_NOTIFICATION_ID)
                    val isSummary = extras.getBoolean(EXTRA_IS_SUMMARY)
                    Log.i(TAG, "notification dismissed id=$notificationId isSummary=$isSummary")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while clearing notifications: ", e)
            }
        }
    }
}