package mctesterson.testy.testapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View

private const val TAG = IntentsActivity.TAG

class DummyActivity : Activity() {

    companion object {
        const val INTENT_ACTION = "mctesterson.testy.testapp.DUMMY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate savedState=$savedInstanceState ${intent?.toString()}")

        setContentView(R.layout.activity_dummy)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop ${intent?.toString()}")

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent ${intent?.toString()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    fun navigateAway(view: View) {
        val intent = Intent(this, NotificationsActivity::class.java)
        startActivity(intent)
    }
}