package io.square1.limor.remote.services.comment

import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWCreateCommentLikeResponse
import io.square1.limor.remote.entities.responses.NWDeleteLikeResponse
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig
import kotlinx.serialization.ImplicitReflectionSerializer
import javax.inject.Inject


@ImplicitReflectionSerializer
class CommentServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<CommentService>(CommentService::class.java, serviceConfig) {


    fun likeComment(id: Int): Single<NWCreateCommentLikeResponse>? {
        return service.likeComment(id)
            .map { response -> response.parseSuccessResponse(NWCreateCommentLikeResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun dislikeComment(id: Int): Single<NWDeleteLikeResponse>? {
        return service.dislikeComment(id)
            .map { response -> response.parseSuccessResponse(NWDeleteLikeResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }
}