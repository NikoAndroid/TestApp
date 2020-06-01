package mctesterson.testy.testapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainTag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate: $intent")

        findViewById<Button>(R.id.button_workmanager).setOnClickListener { _ ->
            startActivity(Intent(this, WorkManagerActivity::class.java))
        }

        findViewById<Button>(R.id.button_notifications).setOnClickListener { _ ->
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<Button>(R.id.button_foreground_service).setOnClickListener { _ ->
            startActivity(Intent(this, ForegroundServiceActivity::class.java))
        }

        findViewById<Button>(R.id.button_intents).setOnClickListener { _ ->
            startActivity(Intent(this, IntentsActivity::class.java))
        }

        findViewById<Button>(R.id.button_dummy).setOnClickListener { _ ->
            startActivity(Intent(this, DummyActivity::class.java))
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: $intent")
    }

}