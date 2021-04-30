package com.limor.app.scenes.auth_new

import android.content.res.AssetManager
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.model.CountriesListProvider
import com.limor.app.scenes.auth_new.util.DobPicker
import com.limor.app.scenes.auth_new.util.PhoneNumberChecker
import com.limor.app.scenes.utils.BACKGROUND
import timber.log.Timber

class AuthViewModelNew : ViewModel() {

    /* Date picking */

    private val _datePicked = MutableLiveData<String>().apply { value = "" }

    val datePickedLiveData: LiveData<String>
        get() = _datePicked

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

    /* PHONE Countries selection */

    private val _countries = MutableLiveData<List<Country>>().apply { value = emptyList() }

    val countriesLiveData: LiveData<List<Country>>
        get() = _countries

    private val _validatePhoneLiveData = MutableLiveData<Boolean>().apply { value = false }

    val phoneIsValidLiveData: LiveData<Boolean>
        get() = _validatePhoneLiveData

    private var currentCountry: Country = Country()
    private var currentPhone: String = ""

    fun loadCountriesList(assets: AssetManager) {
        if (_countries.value?.size ?: 0 > 0) return
        BACKGROUND({
            val countries = CountriesListProvider().provideCountries(assets)
            Timber.d("Countries loaded -> ${countries.size}")
            _countries.postValue(countries)
        })
    }

    val countrySelected: Country?
        get() =
            if (currentCountry.isEmpty) null
            else currentCountry

    fun setCountrySelected(country: Country) {
        currentCountry = country
        updatePhoneValidation()
    }

    fun setPhoneChanged(phone: String) {
        currentPhone = phone
        updatePhoneValidation()
    }

    private fun updatePhoneValidation() {
        val value =
            if (currentCountry.isEmpty or currentPhone.isEmpty()) false
            else PhoneNumberChecker.checkNumber(
                currentPhone,
                currentCountry.codeLetters
            )
        _validatePhoneLiveData.postValue(value)
    }

    val formattedPhone: String
        get() =
            PhoneNumberChecker.getFormattedNumber(currentPhone, currentCountry.codeLetters) ?: ""

    /* PHONE sms code */
    private val _smsCodeIsFullLiveData = MutableLiveData<Boolean>().apply { value = false }

    val smsCodeIsFullLiveData: LiveData<Boolean>
        get() = _smsCodeIsFullLiveData

    fun setSmsCodeForCheck(codes: List<String?>) {
        val value = codes.all { it?.isNotEmpty() ?: false }
        _smsCodeIsFullLiveData.postValue(value)
    }
}