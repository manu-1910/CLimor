package com.limor.app.scenes.main_new.view_model

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.uimodels.UILocations

class LocationViewModel: ViewModel() {
    private val _locationData = MutableLiveData<UILocations>()
    val locationData: LiveData<UILocations> = _locationData

    fun setLocation(location: UILocations) {
        _locationData.value = location
    }
}