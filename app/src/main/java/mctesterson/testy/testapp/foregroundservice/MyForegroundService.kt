package mctesterson.testy.testapp.foregroundservice

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mctesterson.testy.testapp.ForegroundServiceActivity
import mctesterson.testy.testapp.R
import java.lang.ref.WeakReference

private const val TAG = ForegroundServiceActivity.TAG

interface IForegroundService {
    fun handleStopMessage()
}

class MyForegroundService : Service(), IForegroundService {
    companion object {
        private const val MSG_STOP_SERVICE = 1
        private const val DEFAULT_SERVICE_TIMEOUT_MS = 0L
        private const val EXTRA_TIMEOUT_MS = "timeoutMs"
        private const val EXTRA_TITLE_RES = "titleRes"
        private const val EXTRA_SMALL_ICON_RES = "smallIconRes"
        private const val EXTRA_SMALL_ICON_COLOR_RES = "smallIconColorRes"
        private const val EXTRA_CHANNEL_ID = "channelId"

        private var instance: MyForegroundService? = null

        /**
         * Stop the service through app context
         */
        fun stopService(application: Application) {
            Log.d(TAG, "Stopping service through app context")
            application.stopService(getIntent(application))
        }

        /**
         * Stop the service self through our singleton instance
         */
        fun stopSelf() {
            instance?.let {
                Log.d(TAG, "Stopping self")
                it.stopSelf()
                it.stopForeground(true)
            }
        }

        fun startService(application: Application, options: Options) {
            val intent = getIntent(application).apply {
                putExtras(options.toBundle())
            }
            // need to also check channel here before starting service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                application.startForegroundService(intent)
                application.startService(intent)

            } else {
                // make sure not to start in background on oreo and up
                application.startService(intent)
            }
        }

        private fun getIntent(application: Application) = Intent(application, MyForegroundService::class.java)
    }

    open class Options(
            val notificationTitleRes: Int = R.string.foregroundNotification, // title of foreground notification
            val smallIconRes: Int = R.drawable.ic_robber_white,
            val smallIconColorRes: Int? = R.color.notificationIconcolor2,
            val channelId: String, // Channel id to use for foreground notification
            val serviceTimeoutMs: Long = DEFAULT_SERVICE_TIMEOUT_MS, // timeout until service should be stopped. Ideally, the service should be stopped after whatever task you are doing is completed
            val pendingIntentExtras: Bundle? = null // extras to pass through pending intent for when clicking foreground notification
    ) {
        fun toBundle() = Bundle().apply {
            putInt(EXTRA_TITLE_RES, notificationTitleRes)
            putInt(EXTRA_SMALL_ICON_RES, smallIconRes)
            putInt(EXTRA_SMALL_ICON_COLOR_RES, smallIconColorRes ?: 0)
            putString(EXTRA_CHANNEL_ID, channelId)
            putLong(EXTRA_TIMEOUT_MS, serviceTimeoutMs)
            pendingIntentExtras?.let { putAll(it) }
        }

        companion object {
            fun fromBundle(bundle: Bundle?): Options? {
                if (bundle == null) {
                    return null
                }

                val copy = Bundle(bundle)
                val title = copy.getInt(EXTRA_TITLE_RES)
                copy.remove(EXTRA_TITLE_RES)
                val smallIconRes = copy.getInt(EXTRA_SMALL_ICON_RES)
                copy.remove(EXTRA_SMALL_ICON_RES)
                val smallIconColorRes = copy.getInt(EXTRA_SMALL_ICON_COLOR_RES)
                copy.remove(EXTRA_SMALL_ICON_COLOR_RES)
                val channelId = copy.getString(EXTRA_CHANNEL_ID)
                copy.remove(EXTRA_CHANNEL_ID)
                val timeoutMs = copy.getLong(EXTRA_TIMEOUT_MS)
                copy.remove(EXTRA_TIMEOUT_MS)

                return channelId?.let { Options(
                        notificationTitleRes = title,
                        smallIconRes = smallIconRes,
                        smallIconColorRes = if (smallIconColorRes != 0) smallIconColorRes else null,
                        channelId = channelId,
                        serviceTimeoutMs = timeoutMs,
                        pendingIntentExtras = if (!copy.isEmpty) copy else null
                ) }
            }
        }
    }

    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()

        val notification = buildForegroundNotification(Options(
            notificationTitleRes = R.string.reply,
            channelId = "b"
        ))
        startForeground(1, notification)

        handler = StopServiceHandler(this)
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand intent=$intent flags=$flags startId=$startId")

        val options = Options.fromBundle(intent?.extras)
        if (options == null) {
            Log.w(TAG, "intent null or invalid options")
            return START_NOT_STICKY
        }

        // No need for foreground service below Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "starting foreground")
            GlobalScope.launch {
                delay(15000)
                startForeground(1, buildForegroundNotification(options))

            }
        }

        handler.removeMessages(MSG_STOP_SERVICE)
        if (options.serviceTimeoutMs > 0) {
            handler.sendEmptyMessageDelayed(MSG_STOP_SERVICE, options.serviceTimeoutMs)
        }

        // If we get killed, no need to restart service unless more intents to handle
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
        Log.d(TAG, "destroyed")
    }

    override fun handleStopMessage() {
        Log.d(TAG, "force stopping from timer")
        stopSelf()
    }

    class StopServiceHandler(service: IForegroundService) : Handler() {
        val service: WeakReference<IForegroundService>

        init {
            this.service = WeakReference(service)
        }

        override fun handleMessage(msg: Message) {
            service.get()?.handleStopMessage()
        }
    }

    private fun buildForegroundNotification(options: Options): Notification {
        val pendingIntent: PendingIntent = Intent(this, ForegroundServiceActivity::class.java).let { notificationIntent ->
            options.pendingIntentExtras?.let { notificationIntent.putExtras(it) }
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val title = getString(options.notificationTitleRes)
        val builder = NotificationCompat.Builder(this, options.channelId)
                .setContentTitle(title)
                .setTicker(title)
                .setSmallIcon(options.smallIconRes)
                .setContentIntent(pendingIntent)
                .setUsesChronometer(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)

        options.smallIconColorRes?.let {
            builder.color = ContextCompat.getColor(applicationContext, it)
        }

        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}