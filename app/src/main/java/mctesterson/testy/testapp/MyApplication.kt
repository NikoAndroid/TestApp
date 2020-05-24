package mctesterson.testy.testapp

import android.app.Application
import android.content.Context
import android.os.PowerManager
import androidx.core.content.ContextCompat.getSystemService
import com.evernote.android.job.JobManager
import mctesterson.testy.testapp.notifications.NotificationDismissedReceiver
import mctesterson.testy.testapp.workmanager.EvernoteJobCreator

fun acquireWakeLock(appContext: Context): PowerManager.WakeLock {
    return (appContext.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
        newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TestApp::WakeLockTag").apply {
            acquire(120000L)
        }
    }
}

fun releaseWakeLock(wakeLock: PowerManager.WakeLock) {
    if (wakeLock.isHeld) {
        wakeLock.release()
    }
}

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        JobManager.create(applicationContext).addJobCreator(EvernoteJobCreator())
//        WorkManager.initialize(
//                this,
//                Configuration.Builder()
//                        .setMinimumLoggingLevel(Log.VERBOSE)
//                        .setMaxSchedulerLimit(20)
//                        .build())

        applicationContext.registerReceiver(NotificationDismissedReceiver(), NotificationDismissedReceiver.getIntentFilters())
    }
}