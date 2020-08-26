package io.square1.limor.remote.services.comment

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path


const val COMMENT_LIKE_PATH = "/api/v1/comments/{id}/like"


interface CommentService {

    @POST(COMMENT_LIKE_PATH)
    fun likeComment(
        @Path("id") id: Int
    ): Single<ResponseBody>

    @DELETE(COMMENT_LIKE_PATH)
    fun dislikeComment(
        @Path("id") id: Int
    ): Single<ResponseBody>

}