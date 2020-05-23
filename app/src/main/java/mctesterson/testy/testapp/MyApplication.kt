package mctesterson.testy.testapp

import android.app.Application
import com.evernote.android.job.JobManager
import mctesterson.testy.testapp.notifications.NotificationDismissedReceiver
import mctesterson.testy.testapp.workmanager.EvernoteJobCreator


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