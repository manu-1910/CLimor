package io.square1.limor.remote.services.podcast

import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


const val PUBLISH_PODCAST_PATH = "/api/v1/podcasts/"
const val PODCAST_LIKE_PATH = "/api/v1/podcasts/{id}/likes"
const val PODCAST_COMMENTS_PATH = "/api/v1/podcasts/{id}/comments"


interface PodcastService {
//    @POST(PUBLISH_PODCAST_PATH)
//    fun publishPodcast(
//        @QueryMap(encoded = true) publishRequest: RequestBody
//    ): Single<ResponseBody>


    @POST(PUBLISH_PODCAST_PATH)
    fun publishPodcast(
        @Body publishRequest: RequestBody
    ): Single<ResponseBody>

    @POST(PODCAST_LIKE_PATH)
    fun likePodcast(
        @Path("id") id : Int
    ): Single<ResponseBody>

    @DELETE(PODCAST_LIKE_PATH)
    fun dislikePodcast(
        @Path("id") id : Int
    ): Single<ResponseBody>

    @GET(PODCAST_COMMENTS_PATH)
    fun getComments(
        @Path("id") id : Int
    ): Single<ResponseBody>

//
//    @POST(AUTH_MERGE_ACCOUNTS_PATH)
//    fun mergeAccounts(
//        @QueryMap(encoded = true) mergeFacebookAccountRequest: @JvmSuppressWildcards Map<String, @JvmSuppressWildcards Any>
//    ): Single<ResponseBody>
}