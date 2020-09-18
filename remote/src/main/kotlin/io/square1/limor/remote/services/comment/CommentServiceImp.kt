package io.square1.limor.remote.services.comment

import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateCommentRequest
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

    fun dislikeComment(id: Int): Single<NWDeleteResponse>? {
        return service.dislikeComment(id)
            .map { response -> response.parseSuccessResponse(NWDeleteResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun getComments(id: Int, limit : Int, offset: Int): Single<NWGetCommentsResponse>? {
        return service.getComments(id, limit, offset)
            .map { response -> response.parseSuccessResponse(NWGetCommentsResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun createComment(idComment: Int, request: NWCreateCommentRequest): Single<NWCreateCommentResponse> {
        return service.createComment(idComment, RequestBody.create(
            MediaType.parse("application/json"), Json.nonstrict.stringify(
                NWCreateCommentRequest.serializer(), request)))
            .map { response -> response.parseSuccessResponse(NWCreateCommentResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun reportComment(id: Int): Single<NWCreateReportResponse>? {
        return service.reportComment(id)
            .map { response -> response.parseSuccessResponse(NWCreateReportResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }
}