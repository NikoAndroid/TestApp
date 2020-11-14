package mctesterson.testy.testapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mctesterson.testy.testapp.notifications.ChannelImportance
import mctesterson.testy.testapp.notifications.ChannelUtil
import mctesterson.testy.testapp.notifications.NotificationData
import mctesterson.testy.testapp.notifications.NotificationUtil
import java.io.File

class NotificationsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "NotificationsTag"
        const val INTENT_ACTION = "mctesterson.testy.testapp.NOTIFICATIONS"
    }

    private lateinit var edittextChannelName: EditText
    private lateinit var edittextChannelGroupName: EditText
    private var vibrate: Boolean = false
    private var importance = ChannelImportance.DEFAULT

    private lateinit var edittextNotificationId: EditText
    private lateinit var edittextNotificationTag: EditText
    private lateinit var edittextNotificationTitle: EditText
    private lateinit var edittextNotificationMessage: EditText
    private lateinit var edittextGroupId: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        edittextNotificationId = findViewById(R.id.edit_notification_id)
        edittextNotificationTag = findViewById(R.id.edit_notification_tag)
        edittextNotificationTitle = findViewById(R.id.edit_notification_title)
        edittextNotificationMessage = findViewById(R.id.edit_notification_message)
        edittextChannelName = findViewById(R.id.edit_channel_name)
        edittextChannelGroupName = findViewById(R.id.edit_channel_group_name)
        edittextGroupId = findViewById(R.id.edit_group_id) // notification group

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

        val channels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ChannelUtil.getChannelNames(applicationContext) else listOf()
        channels.firstOrNull()?.let {
            edittextChannelName.setText(it)
        }

        val channels2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ChannelUtil.getChannels(applicationContext) else listOf()
        channels2.forEach {
            val uri = it.sound!!
            val id = it.id
//            val exists = File(uri.toString()).exists()
            val resolver = application.contentResolver
//            resolver.//openFile(uri, "rw", null)
            val doc = DocumentFile.fromSingleUri(applicationContext, uri)!!
            val exists = doc.exists()
            Log.d(TAG, "${uri.toString()}  exists=$exists id=$id")
        }
    }

    fun createChannel(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelUtil.createChannel(applicationContext, edittextChannelName.text.toString(), importance, vibrate, edittextChannelGroupName.text.toString())
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

    fun deleteChannelGroup(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelUtil.deleteChannelGroup(applicationContext, edittextChannelGroupName.text.toString())
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

        val groupId = edittextGroupId.text.toString().let { if (it.isBlank()) null else it }
        NotificationUtil.buildAndSendNotification(applicationContext, NotificationData(
                notificationId = notificationId,
                notificationTag = edittextNotificationTag.text.toString(),
                groupId = groupId,
                channelId = ChannelUtil.getChannelId(edittextChannelName.text.toString()),
                title = edittextNotificationTitle.text.toString(),
                message = edittextNotificationMessage.text.toString(),
                shouldGroup = false
        ))
    }

    fun sendGroupedNotifications(view: View) {
        val notificationId = edittextNotificationId.text.toString().toIntOrNull()
        if (notificationId == null) {
            Toast.makeText(this, "NotificationId must be an int", Toast.LENGTH_SHORT).show()
            return
        }

        val groupId = edittextGroupId.text.toString().let { if (it.isBlank()) null else it }
        val notificationDatas = IntArray(4).mapIndexed { index, _ ->
            NotificationData(
                    notificationId = notificationId + index,
                    notificationTag = edittextNotificationTag.text.toString(),
                    groupId = groupId,
                    channelId = ChannelUtil.getChannelId(edittextChannelName.text.toString()),
                    title = edittextNotificationTitle.text.toString() + index,
                    message = edittextNotificationMessage.text.toString() + index
            )
        }

        val showingDatas = mutableListOf<NotificationData>()

        for (i in 0 until notificationDatas.size) {
            val data = notificationDatas[i].copy(
                    time = System.currentTimeMillis(),
                    shouldGroup = false //i != 0 // first one should alert, the others should not
            )
            showingDatas.add(data)

            when (i) {
                2, 3 -> {
                    Thread.sleep(3000)
                }
            }

            NotificationUtil.buildAndSendNotification(applicationContext, data)
            NotificationUtil.buildAndSendGroupNotification(applicationContext, showingDatas)
        }

    }
}
