package mctesterson.testy.testapp

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import mctesterson.testy.testapp.notifications.ChannelUtil
import mctesterson.testy.testapp.notifications.NotificationData
import mctesterson.testy.testapp.notifications.NotificationUtil


class IntentsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "IntentsTag"
    }

    private lateinit var channelNames: List<String>
    private var channelName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intents)

        channelNames = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ChannelUtil.getChannelNames(applicationContext) else listOf("CREATE A CHANNEL")
        channelName = channelNames.firstOrNull()

        findViewById<Spinner>(R.id.channels).apply {
            val adapter = ArrayAdapter<String>(this@IntentsActivity, android.R.layout.simple_spinner_dropdown_item, channelNames)
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    channelName = parent?.getItemAtPosition(position) as String
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }

    fun sendIntentThroughStackBuilder(view: View) {
        channelName?.let {
            NotificationUtil.buildAndSendNotification(applicationContext, NotificationData(
                    notificationId = 123,
                    notificationTag = null,
                    groupId = "groupId",
                    channelId = ChannelUtil.getChannelId(it),
                    title = "To dummy activity",
                    message = "",
                    shouldGroup = false
            )) { builder ->
                val activities: List<ResolveInfo> = packageManager.queryIntentActivities(
                        intent,
                        PackageManager.MATCH_DEFAULT_ONLY
                )
                val isIntentSafe: Boolean = activities.isNotEmpty()
                Log.d(TAG, "intent good = $isIntentSafe")

                val intent = Intent(DummyActivity.INTENT_ACTION).apply {
                    // if data is set with a custom schema, intent-filter but be set too
//                    setData(Uri.parse("testapp://somedata"))
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

                val stackBuilder = TaskStackBuilder.create(applicationContext)
                        .addParentStack(NotificationsActivity::class.java)
                        .addNextIntent(intent)
//                        .addNextIntentWithParentStack(intent)

                val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                builder.setContentIntent(pendingIntent)
            }
        }
    }

    fun sendExplicitIntent(view: View) {
        channelName?.let {
            NotificationUtil.buildAndSendNotification(applicationContext, NotificationData(
                    notificationId = 123,
                    notificationTag = null,
                    groupId = "groupId",
                    channelId = ChannelUtil.getChannelId(it),
                    title = "To dummy activity",
                    message = "",
                    shouldGroup = false
            )) { builder ->

                val intent = Intent(applicationContext, DummyActivity::class.java).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // Using stackbuilder with an explicit intent and just explicit intent by itself works great

                val stackBuilder = TaskStackBuilder.create(applicationContext)
//                        .addParentStack(NotificationsActivity::class.java)
//                        .addNextIntent(intent)
                        .addNextIntentWithParentStack(intent)

                val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                // For some reason, requestCode MUST be 0 to work as expected. If you change from 0, the old stack doesn't build properly for some reason and back press kills app
//                val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                builder.setContentIntent(pendingIntent)
            }
        }
    }

    fun sendImplicitIntent(view: View) {
        channelName?.let {
            NotificationUtil.buildAndSendNotification(applicationContext, NotificationData(
                    notificationId = 123,
                    notificationTag = null,
                    groupId = "groupId",
                    channelId = ChannelUtil.getChannelId(it),
                    title = "To dummy activity",
                    message = "",
                    shouldGroup = false
            )) { builder ->

                val intent = Intent(DummyActivity.INTENT_ACTION).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }

                // For some reason, requestCode MUST be 0 to work as expected. If you change from 0, the old stack doesn't build properly for some reason and back press kills app
                val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                builder.setContentIntent(pendingIntent)
            }
        }
    }

    fun sendMainActivityIntent(view: View) {
        channelName?.let {
            NotificationUtil.buildAndSendNotification(applicationContext, NotificationData(
                    notificationId = 123,
                    notificationTag = null,
                    groupId = "groupId",
                    channelId = ChannelUtil.getChannelId(it),
                    title = "To dummy activity",
                    message = "",
                    shouldGroup = false
            )) { builder ->

                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

                // For some reason, requestCode MUST be 0 to work as expected. If you change from 0, the old stack doesn't build properly for some reason and back press kills app
                val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                builder.setContentIntent(pendingIntent)
            }
        }
    }
}