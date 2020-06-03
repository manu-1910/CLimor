package io.square1.limor.remote.services.user


import io.reactivex.Single
import io.square1.limor.remote.services.auth.AUTH_REGISTER_PATH
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


const val USER_ME_PATH = "/api/v1/users/me"
const val LOG_OUT_PATH = "/oauth/revoke"


interface UserService {

    @GET(USER_ME_PATH)
    fun userMe(): Single<ResponseBody>


    @POST(LOG_OUT_PATH)
    fun logOut(
        @Body logoutRequest: RequestBody
    ): Single<ResponseBody>

}