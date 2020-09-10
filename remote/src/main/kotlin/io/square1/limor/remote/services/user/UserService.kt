package io.square1.limor.remote.services.user


import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


const val USER_ME_PATH = "/api/v1/users/me"
const val LOG_OUT_PATH = "/oauth/revoke"
const val SHOW_FEED_PATH = "/api/v1/users/feed"
const val CREATE_FRIEND_PATH = "/api/v1/users/{id}/friends"
const val NOTIFICATIONS_PATH = "/api/v1/users/notifications"


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

    @GET(NOTIFICATIONS_PATH)
    fun getNotifications(@Query ("limit") limit : Int?, @Query("offset") offset : Int?): Single<ResponseBody>

}