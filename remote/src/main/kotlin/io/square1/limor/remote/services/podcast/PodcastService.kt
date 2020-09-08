package io.square1.limor.remote.services.podcast

import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateCommentRequest
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.http.*


const val PUBLISH_PODCAST_PATH = "/api/v1/podcasts/"
const val PODCAST_LIKE_PATH = "/api/v1/podcasts/{id}/likes"
const val PODCAST_RECAST_PATH = "/api/v1/podcasts/{id}/recasts"
const val PODCAST_COMMENTS_PATH = "/api/v1/podcasts/{id}/comments"


interface PodcastService {
    @POST(PUBLISH_PODCAST_PATH)
    fun publishPodcast(
        @Body publishRequest: RequestBody
    ): Single<ResponseBody>

    @POST(PODCAST_LIKE_PATH)
    fun likePodcast(
        @Path("id") id: Int
    ): Single<ResponseBody>

    @DELETE(PODCAST_LIKE_PATH)
    fun dislikePodcast(
        @Path("id") id: Int
    ): Single<ResponseBody>

    @POST(PODCAST_RECAST_PATH)
    fun recastPodcast(
        @Path("id") id: Int
    ): Single<ResponseBody>

    @DELETE(PODCAST_RECAST_PATH)
    fun deleteRecast(
        @Path("id") id: Int
    ): Single<ResponseBody>

    @GET(PODCAST_COMMENTS_PATH)
    fun getComments(
        @Path("id") id: Int,
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?
    ): Single<ResponseBody>

    @POST(PODCAST_COMMENTS_PATH)
    fun createComment(
        @Path("id") idPodcast: Int,
        @Body request: RequestBody
    ): Single<ResponseBody>
}