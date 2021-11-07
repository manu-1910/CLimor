package io.square1.limor.remote.services.podcast

import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateCommentRequest
import io.square1.limor.remote.entities.requests.NWCreateReportRequest
import io.square1.limor.remote.entities.requests.NWDropOffRequest
import io.square1.limor.remote.entities.requests.NWPublishRequest
import io.square1.limor.remote.entities.responses.*
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig

import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import javax.inject.Inject



class PodcastServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<PodcastService>(PodcastService::class.java, serviceConfig) {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    fun publishPodcast(nwPublishRequest: NWPublishRequest): Single<NWPublishResponse>? {
        val jsonRequest = json.encodeToString(NWPublishRequest.serializer(), nwPublishRequest)
        val request = RequestBody.create("application/json".toMediaTypeOrNull(), jsonRequest)
        return service.publishPodcast(request)
            .map { response -> response.parseSuccessResponse(NWPublishResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun likePodcast(id : Int): Single<NWCreatePodcastLikeResponse>? {
        return service.likePodcast(id)
            .map { response -> response.parseSuccessResponse(NWCreatePodcastLikeResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun dislikePodcast(id : Int): Single<NWDeleteResponse>? {
        return service.dislikePodcast(id)
            .map { response -> response.parseSuccessResponse(NWDeleteResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun recastPodcast(id : Int): Single<NWCreatePodcastRecastResponse>? {
        return service.recastPodcast(id)
            .map { response -> response.parseSuccessResponse(NWCreatePodcastRecastResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun deleteRecast(id : Int): Single<NWDeleteResponse>? {
        return service.deleteRecast(id)
            .map { response -> response.parseSuccessResponse(NWDeleteResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun getComments(id : Int, limit: Int, offset: Int): Single<NWGetCommentsResponse>? {
        return service.getComments(id, limit, offset)
            .map {
                    response -> response.parseSuccessResponse(NWGetCommentsResponse.serializer())
            }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun createComment(
        idPodcast: Int,
        request: NWCreateCommentRequest
    ): Single<NWCreateCommentResponse> {
        return service.createComment(idPodcast, RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.encodeToString(NWCreateCommentRequest.serializer(), request)
        ))
            .map {
                    response -> response.parseSuccessResponse(NWCreateCommentResponse.serializer())
            }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun createDropOff(
        idPodcast: Int,
        request: NWDropOffRequest
    ): Single<NWUpdatedResponse> {
        return service.createDropOff(idPodcast, RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.encodeToString(NWDropOffRequest.serializer(), request)
        ))
            .map {
                    response -> response.parseSuccessResponse(NWUpdatedResponse.serializer())
            }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun deletePodcast(
        idPodcast: Int
    ): Single<NWDeleteResponse> {
        return service.deletePodcast(idPodcast)
            .map {
                    response -> response.parseSuccessResponse(NWDeleteResponse.serializer())
            }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }



    fun reportPodcast(id : Int, request: NWCreateReportRequest): Single<NWCreateReportResponse>? {
        val jsonRequest = json.encodeToString(NWCreateReportRequest.serializer(), request)
        val requestParsed = RequestBody.create("application/json".toMediaTypeOrNull(), jsonRequest)
        return service.reportPodcast(id, requestParsed)
            .map { response -> response.parseSuccessResponse(NWCreateReportResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun getFeaturedPodcasts(): Single<NWFeaturedPodcastsResponse>? {
        return service.getFeaturedPodcasts()
            .map { response -> response.parseSuccessResponse(NWFeaturedPodcastsResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun getPopularPodcasts(): Single<NWPopularPodcastsResponse>? {
        return service.getPopularPodcasts()
            .map { response -> response.parseSuccessResponse(NWPopularPodcastsResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun getPodcastById(id : Int): Single<NWGetPodcastResponse>? {
        return service.getPodcastById(id)
            .map { response -> response.parseSuccessResponse(NWGetPodcastResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

}