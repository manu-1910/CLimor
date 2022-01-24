package com.limor.app.scenes.auth_new.model

data class ApiResult<T>(val result: T?, val callWasSuccessful: Boolean, val errorMessage: String?) {

    companion object {
        fun <T> errored(message: String?): ApiResult<T> {
            return ApiResult(null, false, message)
        }
    }
}