package mctesterson.testy.workmanager_test

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(
                this,
                Configuration.Builder()
                        .setMinimumLoggingLevel(Log.VERBOSE)
                        .setMaxSchedulerLimit(50)
                        .build())
    }
}