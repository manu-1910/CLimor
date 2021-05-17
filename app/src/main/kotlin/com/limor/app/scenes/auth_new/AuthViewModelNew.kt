package com.limor.app.scenes.auth_new

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.os.CountDownTimer
import android.os.Handler
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.limor.app.scenes.auth_new.data.*
import com.limor.app.scenes.auth_new.firebase.FacebookAuthHandler
import com.limor.app.scenes.auth_new.firebase.GoogleAuthHandler
import com.limor.app.scenes.auth_new.firebase.PhoneAuthHandler
import com.limor.app.scenes.auth_new.model.CountriesListProvider
import com.limor.app.scenes.auth_new.util.DobPicker
import com.limor.app.scenes.auth_new.util.PhoneNumberChecker
import com.limor.app.scenes.auth_new.util.combine
import com.limor.app.scenes.auth_new.util.combineWith
import com.limor.app.scenes.utils.BACKGROUND
import timber.log.Timber
import kotlin.random.Random


class AuthViewModelNew : ViewModel() {

    /* Date picking */

    private val _datePicked = MutableLiveData<DobInfo>().apply { value = DobInfo.Empty() }
    val datePickedLiveData: LiveData<DobInfo>
        get() = _datePicked

    fun clearDate() {
        _datePicked.postValue(DobInfo.Empty())
    }

    private val dobPicker = object : DobPicker() {
        override fun onDatePicked(dateMills: Long) {
            _datePicked.postValue(DobInfo(dateMills))
        }
    }

    fun startDobPicker(fragmentManager: FragmentManager) {
        dobPicker.startMaterialPicker(fragmentManager, _datePicked.value?.mills ?: 0)
    }

    /* PHONE Countries selection */

    fun initPhoneAuthHandler(activity: Activity) {
        PhoneAuthHandler.init(activity)
    }

    fun submitPhoneNumber() {
        PhoneAuthHandler.sendCodeToPhone(formattedPhone)
    }

    private val _resendButtonEnableLiveData = MutableLiveData<Boolean>().apply { value = true }
    private val _resendButtonCountDownLiveData = MutableLiveData<Int?>().apply { value = null }

    val resendButtonEnableLiveData: LiveData<Boolean>
        get() = _resendButtonEnableLiveData

    val resendButtonCountDownLiveData: LiveData<Int?>
        get() = _resendButtonCountDownLiveData

    private var countDownTimer: CountDownTimer? = null

    fun resendCode() {
        _resendButtonEnableLiveData.postValue(false)
        _resendButtonCountDownLiveData.postValue(30)
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _resendButtonCountDownLiveData.postValue((millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                _resendButtonEnableLiveData.postValue(true)
                _resendButtonCountDownLiveData.postValue(null)
                PhoneAuthHandler.sendCodeToPhone(formattedPhone, resend = true)
            }
        }.start()
    }

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

    val smsCodeValidationErrorMessage: LiveData<String>
        get() = PhoneAuthHandler.smsCodeValidationErrorMessage

    val smsCodeValidatedLiveData: LiveData<Boolean>
        get() = PhoneAuthHandler.smsCodeValidatedLiveData

    val smsContinueButtonEnabled: LiveData<Boolean> =
        _smsCodeIsFullLiveData.combineWith(smsCodeValidationErrorMessage) { full, error ->
            full!! && (error?.isEmpty() ?: false)
        }

    fun setSmsCodeForCheck(codes: List<String?>) {
        PhoneAuthHandler.clearError()
        val value = codes.all { it?.isNotEmpty() ?: false }
        _smsCodeIsFullLiveData.postValue(value)
    }

    fun submitSmsCode(codes: List<String?>) {
        val value = codes.joinToString(separator = "")
        PhoneAuthHandler.enterCodeAndSignIn(value)
//        _smsCodeValidatedLiveData.postValue(true)
//        Handler().postDelayed({ _smsCodeValidatedLiveData.postValue(false) }, 500)
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
        BACKGROUND({
            val categories = createMockedCategories()
            this.categories = categories
            _categoriesLiveData.postValue(categories)
        })
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


    /* Languages */

    private var languages: List<Language> = mutableListOf()

    fun downloadLanguages() {
        if (languages.isEmpty())
            loadLanguagesRepo()
    }

    private fun loadLanguagesRepo() {
        BACKGROUND({
            val languages = createMockedLanguages()
            this.languages = languages
            _languagesLiveData.postValue(languages)
        })
    }

    private val _languagesLiveData =
        MutableLiveData<List<Language>>().apply { value = languages }

    val languagesLiveData: LiveData<List<Language>>
        get() = _languagesLiveData


    fun updateLanguagesSelection() {
        val anySelected = languages.any { it.isSelected }
        _languagesSelectionDone.postValue(anySelected)
    }

    private val _languagesSelectionDone =
        MutableLiveData<Boolean>().apply { value = false }

    val languagesSelectionDone: LiveData<Boolean>
        get() = _languagesSelectionDone

    fun onLanguageInputChanged(input: String?) {
        if (input == null || input.trim().isEmpty()) {
            _languagesLiveData.postValue(languages)
            return
        }
        val filtered = getLanguagesByInput(input, languages)
        _languagesLiveData.postValue(filtered)
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    /*GOOGLE AUTH*/

    fun startGoogleAuth(activity: Activity) {
        GoogleAuthHandler.initClientAndSignIn(activity)
    }

    fun handleGoogleAuthResult(data: Intent) {
        GoogleAuthHandler.handleResultIntent(data)
    }

    val googleSignIsComplete: LiveData<Boolean>
        get() = GoogleAuthHandler.googleSignIsComplete

    fun clearSignErrors(){
        GoogleAuthHandler.clearError()
        FacebookAuthHandler.clearError()
    }
    val signErrorMessageLiveData: LiveData<String?>
        get() = GoogleAuthHandler.googleLoginError.combine(FacebookAuthHandler.facebookLoginError)

    /*FACEBOOK AUTH*/

    val facebookSignIsComplete: LiveData<Boolean>
        get() = FacebookAuthHandler.facebookLoginSuccess
}