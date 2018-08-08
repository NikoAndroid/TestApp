package mctesterson.testy.workmanager_test

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class CountViewModel : ViewModel() {

    // Create a LiveData with a String
    private var mCountData: MutableLiveData<Int>? = null

    val currentCount: MutableLiveData<Int>
        get() {
            if (mCountData == null) {
                mCountData = MutableLiveData()
            }
            return mCountData!!
        }

}