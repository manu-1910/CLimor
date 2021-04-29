package com.limor.app.scenes.auth_new

import android.content.res.AssetManager
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.model.CountriesListProvider
import com.limor.app.scenes.utils.BACKGROUND
import timber.log.Timber

class AuthViewModelNew : ViewModel() {
    private val _datePicked = MutableLiveData<String>().apply { value = "" }

    val datePickedLiveData: LiveData<String>
        get() = _datePicked

    private val _countries = MutableLiveData<List<Country>>().apply { value = emptyList() }

    val countriesLiveData: LiveData<List<Country>>
        get() = _countries

    fun clearDate() {
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

    fun loadCountriesList(assets: AssetManager) {
        if(_countries.value?.size ?: 0 > 0) return
        BACKGROUND({
            val countries = CountriesListProvider().provideCountries(assets)
            Timber.d("Countries loaded -> ${countries.size}")
            _countries.postValue(countries)
        })

    }
}