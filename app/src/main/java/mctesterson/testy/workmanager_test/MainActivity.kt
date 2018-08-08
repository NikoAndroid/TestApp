package mctesterson.testy.workmanager_test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.WorkManager


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var mButton: Button
    private lateinit var mCancelButton: Button
    private lateinit var mCounter: TextView

    //private lateinit var mModel: CountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mButton = findViewById(R.id.button)
        mButton.setOnClickListener { _ ->
            val workId = MainWorker.submitNewWork()

//            WorkManager.getInstance().getStatusById(workId)
//                    .observe(this, Observer { status ->
//                        if (status != null) {
//                            if (status.state.isFinished) {
//                                Log.d(TAG, "observer: work FINISHED")
//                            } else {
//                                Log.d(TAG, "observer: work not yet finished but something changed")
//                            }
//                        } else {
//                            Log.d(TAG, "observer: work status is null")
//                        }
//                    })
        }

        mCancelButton = findViewById(R.id.cancelButton)
        mCancelButton.setOnClickListener { _ ->
            MainWorker.cancelWork()
        }

        mCounter = findViewById(R.id.counter)
        CounterSingleton.getCount().observe(this, Observer { count ->
            if (count != null) {
                Log.d(TAG, "count observer: count=$count")
            } else {
                Log.d(TAG, "count observer: is null")
            }
            mCounter.text = if (count == null) "null" else count.toString(10)
        })


       // mModel = ViewModelProviders.of(applicationContext as Activity).get(CountViewModel::class.java)
    }
}

object CounterSingleton {
    private var mCountLiveData: MutableLiveData<Int>? = null
    private val lock: Any = Object()

    fun getCount(): MutableLiveData<Int> {
        if (mCountLiveData == null) {
            synchronized(lock) {
                if (mCountLiveData == null) {
                    mCountLiveData = MutableLiveData()
                }
            }
        }
        return mCountLiveData!!
    }
}