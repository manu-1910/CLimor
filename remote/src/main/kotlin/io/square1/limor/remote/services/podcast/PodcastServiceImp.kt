package io.square1.limor.remote.services.podcast

import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateCommentRequest
import io.square1.limor.remote.entities.requests.NWPublishRequest
import io.square1.limor.remote.entities.responses.*
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject


@ImplicitReflectionSerializer
class PodcastServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<PodcastService>(PodcastService::class.java, serviceConfig) {



    fun publishPodcast(nwPublishRequest: NWPublishRequest): Single<NWPublishResponse>? {
        return service.publishPodcast(RequestBody.create(MediaType.parse("application/json"), Json.nonstrict.stringify(NWPublishRequest.serializer(), nwPublishRequest)))
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

    fun dislikePodcast(id : Int): Single<NWDeleteLikeResponse>? {
        return service.dislikePodcast(id)
            .map { response -> response.parseSuccessResponse(NWDeleteLikeResponse.serializer()) }
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
        return service.createComment(idPodcast, RequestBody.create(MediaType.parse("application/json"), Json.nonstrict.stringify(NWCreateCommentRequest.serializer(), request)))
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


//    fun forgotPassword(NWForgotPasswordRequest: NWForgotPasswordRequest): Completable {
//        return service.forgotPassword(Mapper.map(NWForgotPasswordRequest))
//            .doOnSuccess { println("SUCCESS: $it") }
//            .doOnError { println("error: $it") }
//            .ignoreElement()
//    }






}