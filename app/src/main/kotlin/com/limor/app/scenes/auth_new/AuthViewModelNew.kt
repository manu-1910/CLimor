package com.limor.app.scenes.auth_new

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.CountDownTimer
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.limor.app.BuildConfig
import com.limor.app.CreateVendorMutation
import com.limor.app.GendersQuery
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.auth_new.data.*
import com.limor.app.scenes.auth_new.firebase.EmailAuthHandler
import com.limor.app.scenes.auth_new.firebase.FacebookAuthHandler
import com.limor.app.scenes.auth_new.firebase.GoogleAuthHandler
import com.limor.app.scenes.auth_new.firebase.PhoneAuthHandler
import com.limor.app.scenes.auth_new.model.*
import com.limor.app.scenes.auth_new.model.UserInfoProvider.Companion.userNameRegExCheck
import com.limor.app.scenes.auth_new.util.*
import com.limor.app.uimodels.UserExistsModel
import com.tonyodev.fetch2.fetch.LiveSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject


class AuthViewModelNew @Inject constructor(
    val categoriesProvider: CategoriesProvider,
    val gendersProvider: GendersProvider,
    val languagesProvider: LanguagesProvider,
    val suggestedProvider: SuggestedProvider,
    val userInfoProvider: UserInfoProvider,
    val phoneAuthHandler: PhoneAuthHandler,
    val emailAuthHandler: EmailAuthHandler,
    val generalInfoRepository: GeneralInfoRepository,
) : ViewModel() {

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

    fun initPhoneAuthHandler(activityRef: WeakReference<Activity>) {
        phoneAuthHandler.init(activityRef, viewModelScope)
    }

    fun submitPhoneNumber() {
        phoneAuthHandler.sendCodeToPhone(formattedPhone, isSignInCase = signInCase)
    }

    private val _resendButtonEnableLiveData = MutableLiveData<Boolean>().apply { value = false }
    private val _resendButtonCountDownLiveData = MutableLiveData<Int?>().apply { value = null }

    val resendButtonEnableLiveData: LiveData<Boolean>
        get() = _resendButtonEnableLiveData.apply { false }

    val resendButtonCountDownLiveData: LiveData<Int?>
        get() = _resendButtonCountDownLiveData.apply { 60 }

    private var countDownTimer: CountDownTimer? = null

    private var reActivate: Boolean = false

    fun setReactivate(reactivate: Boolean){
        reActivate = reactivate
    }

    fun resendCode() {
        //phoneAuthHandler.sendCodeToPhone(formattedPhone, resend = true, signInCase)
        userInfoProvider.sendOtpToPhoneNumber(viewModelScope, formattedPhone, signInCase, reactivate = reActivate)
    }

    val otpSent:LiveData<String?>
        get() = userInfoProvider.otpSent.apply { null }

    fun sendOtp(){
        userInfoProvider.sendOtpToPhoneNumber(viewModelScope, formattedPhone, signInCase, reactivate = reActivate)
    }

    fun enableResend() {
        _resendButtonEnableLiveData.postValue(false)
        _resendButtonCountDownLiveData.postValue(120)
        countDownTimer = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _resendButtonCountDownLiveData.postValue((millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                _resendButtonEnableLiveData.postValue(true)
                _resendButtonCountDownLiveData.postValue(null)
            }
        }.start()
    }

    private val _countries = MutableLiveData<List<Country>>().apply { value = emptyList() }

    val countriesLiveData: LiveData<List<Country>>
        get() = _countries

    private val _validatePhoneLiveData = MutableLiveData<Boolean>().apply { value = false }

    val phoneIsValidLiveData: LiveData<Boolean>
        get() = _validatePhoneLiveData

    val phoneNumberExistsLiveData: LiveData<UserExistsModel?>
        get() = userInfoProvider.userExists

    fun checkPhoneNumberExistence() {
        setReactivate(false)
        userInfoProvider.checkIfUserExistsWithThisPhoneNumber(viewModelScope, formattedPhone)
    }

    private var currentCountry: Country = Country()
    private var currentPhone: String = ""

    fun loadCountriesList(assets: AssetManager) {
        if (_countries.value?.size ?: 0 > 0) return
        viewModelScope.launch {
            val countries = CountriesListProvider().provideCountries(assets)
            Timber.d("Countries loaded -> ${countries.size}")
            _countries.postValue(countries)
        }
    }

    val countrySelected: Country?
        get() =
            if (currentCountry.isEmpty) null
            else currentCountry

    private val _countrySelectedManually = MutableLiveData<Boolean?>().apply { value = null }

    val countrySelectedManuallyLiveData: LiveData<Boolean?>
        get() = _countrySelectedManually

    fun setCountrySelected(country: Country, manuallySelectedFromList: Boolean = false) {
        currentCountry = country
        if(manuallySelectedFromList){
            _countrySelectedManually.postValue(true)
        }
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

    val smsCodeValidationErrorMessage: LiveData<SmsCodeValidationResponse?>
        get() = phoneAuthHandler.smsCodeValidationErrorMessage

    val smsCodeValidationPassed: LiveData<Boolean>
        get() = phoneAuthHandler.smsCodeValidationPassed
    val smsContinueButtonEnabled: LiveData<Boolean> =
        _smsCodeIsFullLiveData.combineWith(smsCodeValidationErrorMessage) { full, error ->
            full!! && (error == null)
        }

    fun setSmsCodeForCheck(codes: List<String?>) {
        clearSmsCodeError()
        val value = codes.all { it?.isNotEmpty() ?: false }
        _smsCodeIsFullLiveData.postValue(value)
    }

    fun clearSmsCodeError() {
        phoneAuthHandler.clearError()
    }

    fun submitSmsCode(codes: List<String?>) {
        val value = codes.joinToString(separator = "")
        //phoneAuthHandler.enterCodeAndSignIn(value)
        if (signInCase){
            userInfoProvider.validateOtp(viewModelScope, formattedPhone, value.toInt(), reActivate = reActivate)
        } else{
            userInfoProvider.validateOtpForSignUp(viewModelScope, formattedPhone, value.toInt(), _datePicked.value?.mills ?: 0)
        }
    }

    /*
     *   Delete user account
     */

    val otpValidToDeleteUser: LiveData<String?>
        get() = userInfoProvider.otpValidToDeleteUser

    val otpSentToDeleteUser: LiveData<String?>
        get() = userInfoProvider.otpSentDeleteAccount

    fun sendOtpToDeleteUserAccount(){
        userInfoProvider.sendOtpToPhoneNumberToDeleteUserAccount(viewModelScope, formattedPhone)
    }

    fun validateOtpToDeleteUserAccount(codes: List<String?>){
        val value = codes.joinToString(separator = "")
        userInfoProvider.validateOtpToDeleteUserAccount(viewModelScope, formattedPhone, value.toInt())
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

    fun checkEmailIsInUse() {
        emailAuthHandler.checkEmailIsInUse(currentEmail, viewModelScope)
    }

    fun addEmailToUserAccount() {
        emailAuthHandler.addEmailToUser(currentEmail, viewModelScope)
    }

    val currentEmailIsInUseLiveData: LiveData<Boolean?>
        get() = emailAuthHandler.emailIsInUseLiveData

    val emailIsAttachedToUserLiveData: LiveData<Boolean?>
        get() = emailAuthHandler.emailAttachedToUserLiveData

    val emailAuthHandlerErrorLiveData: LiveData<String?>
        get() = emailAuthHandler.emailAuthErrorLiveData

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
        val action = UserNameStateBundle.Editing()
        _currentUsernameState.postValue(action)
        _currentUsernameIsValid.postValue(userNameRegExCheck(currentUsername))
    }

    private val _currentUsernameIsValid = MutableLiveData<Boolean>().apply { value = false }

    val currentUsernameIsValid: LiveData<Boolean>
        get() = _currentUsernameIsValid

    private val _currentUsernameState =
        MutableLiveData<UserNameStateBundle>().apply { value = UserNameStateBundle.Editing() }

    val currentUsernameState: LiveData<UserNameStateBundle>
        get() = Transformations.distinctUntilChanged(_currentUsernameState)

    fun submitUsername(username: String?) {
        userInfoProvider.updateFirebaseUserName(viewModelScope, username!!)
    }

    val userNameAttachedToUserLiveData: LiveData<Boolean?>
        get() = userInfoProvider.userNameAttachedToUserLiveData


    /* First name and Last name */
    var firstName: String = ""
        private set

    var lastName: String = ""
        private set

    private val _currentUserFullNameIsValid = MutableLiveData<Boolean?>().apply { value = null }

    val currentUserFullNameIsValid: LiveData<Boolean?>
        get() = _currentUserFullNameIsValid

    fun changeFirstName(fName: String) {
        firstName = fName
        if (firstName.trim().isNotEmpty() && lastName.trim().isNotEmpty()) {
            _currentUserFullNameIsValid.postValue(true)
        } else {
            _currentUserFullNameIsValid.postValue(false)
        }
    }

    fun changeLastName(lName: String) {
        lastName = lName
        if (firstName.trim().isNotEmpty() && lastName.trim().isNotEmpty()) {
            _currentUserFullNameIsValid.postValue(true)
        } else {
            _currentUserFullNameIsValid.postValue(false)
        }
    }

    fun submitNames() {
        userInfoProvider.updateUserFirstNameAndLastName(viewModelScope, firstName, lastName)
    }

    /* Gender */

    fun downloadGenders() = gendersProvider.downloadGenders(viewModelScope)
    val currentGenderId: Int
        get() = gendersProvider.selectedGenderId
    val selectedGenderIndex: Int
        get() = gendersProvider.selectedGenderIndex()

    fun selectGender(id: Int) {
        gendersProvider.selectedGenderId = id
    }

    val gendersLiveData: LiveData<List<GendersQuery.Gender>>
        get() = gendersProvider.gendersLiveData

    val gendersSelectionDone: LiveData<Boolean>
        get() = gendersProvider.gendersSelectionDone

    val gendersLiveDataError: LiveData<String>
        get() = gendersProvider.gendersLiveDataError


    /* Categories */

    fun downloadCategories() = categoriesProvider.downloadCategories(viewModelScope)

    fun updateCategoriesSelection() =
        categoriesProvider.updateCategoriesSelection()

    val categoriesLiveData: LiveData<List<CategoryWrapper>>
        get() = categoriesProvider.categoriesLiveData

    val categorySelectionDone: LiveData<Boolean>
        get() = categoriesProvider.categorySelectionDone

    val categoryLiveDataError: LiveData<String>
        get() = categoriesProvider.categoryLiveDataError

    /* Languages */


    fun downloadLanguages() = languagesProvider.downloadLanguages(viewModelScope)

    val languagesLiveData: LiveData<List<LanguageWrapper>>
        get() = Transformations.distinctUntilChanged(languagesProvider.languagesLiveData)

    val languagesSelectionDone: LiveData<Boolean>
        get() = Transformations.distinctUntilChanged(languagesProvider.languagesSelectionDone)

    val languagesLiveDataError: LiveData<String>
        get() = languagesProvider.languageLiveDataError

    fun updateLanguagesSelection() = languagesProvider.updateLanguagesSelection()

    fun onLanguageInputChanged(input: String?) = languagesProvider.onLanguageInputChanged(input)

    /* Suggested users */

    fun downloadSuggested() = suggestedProvider.downloadSuggested(viewModelScope)

    fun followSuggestedUser(suggestedUser: SuggestedUser) =
        suggestedProvider.followUser(suggestedUser, viewModelScope)

    val suggestedUsersLiveData: LiveData<List<SuggestedUser>>
        get() = suggestedProvider.suggestedLiveData

    val suggestedSelectedLiveData: LiveData<Boolean>
        get() = suggestedProvider.suggestedSelectedLiveData

    val suggestedForwardNavigationLiveData: LiveData<Boolean>
        get() = suggestedProvider.suggestedForwardNavigationLiveData

    val suggestedLiveDataError: LiveData<String>
        get() = suggestedProvider.suggestedLiveDataError

    fun sendSuggestedPeopleSelectionResult() {
        suggestedProvider.sendSuggestedPeopleSelectionResult(viewModelScope)
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

    fun clearSignErrors() {
        GoogleAuthHandler.clearError()
        FacebookAuthHandler.clearError()
    }

    val signErrorMessageLiveData: LiveData<String?>
        get() = GoogleAuthHandler.googleLoginError.combine(FacebookAuthHandler.facebookLoginError)

    /*FACEBOOK AUTH*/

    val facebookSignIsComplete: LiveData<Boolean>
        get() = FacebookAuthHandler.facebookLoginSuccess


    /* SIGN IN*/

    private val _signInMethodLiveData =
        MutableLiveData<SignInMethod>()
    val signInMethodLiveData: LiveData<SignInMethod>
        get() = _signInMethodLiveData

    var signInCase: Boolean = false
        private set

    val signInMethodContinueEnabledLiveData: LiveData<Boolean>
        get() = _phoneSignInMethodContinueEnabledLiveData.combineWith(
            _emailSignInMethodContinueEnabledLiveData,
            block = { one, two ->
                Timber.d("signInMethodContinueEnabledLiveData $one | $two")
                signInCase = one ?: false || two ?: false
                signInCase
            })

    private val _phoneSignInMethodContinueEnabledLiveData: LiveData<Boolean>
        get() =
            phoneIsValidLiveData.combineWith(
                _signInMethodLiveData,
                block = { it, method -> (it ?: false) && method == SignInMethod.PHONE })
    private val _emailSignInMethodContinueEnabledLiveData: LiveData<Boolean>
        get() = currentEmailIsValidLiveData.combineWith(
            _signInMethodLiveData,
            block = { it, method -> (it ?: false) && method == SignInMethod.EMAIL })

    fun setCurrentSignInMethod(signInMethod: SignInMethod) {
        _signInMethodLiveData.postValue(signInMethod)
        if(signInMethod == SignInMethod.NONE){
            signInCase = false
        }
    }

    fun sendFirebaseDynamicLinkToEmail(context: Context) =
        emailAuthHandler.sendFirebaseDynamicLinkToEmailScoped(context, currentEmail, viewModelScope)

    val emailLinkSentLiveData: LiveData<Boolean?>
        get() = emailAuthHandler.emailLinkSentLiveData

    val handleEmailDynamicLinkLiveData: LiveData<Boolean?>
        get() = emailAuthHandler.handleEmailDynamicLinkLiveData

    fun handleEmailDynamicLink(context: Context, link: String) {
        if (currentEmail.isEmpty()) {
            Timber.d("CurrentEmail is empty -> return")
            return
        }
        emailAuthHandler.handleDynamicLink(context, link, viewModelScope)
    }

    val otpValid: LiveData<String?>
        get() = userInfoProvider.otpValid

    val otpInValid: LiveData<String?>
        get() = userInfoProvider.otpInValid

    fun signInWithToken(token: String){
        phoneAuthHandler.signInWithCustomToken(token)
    }

    /*User info*/

    val navigationBreakPointLiveData: LiveData<String?>
        get() = userInfoProvider.breakPointLiveData

    val userInfoProviderErrorLiveData: LiveData<Any?>
        get() = userInfoProvider.userInfoProviderErrorLiveData

    val updatePreferredInfoLiveData: LiveData<String?>
        get() = userInfoProvider.updatePreferredInfoLiveData

    val createUserLiveData: LiveData<String?>
        get() = userInfoProvider.createUserLiveData

    val updateOnboardingStatusLiveData: LiveData<String?>
        get() = userInfoProvider.updateOnboardingStatusLiveData

    val updateUserNameLiveData: LiveData<String?>
        get() = userInfoProvider.updateUserNameLiveData

    val updateUserDOBLiveData: LiveData<String?>
        get() = userInfoProvider.updateUserDOBLiveData

    val updateUserFirstNameAndLastNameLiveData: LiveData<String?>
        get() = userInfoProvider.updateUserFirstNameAndLastNameLiveData

    fun checkJwtForLuidAndProceed() {
        setReactivate(false)
        viewModelScope.launch {
            val userHasBeenCreatedBefore = JwtChecker.isFirebaseJwtContainsLuid()
            if (userHasBeenCreatedBefore)
                getUserOnboardingStatus()
            else
                createUser()
        }
    }

    fun saveNavigationBreakPoint(context: Context, breakpoint: String?) {
        viewModelScope.launch {
            PrefsHandler.saveNavigationBreakPoint(context, breakpoint)
        }
    }

    fun getUserOnboardingStatus() = userInfoProvider.getUserOnboardingStatus(viewModelScope)

    fun createUser() = userInfoProvider.createUser(viewModelScope, _datePicked.value?.mills ?: 0)

    fun updateUserName() = userInfoProvider.updateUserName(viewModelScope, currentUsername)

    fun updateDOB() = userInfoProvider.updateDOB(viewModelScope, _datePicked.value?.mills ?: 0)

    fun updatePreferredInfo() {
        val categoriesIds = categoriesProvider.getActiveCategoriesIds()
        val languages = languagesProvider.getActiveLanguages();
        userInfoProvider.updatePreferredInfo(
            viewModelScope,
            currentGenderId,
            categoriesIds,
            languages
        )
    }

    fun updateGenderInfo() {
        userInfoProvider.updateGender(
            viewModelScope,
            currentGenderId
        )
    }

    fun updateLanguagesAndCategories() {
        val categoriesIds = categoriesProvider.getActiveCategoriesIds()
        val languages = languagesProvider.getActiveLanguages();
        userInfoProvider.updateLanguagesAndCategories(
            viewModelScope,
            categoriesIds,
            languages
        )
    }

    fun updateUserOnboardingStatus(nextStep: String) =
        userInfoProvider.updateUserOnboardingStatus(viewModelScope, nextStep)

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    fun cancelTimers() {
        countDownTimer?.cancel()
    }

    fun createVendor(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        birthDate: String
    ): LiveData<ApiResult<String?>> {
        val callResult = MutableLiveData<ApiResult<String?>>()

        if (BuildConfig.DEBUG) {
            println("Will create vendor for $firstName $lastName, $email, $phone, $birthDate")
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                generalInfoRepository.createVendor(
                    firstName,
                    lastName,
                    email,
                    phone,
                    birthDate
                )
            }.onSuccess { result ->
                callResult.postValue(
                    ApiResult(
                        result = result?.data?.onboardingURL,
                        callWasSuccessful = true,
                        errorMessage = null
                    )
                )
            }.onFailure { throwable ->
                throwable.printStackTrace()
                if (BuildConfig.DEBUG) {
                    println("Error vendor: ${throwable.message}")
                }
                callResult.postValue(ApiResult.errored<String?>(throwable.localizedMessage))
            }
        }

        return callResult
    }
}
