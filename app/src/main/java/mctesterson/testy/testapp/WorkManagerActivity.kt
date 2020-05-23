package mctesterson.testy.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.evernote.android.job.JobManager
import mctesterson.testy.testapp.workmanager.EvernoteJob
import mctesterson.testy.testapp.workmanager.MainWorker


class WorkManagerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WorkManagerActivity"
        private const val REFRESH_FREQ = 1000L
    }

    private val uiHandler = Handler()
    private var isForeground = false
    private lateinit var buttonEnqueueNow: Button
    private lateinit var buttonEnqueueFuture: Button
    private lateinit var buttonPeriodicWork: Button
    private lateinit var buttonCancelWorks: Button
    private lateinit var buttonCancelEvernoteJobs: Button
    private lateinit var textTotalEnqueued: TextView
    private lateinit var textTotalExecuted: TextView
    private lateinit var buttonEvernoteDailyJob: Button

    //private lateinit var mModel: CountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workmanager)

        buttonEnqueueNow = findViewById(R.id.button_now)
        buttonEnqueueNow.setOnClickListener { _ ->
            Log.d(TAG, "button pressed")
            MainWorker.submitNewWork(applicationContext, false)
        }

        buttonEnqueueFuture = findViewById(R.id.button_future)
        buttonEnqueueFuture.setOnClickListener { _ ->
            MainWorker.submitNewWork(applicationContext, true)
        }


        buttonPeriodicWork = findViewById(R.id.button_periodic)
        buttonPeriodicWork.setOnClickListener { MainWorker.submitPeriodicWork(applicationContext) }

        textTotalEnqueued = findViewById(R.id.text_scbeduled_count)
        textTotalExecuted = findViewById(R.id.text_executed_count)

        WorkManagerCounterSingleton.getTotalEnqueued().observe(this, Observer { count ->
            if (count != null) {
                Log.d(TAG, "enqueued observer: # enqueued=$count")
            } else {
                Log.d(TAG, "enqueued observer: is null")
            }
            textTotalEnqueued.text = if (count == null) "null" else count.toString(10)
        })

        WorkManagerCounterSingleton.getTotalExecuted().observe(this, Observer { count ->
            if (count != null) {
                Log.d(TAG, "executed observer: # executed=$count")
            } else {
                Log.d(TAG, "executed observer: is null")
            }
            textTotalExecuted.text = if (count == null) "null" else count.toString(10)
        })

        buttonCancelWorks = findViewById(R.id.button_cancel_works)
        buttonCancelWorks.setOnClickListener { _ ->
            Log.d(TAG, "Cancelling all works by tag")
            MainWorker.cancelWork()
        }

        buttonCancelEvernoteJobs = findViewById(R.id.button_cancel_evernote_jobs)
        buttonCancelEvernoteJobs.setOnClickListener { _ ->
            Log.d(TAG, "Cancelling all evernote jobs")
            JobManager.create(applicationContext).cancelAllForTag(EvernoteJob.TAG)
        }

        buttonEvernoteDailyJob = findViewById(R.id.button_enqueue_evernote_job)
        buttonEvernoteDailyJob.setOnClickListener { _ ->
            EvernoteJob.scheduleDailyJob()
        }


        // mModel = ViewModelProviders.of(applicationContext as Activity).get(CountViewModel::class.java)
    }

    override fun onResume() {
        isForeground = true

        uiHandler.postDelayed(object : Runnable {
            override fun run() {
                if (isForeground) {
                    WorkManagerCounterSingleton.getTotalEnqueued().postValue(MainWorker.getNumberWorksQueued(applicationContext))
                    uiHandler.postDelayed(this, REFRESH_FREQ)
                }
            }
        }, REFRESH_FREQ)
        super.onResume()
    }

    override fun onPause() {
        isForeground = false
        super.onPause()
    }
}

object WorkManagerCounterSingleton {
    private var totalExecutedLiveData: MutableLiveData<Int>? = null
    private var totalQueuedLiveData: MutableLiveData<Int>? = null
    private val lock: Any = Object()

    fun getTotalExecuted(): MutableLiveData<Int> {
        if (totalExecutedLiveData == null) {
            synchronized(lock) {
                if (totalExecutedLiveData == null) {
                    totalExecutedLiveData = MutableLiveData()
                }
            }
        }
        return totalExecutedLiveData!!
    }

    fun getTotalEnqueued(): MutableLiveData<Int> {
        if (totalQueuedLiveData == null) {
            synchronized(lock) {
                if (totalQueuedLiveData == null) {
                    totalQueuedLiveData = MutableLiveData()
                }
            }
        }
        return totalQueuedLiveData!!
    }
}