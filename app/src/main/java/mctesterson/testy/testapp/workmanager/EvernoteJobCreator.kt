package mctesterson.testy.testapp.workmanager

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import mctesterson.testy.testapp.workmanager.EvernoteJob

class EvernoteJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        return when (tag) {
            EvernoteJob.TAG -> EvernoteJob()
            else -> null
        }
    }

}