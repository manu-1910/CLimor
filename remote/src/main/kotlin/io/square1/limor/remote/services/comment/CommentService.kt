package io.square1.limor.remote.services.comment

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*


const val COMMENT_LIKE_PATH = "/api/v1/comments/{id}/like"
const val COMMENT_COMMENT_PATH = "/api/v1/comments/{id}/comments"


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
        @Path("id") id : Int,
        @Query("limit") limit : Int?,
        @Query("offset") offset: Int?
    ): Single<ResponseBody>

}