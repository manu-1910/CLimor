package io.square1.limor.remote.services.podcast

import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWPublishRequest
import io.square1.limor.remote.entities.responses.NWCreatePodcastLikeResponse
import io.square1.limor.remote.entities.responses.NWDeleteLikeResponse
import io.square1.limor.remote.entities.responses.NWGetCommentsResponse
import io.square1.limor.remote.entities.responses.NWPublishResponse
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject


@ImplicitReflectionSerializer
class CommentServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<CommentService>(CommentService::class.java, serviceConfig) {



    fun likeComment(id : Int): Single<NWCreatePodcastLikeResponse>? {
        return service.likeComment(id)
            .map { response -> response.parseSuccessResponse(NWCreatePodcastLikeResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun dislikeComment(id : Int): Single<NWDeleteLikeResponse>? {
        return service.dislikeComment(id)
            .map { response -> response.parseSuccessResponse(NWDeleteLikeResponse.serializer()) }
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