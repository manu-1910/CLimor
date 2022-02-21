package com.limor.app.scenes.auth_new.model

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.limor.app.App
import com.limor.app.R

class SmsCodeValidationResponse {

    private var errorType: ErrorType = ErrorType.NO_ERROR
    private var error: Exception? = null

    constructor(exception: Exception?) {
        errorType = if(exception !is FirebaseException) ErrorType.FIREBASE_EXCEPTION else ErrorType.GENERIC_ERROR
        error = exception
    }

    constructor(error: ErrorType){
        errorType = error
    }

    fun hasError(): Boolean{
        return (errorType != ErrorType.NO_ERROR)
    }

    fun canResend(): Boolean {
        return (error !is FirebaseTooManyRequestsException)
    }

    fun getLocalisedErrorMessage(): String {
        return when (errorType) {
            ErrorType.FIREBASE_EXCEPTION -> error?.localizedMessage ?: ""
            ErrorType.GENERIC_ERROR -> if(error == null) errorType.message else error?.localizedMessage ?: ""
            else -> ""
        }
    }

    fun getErrorCode(): Int {
        return if (errorType == ErrorType.FIREBASE_EXCEPTION && error is FirebaseAuthException) (error as FirebaseAuthException).errorCode.toInt() else -1
    }

    enum class ErrorType(val message: String) {
        FIREBASE_EXCEPTION(""),
        NO_CODE_ENTERED(App.instance.getString(R.string.no_code_entered)),
        CODE_HAS_NOT_BEEN_SENT(App.instance.getString(R.string.code_hasnt_been_sent)),
        CREDENTIAL_ERROR(App.instance.getString(R.string.error_getting_credentials)),
        USER_NOT_FOUND(App.instance.getString(R.string.no_user_found_offer_to_sign_up)),
        NO_ERROR(""),
        GENERIC_ERROR(App.instance.getString(R.string.sign_in_generic_error))
    }

}