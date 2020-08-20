package io.square1.limor.remote.services.podcast

import io.reactivex.Single
import io.square1.limor.remote.services.auth.AUTH_REGISTER_PATH
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap


const val PUBLISH_PODCAST_PATH = "/api/v1/podcasts/"
const val CREATE_PODCAST_LIKE_PATH = "/api/v1/podcasts/{id}/likes"


interface PodcastService {
//    @POST(PUBLISH_PODCAST_PATH)
//    fun publishPodcast(
//        @QueryMap(encoded = true) publishRequest: RequestBody
//    ): Single<ResponseBody>


    @POST(PUBLISH_PODCAST_PATH)
    fun publishPodcast(
        @Body publishRequest: RequestBody
    ): Single<ResponseBody>

    @POST(CREATE_PODCAST_LIKE_PATH)
    fun likePodcast(
        @Path("id") id : Int
    ): Single<ResponseBody>

//
//    @POST(AUTH_MERGE_ACCOUNTS_PATH)
//    fun mergeAccounts(
//        @QueryMap(encoded = true) mergeFacebookAccountRequest: @JvmSuppressWildcards Map<String, @JvmSuppressWildcards Any>
//    ): Single<ResponseBody>
}