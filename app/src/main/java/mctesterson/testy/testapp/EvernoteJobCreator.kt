package mctesterson.testy.testapp

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

class EvernoteJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        return when (tag) {
            EvernoteJob.TAG -> EvernoteJob()
            else -> null
        }
    }

}