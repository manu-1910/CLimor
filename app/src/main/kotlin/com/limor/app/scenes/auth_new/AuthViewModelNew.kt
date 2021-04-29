package com.limor.app.scenes.auth_new

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AuthViewModelNew : ViewModel() {
    private val _datePicked = MutableLiveData<String>().apply { value = "" }

    val datePickedLiveData: LiveData<String>
        get() = _datePicked

    fun clearDate(){
        _datePicked.postValue("")
    }

    fun startDobPicker(fragmentManager: FragmentManager) {
        val dobPicker = object : DobPicker() {
            override fun onDatePicked(dateMills: Long) {
                val formattedDate = parseDate(dateMills)
                _datePicked.postValue(formattedDate)
            }
        }
        dobPicker.startMaterialPicker(fragmentManager)
    }
}