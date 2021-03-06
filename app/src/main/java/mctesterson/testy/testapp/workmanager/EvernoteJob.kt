package mctesterson.testy.testapp.workmanager

import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit

class EvernoteJob: Job() {

    companion object {
        const val TAG = "EvernoteJob"

        fun scheduleDailyJob() {
            Log.d(TAG, "scheduling evernote daily job")
            JobRequest.Builder(TAG)
                    .setUpdateCurrent(false)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                    .build()
                    .schedule()
        }
    }

    override fun onRunJob(params: Params): Result {
        Log.d(TAG, "executing evernote daily job")
        return Result.SUCCESS
    }

}