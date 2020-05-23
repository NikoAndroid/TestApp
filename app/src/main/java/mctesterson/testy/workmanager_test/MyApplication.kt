package mctesterson.testy.workmanager_test

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
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