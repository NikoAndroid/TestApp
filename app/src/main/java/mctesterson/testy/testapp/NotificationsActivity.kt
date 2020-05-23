package mctesterson.testy.testapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import mctesterson.testy.testapp.notifications.ChannelImportance
import mctesterson.testy.testapp.notifications.ChannelUtil
import mctesterson.testy.testapp.notifications.NotificationData
import mctesterson.testy.testapp.notifications.NotificationUtil
import mctesterson.testy.testapp.workmanager.MainWorker


class NotificationsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "NotificationsTag"
        private const val REFRESH_FREQ = 1000L
    }

    private val uiHandler = Handler()
    private var isForeground = false

    private lateinit var edittextChannelName: EditText
    private var vibrate: Boolean = false
    private var importance = ChannelImportance.DEFAULT

    private lateinit var edittextNotificationId: EditText
    private lateinit var edittextNotificationTag: EditText
    private lateinit var edittextNotificationTitle: EditText
    private lateinit var edittextNotificationMessage: EditText


    //private lateinit var mModel: CountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)


        edittextNotificationId = findViewById(R.id.edit_notification_id)
        edittextNotificationTag = findViewById(R.id.edit_notification_tag)
        edittextNotificationTitle = findViewById(R.id.edit_notification_title)
        edittextNotificationMessage = findViewById(R.id.edit_notification_message)
        edittextChannelName = findViewById(R.id.edit_channel_name)

        findViewById<Spinner>(R.id.channel_importance).apply {
            val items = ChannelImportance.values().map { it.name }
            val adapter = ArrayAdapter<String>(this@NotificationsActivity, android.R.layout.simple_spinner_dropdown_item, items)
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    importance = ChannelImportance.valueOf(parent?.getItemAtPosition(position) as String)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }

    fun createChannel(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelUtil.createChannel(applicationContext, edittextChannelName.text.toString(), importance, vibrate)
        } else {
            Toast.makeText(applicationContext, "Channels are only api 26+", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteChannel(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelUtil.deleteChannel(applicationContext, edittextChannelName.text.toString())
        } else {
            Toast.makeText(applicationContext, "Channels are only api 26+", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteAllChannels(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelUtil.deleteAllChannels(applicationContext)
        } else {
            Toast.makeText(applicationContext, "Channels are only api 26+", Toast.LENGTH_SHORT).show()
        }
    }

    fun onVibrationChange(view: View) {
        vibrate = (view as CheckBox).isChecked
    }

    fun openSystemSettings(view: View) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, applicationContext.packageName)
        startActivity(intent)
    }

    fun sendNotification(view: View) {
        val notificationId = edittextNotificationId.text.toString().toIntOrNull()
        if (notificationId == null) {
            Toast.makeText(this, "NotificationId must be an int", Toast.LENGTH_SHORT).show()
            return
        }
        NotificationUtil.buildAndSendNotification(applicationContext, NotificationData(
                notificationId = notificationId,
                notificationTag = edittextNotificationTag.text.toString(),
                channelId = ChannelUtil.getChannelId(edittextChannelName.text.toString()),
                title = edittextNotificationTitle.text.toString(),
                message = edittextNotificationMessage.text.toString()
        ))
    }

    override fun onResume() {
        isForeground = true

        uiHandler.postDelayed(object : Runnable {
            override fun run() {
                if (isForeground) {
                    WorkManagerCounterSingleton.getTotalEnqueued().postValue(MainWorker.getNumberWorksQueued(applicationContext))
                    uiHandler.postDelayed(this, REFRESH_FREQ)
                }
            }
        }, REFRESH_FREQ)
        super.onResume()
    }

    override fun onPause() {
        isForeground = false
        super.onPause()
    }
}
