package io.square1.limor.remote.services.comment

import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWContentRequest
import io.square1.limor.remote.entities.requests.NWCreateCommentRequest
import io.square1.limor.remote.entities.requests.NWCreateReportRequest
import io.square1.limor.remote.entities.requests.NWDropOffRequest
import io.square1.limor.remote.entities.responses.*
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig

import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import javax.inject.Inject



class CommentServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<CommentService>(CommentService::class.java, serviceConfig) {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

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
            "application/json".toMediaTypeOrNull(), json.encodeToString(
                NWCreateCommentRequest.serializer(), request)))
            .map { response -> response.parseSuccessResponse(NWCreateCommentResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun reportComment(id: Int, request: NWCreateReportRequest): Single<NWCreateReportResponse>? {
        val jsonRequest = json.encodeToString(NWCreateReportRequest.serializer(), request)
        val requestParsed = RequestBody.create("application/json".toMediaTypeOrNull(), jsonRequest)
        return service.reportComment(id, requestParsed)
            .map { response -> response.parseSuccessResponse(NWCreateReportResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun deleteComment(id: Int, request: NWContentRequest): Single<NWDeleteResponse> {
        val jsonRequest = json.encodeToString(NWContentRequest.serializer(), request)
        val requestParsed = RequestBody.create("application/json".toMediaTypeOrNull(), jsonRequest)
        return service.deleteComment(id, requestParsed)
            .map { response -> response.parseSuccessResponse(NWDeleteResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun createDropOff(id: Int, request: NWDropOffRequest): Single<NWUpdatedResponse> {
        val jsonRequest = json.encodeToString(NWDropOffRequest.serializer(), request)
        val requestParsed = RequestBody.create("application/json".toMediaTypeOrNull(), jsonRequest)
        return service.createDropOff(id, requestParsed)
            .map { response -> response.parseSuccessResponse(NWUpdatedResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }
}