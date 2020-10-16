package io.square1.limor.remote.services.podcast

import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


const val PUBLISH_PODCAST_PATH = "/api/v1/podcasts/"
const val PODCAST_PATH = "/api/v1/podcasts/{id}"
const val PODCAST_LIKE_PATH = "/api/v1/podcasts/{id}/likes"
const val PODCAST_RECAST_PATH = "/api/v1/podcasts/{id}/recasts"
const val PODCAST_COMMENTS_PATH = "/api/v1/podcasts/{id}/comments"
const val PODCAST_REPORTS_PATH = "/api/v1/podcasts/{id}/reports"
const val FEATURED_PODCASTS_PATH = "/api/v1/podcasts/featured"
const val POPULAR_PODCASTS_PATH = "/api/v1/podcasts/popular"
const val PODCAST_DROPOFF_PATH = "/api/v1/podcasts/{id}/drop_offs"


interface PodcastService {
    @POST(PUBLISH_PODCAST_PATH)
    fun publishPodcast(
        @Body publishRequest: RequestBody
    ): Single<ResponseBody>

    @DELETE(PODCAST_PATH)
    fun deletePodcast(
        @Path("id") id: Int
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

    @POST(PODCAST_REPORTS_PATH)
    fun reportPodcast(
        @Path("id") idPodcast: Int,
        @Body request: RequestBody
    ): Single<ResponseBody>

    @POST(PODCAST_DROPOFF_PATH)
    fun createDropOff(
        @Path("id") idPodcast: Int,
        @Body request: RequestBody
    ): Single<ResponseBody>

    @GET(FEATURED_PODCASTS_PATH)
    fun getFeaturedPodcasts(): Single<ResponseBody>

    @GET(POPULAR_PODCASTS_PATH)
    fun getPopularPodcasts(): Single<ResponseBody>

    @GET(PODCAST_PATH)
    fun getPodcastById(
        @Path("id") idPodcast: Int
    ): Single<ResponseBody>
}