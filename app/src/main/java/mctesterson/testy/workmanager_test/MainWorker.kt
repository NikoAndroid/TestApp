package mctesterson.testy.workmanager_test

import android.app.job.JobScheduler
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class MainWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    companion object {
        private const val TAG = "MainWorker"
        const val WORK_TAG = "${TAG}Work"
        private var totalWorkExecuted = 0


        fun submitNewWork(appContext: Context, shouldDelay: Boolean) {
            val delay = 1L // 1 day
            Log.d(TAG, "Submitting work delayed $delay seconds")
            val constraints = Constraints.Builder()
                    .build()

            val worker = OneTimeWorkRequest.Builder(MainWorker::class.java)
                    .setConstraints(constraints)
                    .addTag(WORK_TAG)

            if (shouldDelay) {
                worker.setInitialDelay(delay, TimeUnit.DAYS)
            }
            WorkManager.getInstance().enqueue(worker.build())
            CounterSingleton.getTotalEnqueued().postValue(getNumberWorksQueued(appContext))
        }

        fun submitPeriodicWork(appContext: Context) {
            Log.d(TAG, "Submitting periodic work")

            val worker = PeriodicWorkRequest.Builder(MainWorker::class.java, 1, TimeUnit.DAYS)
                    .addTag(WORK_TAG)
            WorkManager.getInstance().enqueue(worker.build())
            CounterSingleton.getTotalEnqueued().postValue(getNumberWorksQueued(appContext))
        }

        fun cancelWork() {
            Log.d(TAG, "Cancelling work")
            WorkManager.getInstance().cancelAllWorkByTag(WORK_TAG)
        }

        fun getNumberWorksQueued(appContext: Context): Int {
            val jobScheduler = appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            return jobScheduler.allPendingJobs.count()
        }
    }


    override fun doWork(): Result {
        Log.d(TAG, "doing work")

        CounterSingleton.getTotalExecuted().postValue(++totalWorkExecuted)
        CounterSingleton.getTotalEnqueued().postValue(getNumberWorksQueued(applicationContext))
        return Result.SUCCESS
    }
}
