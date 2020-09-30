package io.square1.limor.remote.services.user


import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


const val USER_ME_PATH = "/api/v1/users/me"
const val USERS_PATH = "/api/v1/users/{id}"
const val USER_PODCASTS_PATH = "/api/v1/users/{id}/podcasts"
const val USER_LIKED_PODCASTS_PATH = "/api/v1/users/{id}/podcasts/likes"
const val LOG_OUT_PATH = "/oauth/revoke"
const val SHOW_FEED_PATH = "/api/v1/users/feed"
const val BLOCKED_USERS_PATH = "/api/v1/users/blocked_users"
const val FRIENDS_PATH = "/api/v1/users/{id}/friends"
const val REPORT_USER_PATH = "/api/v1/users/{id}/reports"
const val NOTIFICATIONS_PATH = "/api/v1/users/notifications"

interface UserService {

    @GET(USER_ME_PATH)
    fun userMe(): Single<ResponseBody>


    @GET(USERS_PATH)
    fun getUser(@Path ("id") id : Int): Single<ResponseBody>


    @POST(LOG_OUT_PATH)
    fun logOut(
        @Body logoutRequest: RequestBody
    ): Single<ResponseBody>


    @GET(SHOW_FEED_PATH)
    fun feedShow(@Query ("limit") limit : Int?, @Query("offset") offset : Int?): Single<ResponseBody>

    @GET(SHOW_FEED_PATH)
    fun feedShow(): Single<ResponseBody>

    @POST(FRIENDS_PATH)
    fun createFriend(@Path("id") id : Int): Single<ResponseBody>

    @DELETE(FRIENDS_PATH)
    fun deleteFriend(@Path("id") id : Int): Single<ResponseBody>

    @GET(BLOCKED_USERS_PATH)
    fun getBlockedUsers(@Query ("limit") limit : Int?, @Query("offset") offset : Int?): Single<ResponseBody>

    @POST(BLOCKED_USERS_PATH)
    fun createBlockedUser(@Body request: RequestBody): Single<ResponseBody>

    @HTTP(method = "DELETE", path = BLOCKED_USERS_PATH, hasBody = true) // this is a workaround to make delete work with a request body
    fun deleteBlockedUser(@Body request: RequestBody): Single<ResponseBody>

    @POST(REPORT_USER_PATH)
    fun reportUser(@Path("id") id: Int, @Body request: RequestBody): Single<ResponseBody>

    @GET(NOTIFICATIONS_PATH)
    fun getNotifications(@Query ("limit") limit : Int?, @Query("offset") offset : Int?): Single<ResponseBody>

    @GET(USER_PODCASTS_PATH)
    fun getPodcasts(@Path("id") id : Int, @Query ("limit") limit : Int? = 10, @Query("offset") offset : Int? = 0): Single<ResponseBody>

    @GET(USER_LIKED_PODCASTS_PATH)
    fun getPodcastsLiked(@Path("id") id : Int, @Query ("limit") limit : Int? = 10, @Query("offset") offset : Int? = 0): Single<ResponseBody>

}