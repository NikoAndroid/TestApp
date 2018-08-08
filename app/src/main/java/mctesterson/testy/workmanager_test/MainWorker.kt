package mctesterson.testy.workmanager_test

import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import java.util.*
import java.util.concurrent.TimeUnit

class MainWorker: Worker() {

    companion object {
        private const val TAG = "MainWorker"
        const val WORK_TAG = "${TAG}Work"
        private var mCounter = 0


        fun submitNewWork(): UUID {
            val delay = 6L
            Log.d(TAG, "Submitting work delayed $delay seconds")
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val worker = OneTimeWorkRequest.Builder(MainWorker::class.java)
                    .setConstraints(constraints)
                    .addTag(WORK_TAG)
                    .setInitialDelay(delay, TimeUnit.SECONDS)
                    .build()
            WorkManager.getInstance().enqueue(worker)
            return worker.id
        }

        fun cancelWork() {
            Log.d(TAG, "Cancelling work")
            WorkManager.getInstance().cancelAllWorkByTag(WORK_TAG)
        }
    }


    override fun doWork(): Result {
        Log.d(TAG, "doing work")


        CounterSingleton.getCount().postValue(++mCounter)

        submitNewWork()
        return Result.SUCCESS
    }
}
