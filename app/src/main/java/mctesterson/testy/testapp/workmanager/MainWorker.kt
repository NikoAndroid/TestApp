package mctesterson.testy.testapp.workmanager

import android.annotation.SuppressLint
import android.app.job.JobScheduler
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.evernote.android.job.JobManager
import mctesterson.testy.testapp.WorkManagerCounterSingleton
import java.util.UUID
import java.util.concurrent.TimeUnit

class MainWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    companion object {
        private const val TAG = "MainWorker"
        const val WORK_TAG = "${TAG}Work"
        const val WORK_TAG_PERIODIC = "${TAG}Work_periodic"
        private const val BS_TAG = "some_bs_tag"
        private const val BS_TAG2 = "other_bs_tag"
        private var totalWorkExecuted = 0


        @SuppressLint("RestrictedApi")
        fun submitNewWork(appContext: Context, shouldDelay: Boolean) {
            val delay = 30L // seconds
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val worker = OneTimeWorkRequest.Builder(MainWorker::class.java)
                    .setConstraints(constraints)
                    .setInputData(Data.Builder().putString("yo", "sdlakfjhblskadjghljk saghlkjsdhagk ljfhagl kjdlksjb lxkjvnludngljksf gklsjdhlkjdf glksdlakfjhblskadjghljk saghlkjsdhagk ljfhagl kjdlksjb lxkjvnludngljksf gklsjdhlkjdf glksdlakfjhblskadjghljk saghlkjsdhagk ljfhagl kjdlksjb lxkjvnludngljksf gklsjdhlkjdf glksdlakfjhblskadjghljk saghlkjsdhagk ljfhagl kjdlksjb lxkjvnludngljksf gklsjdhlkjdf glksdlakfjhblskadjghljk saghlkjsdhagk ljfhagl kjdlksjb lxkjvnludngljksf gklsjdhlkjdf glk").build())
                    .addTag(BS_TAG2)
                    .addTag(WORK_TAG)
                    .addTag(BS_TAG)

            if (shouldDelay) {
                Log.d(TAG, "Submitting work delayed $delay seconds")
                worker.setInitialDelay(delay, TimeUnit.SECONDS)
            } else {
                Log.d(TAG, "Submitting work with no delay")
            }
            val builtWorker = worker.build()

            Log.d("NIKO", "enqueing worker with id=${builtWorker.id}  other id=${builtWorker.workSpec.id}")
            WorkManager.getInstance().enqueue(builtWorker)
//            WorkManager.getInstance().beginUniqueWork(WORK_TAG, ExistingWorkPolicy.KEEP, worker.build()).enqueue()
            WorkManagerCounterSingleton.getTotalEnqueued().postValue(getNumberWorksQueued(appContext))
        }

        @SuppressLint("RestrictedApi")
        fun submitPeriodicWork(appContext: Context) {
            Log.d(TAG, "Submitting periodic work")

            val worker = PeriodicWorkRequest.Builder(MainWorker::class.java, 1, TimeUnit.DAYS)
                    .addTag(WORK_TAG_PERIODIC)
                    .addTag(WORK_TAG)
            val builtWorker = worker.build()

            Log.d("NIKO", "enqueing worker with id=${builtWorker.id}  other id=${builtWorker.workSpec.id}")
            WorkManager.getInstance().enqueue(builtWorker)
            WorkManagerCounterSingleton.getTotalEnqueued().postValue(getNumberWorksQueued(appContext))
        }

        fun cancelWork() {
            Log.d(TAG, "Cancelling work")
            WorkManager.getInstance().cancelAllWorkByTag(WORK_TAG)
        }

        // .className.endsWith("SystemJobService") // PlatformJobService is evernote // com.evernote.android.job
        fun getNumberWorksQueued(appContext: Context): Int {
            val jobScheduler = appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val requestsMap = mutableMapOf<String, Int>()
            JobManager.create(appContext).allJobRequests.forEach {
                val tag = it.tag
                requestsMap.put(tag, (requestsMap[tag] ?: 0) + 1)
            }
            Log.d("NIKO", "yo ${requestsMap.toString()}")

            // try to get workers
            val allWorkers = jobScheduler.allPendingJobs.filterNot { it.service.className.startsWith("com.evernote.android.job") }
            val workDistributionMap = mutableMapOf<String, Int>()
            allWorkers.forEach { jobInfo ->
                jobInfo.extras.getString("EXTRA_WORK_SPEC_ID")?.let { workId ->
                    WorkManager.getInstance().getWorkInfoById(UUID.fromString(workId)).get()?.let { workInfo ->
                        val tags = workInfo.tags.filterNot { it == BS_TAG || it == BS_TAG2 }.sorted() // keep consistent sort
                        val mapTag = tags.joinToString("__")
                        workDistributionMap.put(mapTag, (workDistributionMap[mapTag] ?: 0) + 1)
                    }
                }

            }
            Log.d("NIKO", "work distro ${workDistributionMap.toString()}")
            // allWorkers[5].extras.getString("EXTRA_WORK_SPEC_ID")
            //WorkManager.getInstance().getWorkInfoById(UUID.fromString("f6570edf-a41a-4fc0-b5c8-37325c1a1ae9")).get().tags

            return jobScheduler.allPendingJobs.count()
        }


        /**
         * Get all evernote job requests and return a mapping of job tags and count of each that is scheduled.
         * Annotating as workerthread b/c MailDependencies.getJobManager() has a potential delay
         */
//        @WorkerThread
//        private fun getEvernoteJobRequestDistribution(): Map<String, Int> {
//            val requestsMap = mutableMapOf<String, Int>()
//            JobManager.create(appContext).allJobRequests.forEach {
//                val tag = it.tag
//                requestsMap.put(tag, (requestsMap[tag] ?: 0) + 1)
//            }
//            return requestsMap.toMap()
//        }
//
//        /**
//         * Get all pending jobs split between evernote and workmanager
//         * @return Pair where first item is count of evernote pending jobs, second is workmanager
//         */
//        private fun getPendingJobsSplit(appContext: Context): Pair<Int, Int> {
//            val jobScheduler = appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//            val partition = jobScheduler.allPendingJobs.map { it.service.className }.partition { it.startsWith("com.evernote.android.job") }
//            return partition.first.size to partition.second.size
//        }
//
//        /**
//         * Leave breadcrumbs of all scheduled evernote jobs, their tags, and a count of each. Additionally leave breadcrumb how many evernote to workmanager requests
//         * are pending.
//         * @param context optional to additionally log job split between evernote and workmanager
//         */
//        @WorkerThread
//        @JvmStatic
//        @JvmOverloads
//        fun leaveJobDistributionBreadcrumb(appContext: Context? = null) {
//            val distribution = getEvernoteJobRequestDistribution()
//            val crumbs = if (appContext != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                val (numJobs, numWorks) = getPendingJobsSplit(appContext)
//                "job/worker usage: total=${numJobs + numWorks} jobs=$numJobs works=$numWorks $distribution".chunked(250)
//            } else "job/worker usage: $distribution".chunked(250)
//            crumbs.forEach {
//                Log.d(TAG, it)
//            }
//        }

    }


    override fun doWork(): Result {
        Log.d(TAG, "doing work")

        WorkManagerCounterSingleton.getTotalExecuted().postValue(++totalWorkExecuted)
        WorkManagerCounterSingleton.getTotalEnqueued().postValue(getNumberWorksQueued(applicationContext))

        //submitNewWork(applicationContext, false)
        return Result.success()
    }
}
