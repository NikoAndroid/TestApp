package mctesterson.testy.testapp

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log



import androidx.work.Configuration;
import androidx.work.WorkManager;

/**
 * Initializes workmanager
 * We must explicitly set max scheduler limit to 50. Default is 20. [MA-12953]
 */

class WorkManagerInitializer : ContentProvider() {
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun onCreate(): Boolean {
        WorkManager.initialize(context!!, Configuration.Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .setMaxSchedulerLimit(50)
                .build());
        return true;
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

}