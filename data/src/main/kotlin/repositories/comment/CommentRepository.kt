package repositories.comment

import entities.request.DataCreateCommentRequest
import entities.response.CreateCommentLikeResponseEntity
import entities.response.CreateCommentResponseEntity
import entities.response.DeleteResponseEntity
import entities.response.GetCommentsResponseEntity
import io.reactivex.Single


interface CommentRepository {

    fun createComment(id: Int, request: DataCreateCommentRequest): Single<CreateCommentResponseEntity>?
    fun likeComment(id: Int): Single<CreateCommentLikeResponseEntity>?
    fun dislikeComment(id: Int): Single<DeleteResponseEntity>?
    fun getComments(id: Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>?
}