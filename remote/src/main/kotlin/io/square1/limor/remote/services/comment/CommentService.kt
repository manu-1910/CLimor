package io.square1.limor.remote.services.comment

import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateCommentRequest
import io.square1.limor.remote.services.user.BLOCKED_USERS_PATH
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


const val COMMENT_LIKE_PATH = "/api/v1/comments/{id}/like"
const val COMMENT_COMMENT_PATH = "/api/v1/comments/{id}/comments"
const val COMMENT_PATH = "/api/v1/comments/{id}"
const val REPORT_COMMENT_PATH = "/api/v1/comments/{id}/reports"


interface CommentService {

    @POST(COMMENT_LIKE_PATH)
    fun likeComment(
        @Path("id") id: Int
    ): Single<ResponseBody>

    @DELETE(COMMENT_LIKE_PATH)
    fun dislikeComment(
        @Path("id") id: Int
    ): Single<ResponseBody>

    @GET(COMMENT_COMMENT_PATH)
    fun getComments(
        @Path("id") id: Int,
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?
    ): Single<ResponseBody>

    @POST(COMMENT_COMMENT_PATH)
    fun createComment(
        @Path("id") idComment: Int,
        @Body request: RequestBody
    ): Single<ResponseBody>

    @POST(REPORT_COMMENT_PATH)
    fun reportComment(
        @Path("id") idComment: Int,
        @Body request: RequestBody
    ): Single<ResponseBody>

    @HTTP(method = "DELETE", path = COMMENT_PATH, hasBody = true) // this is a workaround to make delete work with a request body
    fun deleteComment(
        @Path("id") idComment: Int,
        @Body request: RequestBody // I implement this body because it's documented in the API doc, but I think it's useless atm
    ): Single<ResponseBody>

}