package io.square1.limor.common

class Constants{


    companion object {
        val TERMS_URL = "https://www.limor.ie/terms-and-conditions-of-use"
        val PRIVACY_URL = "https://www.limor.ie/privacy-policy"
        val SUPPORT_EMAIL = "support@limor.ie"

        // Authorization
        val CLIENT_ID: String = io.square1.limor.BuildConfig.CLIENT_ID
        val CLIENT_SECRET: String = io.square1.limor.BuildConfig.CLIENT_SECRET
        const val GRANT_TYPE_FACEBOOK = "facebook"
        const val GRANT_TYPE = "password"
        const val SCOPES = "user"


        // Errors
        private val ERROR_CODE_NO_AUTHENTICATED_USER: Int = 10
        private const val ERROR_CODE_AUTH_INVALID_CREDENTIALS = 100
        const val ERROR_CODE_FACEBOOK_USER_EXISTS = 1337
    }
}
