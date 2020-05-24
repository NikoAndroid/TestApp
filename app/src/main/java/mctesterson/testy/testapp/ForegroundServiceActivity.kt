package mctesterson.testy.testapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import mctesterson.testy.testapp.foregroundservice.MyForegroundService
import mctesterson.testy.testapp.notifications.ChannelUtil
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicBoolean


class ForegroundServiceActivity : AppCompatActivity() {

    companion object {
        const val TAG = "FGServiceTag"
    }

    var isRunning = AtomicBoolean(false)
        private set
    private lateinit var channelNames: List<String>
    private var channelName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foreground_service)

        channelNames = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ChannelUtil.getChannelNames(applicationContext) else listOf("CREATE A CHANNEL")
        channelName = channelNames.firstOrNull()

        findViewById<Spinner>(R.id.channels).apply {
            val adapter = ArrayAdapter<String>(this@ForegroundServiceActivity, android.R.layout.simple_spinner_dropdown_item, channelNames)
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

    fun stopService(view: View) {
        MyForegroundService.stopService(application)
//        MyForegroundService.stopSelf()
    }

    fun startService(view: View) {
        val channelId = ChannelUtil.getChannelId(channelName ?: "")
        MyForegroundService.startService(application, MyForegroundService.Options(channelId = channelId))
    }

    fun startServiceWithTimer(view: View) {
        val channelId = ChannelUtil.getChannelId(channelName ?: "")
        MyForegroundService.startService(application, MyForegroundService.Options(channelId = channelId, serviceTimeoutMs = 5000L))

        // do things in the background
        Thread {
            val duration = 10000L
            val wakelock = acquireWakeLock(applicationContext)
            Log.d(TAG, "Background thread started for $duration ms")
            Thread.sleep(duration)
            Log.d(TAG, "Background thread finished")
            releaseWakeLock(wakelock)
        }.start()
    }
}