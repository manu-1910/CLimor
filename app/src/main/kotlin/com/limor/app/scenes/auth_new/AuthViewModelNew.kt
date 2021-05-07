package com.limor.app.scenes.auth_new

import android.content.res.AssetManager
import android.os.Handler
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.limor.app.scenes.auth_new.data.*
import com.limor.app.scenes.auth_new.model.CountriesListProvider
import com.limor.app.scenes.auth_new.util.DobPicker
import com.limor.app.scenes.auth_new.util.PhoneNumberChecker
import com.limor.app.scenes.utils.BACKGROUND
import timber.log.Timber
import kotlin.random.Random

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
        _smsCodeValidationErrorMessageLiveData.postValue("")
        val value = codes.all { it?.isNotEmpty() ?: false }
        _smsCodeIsFullLiveData.postValue(value)
    }

    private val _smsCodeValidationErrorMessageLiveData =
        MutableLiveData<String>().apply { value = "" }

    val smsCodeValidationErrorMessageLiveData: LiveData<String>
        get() = _smsCodeValidationErrorMessageLiveData

    private val _smsCodeValidatedLiveData = MutableLiveData<Boolean>().apply { value = false }

    val smsCodeValidatedLiveData: LiveData<Boolean>
        get() = _smsCodeValidatedLiveData

    fun submitSmsCode(codes: List<String?>) {
//        val value = codes.all { it?.isNotEmpty() ?: false }
        _smsCodeValidationErrorMessageLiveData.postValue("")
        _smsCodeValidatedLiveData.postValue(true)
        Handler().postDelayed({ _smsCodeValidatedLiveData.postValue(false) }, 500)
    }

    /* Email validation*/

    var currentEmail: String = ""
        private set


    fun changeCurrentEmail(email: String?) {
        currentEmail = email?.trim() ?: ""
        _currentEmailIsValid.postValue(isEmailValid(currentEmail))
    }

    private val _currentEmailIsValid = MutableLiveData<Boolean>().apply { value = false }

    val currentEmailIsValidLiveData: LiveData<Boolean>
        get() = _currentEmailIsValid


    companion object {
        fun isEmailValid(email: String?): Boolean {
            if (email == null || email.isEmpty()) return false
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    /* Username validation*/

    var currentUsername: String = ""
        private set

    fun changeCurrentUserName(userName: String?, fromVariant: Boolean = false) {
        currentUsername = userName?.trim() ?: ""
        val action =
            if (fromVariant) UserNameStateBundle.Approved() else UserNameStateBundle.Editing()
        _currentUsernameState.postValue(action)
        _currentUsernameIsValid.postValue(currentUsername.length > 3)
    }

    private val _currentUsernameIsValid = MutableLiveData<Boolean>().apply { value = false }

    val currentUsernameIsValid: LiveData<Boolean>
        get() = _currentUsernameIsValid

    private val _currentUsernameState =
        MutableLiveData<UserNameStateBundle>().apply { value = UserNameStateBundle.Editing() }

    val currentUsernameState: LiveData<UserNameStateBundle>
        get() = Transformations.distinctUntilChanged(_currentUsernameState)

    fun submitUsername(username: String?) {
        if (_currentUsernameState.value?.approved!!) {
            _navigationFromUsernameScreenAllowed.postValue(true)
            Handler().postDelayed({ _navigationFromUsernameScreenAllowed.postValue(false) }, 500)
            return
        }
        val variants = List(5) {
            username + Random.nextInt(0, 100)
        }
        _currentUsernameState.postValue(UserNameStateBundle.Error(variants))
    }

    private val _navigationFromUsernameScreenAllowed =
        MutableLiveData<Boolean>().apply { value = false }

    val navigationFromUsernameScreenAllowed: LiveData<Boolean>
        get() = _navigationFromUsernameScreenAllowed


    /* Gender */
    var currentGender: Gender = Gender.Male
        private set

    fun setCurrentGender(gender: Gender) {
        currentGender = gender
    }

    /* Categories */

    private var categories: List<Category> = mutableListOf()

    fun downloadCategories() {
        if (categories.isEmpty())
            loadCategoriesRepo()
    }

    private fun loadCategoriesRepo() {
        val categories = createMockedCategories()
        this.categories = categories
        _categoriesLiveData.postValue(categories)
    }

    private val _categoriesLiveData =
        MutableLiveData<List<Category>>().apply { value = categories }

    val categoriesLiveData: LiveData<List<Category>>
        get() = _categoriesLiveData


    fun updateCategoriesSelection() {
        val anySelected = categories.any { it.isSelected }
        _categorySelectionDone.postValue(anySelected)
    }

    private val _categorySelectionDone =
        MutableLiveData<Boolean>().apply { value = false }

    val categorySelectionDone: LiveData<Boolean>
        get() = _categorySelectionDone
}