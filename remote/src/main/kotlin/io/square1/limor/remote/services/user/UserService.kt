package io.square1.limor.remote.services.user


import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


const val USER_ME_PATH = "/api/v1/users/me"
const val LOG_OUT_PATH = "/oauth/revoke"
const val SHOW_FEED_PATH = "/api/v1/users/feed"
const val BLOCKED_USERS = "/api/v1/users/blocked_users"
const val CREATE_FRIEND_PATH = "/api/v1/users/{id}/friends"
const val REPORT_USER_PATH = "/api/v1/users/{id}/reports"


interface UserService {

    @GET(USER_ME_PATH)
    fun userMe(): Single<ResponseBody>


    @POST(LOG_OUT_PATH)
    fun logOut(
        @Body logoutRequest: RequestBody
    ): Single<ResponseBody>


    @GET(SHOW_FEED_PATH)
    fun feedShow(@Query ("limit") limit : Int?, @Query("offset") offset : Int?): Single<ResponseBody>

    @GET(SHOW_FEED_PATH)
    fun feedShow(): Single<ResponseBody>

    @POST(CREATE_FRIEND_PATH)
    fun createFriend(@Path("id") id : Int): Single<ResponseBody>

    @POST(BLOCKED_USERS)
    fun createBlockedUser(@Body request: RequestBody): Single<ResponseBody>

    @HTTP(method = "DELETE", path = BLOCKED_USERS, hasBody = true) // this is a workaround to make delete work with a request body
    fun deleteBlockedUser(@Body request: RequestBody): Single<ResponseBody>

    @POST(REPORT_USER_PATH)
    fun reportUser(@Path("id") id: Int, @Body request: RequestBody): Single<ResponseBody>

}