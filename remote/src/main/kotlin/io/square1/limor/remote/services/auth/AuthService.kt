package io.square1.limor.remote.services.auth


import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.QueryMap



const val AUTH_LOGIN_PATH = "/oauth/token"
const val FACEBOOK_LOGIN_PATH = "/oauth/token"
const val AUTH_REGISTER_PATH = "/api/v1/users"
const val AUTH_FORGOT_PASSWORD_PATH = "/api/v1/users/reset_password"  //TODO JJ IMPLEMENT LOGOUT
const val AUTH_MERGE_ACCOUNTS_PATH = "/api/v1/users/merge"


interface AuthService {
    @POST(AUTH_LOGIN_PATH)
    fun login(
        @QueryMap(encoded = true) loginRequest: @JvmSuppressWildcards Map<String, @JvmSuppressWildcards Any>
    ): Single<ResponseBody>


    @POST(AUTH_REGISTER_PATH)
    fun registerBody(
        @Body signUpRequest: RequestBody
    ): Single<ResponseBody>


    @PUT(AUTH_FORGOT_PASSWORD_PATH)
    fun forgotPassword(
        @QueryMap(encoded = true) forgotPasswordRequest: @JvmSuppressWildcards Map<String, @JvmSuppressWildcards Any>
    ): Single<ResponseBody>


    @POST(FACEBOOK_LOGIN_PATH)
    fun requestTokenFacebook(
        @QueryMap(encoded = true) facebookTokenRequest: @JvmSuppressWildcards Map<String, @JvmSuppressWildcards Any>
    ): Single<ResponseBody>


    @POST(AUTH_MERGE_ACCOUNTS_PATH)
    fun mergeAccounts(
        @QueryMap(encoded = true) mergeFacebookAccountRequest: @JvmSuppressWildcards Map<String, @JvmSuppressWildcards Any>
    ): Single<ResponseBody>
}