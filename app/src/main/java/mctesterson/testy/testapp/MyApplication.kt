package mctesterson.testy.testapp

import android.app.Application
import com.evernote.android.job.JobManager


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
    }
}