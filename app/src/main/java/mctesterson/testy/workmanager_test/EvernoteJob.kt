package mctesterson.testy.workmanager_test

import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit

class EvernoteJob: Job() {

    companion object {
        const val TAG = "EvernoteJob"

        fun scheduleDailyJob() {
            Log.d(TAG, "scheduling evernote daily job")
            JobRequest.Builder(EvernoteJob.TAG)
                    .setUpdateCurrent(false)
                    .setPeriodic(TimeUnit.DAYS.toMillis(1))
                    .build()
                    .schedule()
        }
    }

    override fun onRunJob(params: Params): Result {
        Log.d(TAG, "executing evernote daily job")
        return Result.SUCCESS
    }

}