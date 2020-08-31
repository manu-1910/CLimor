package repositories.comment

import entities.response.CreateCommentLikeResponseEntity
import entities.response.DeleteLikeResponseEntity
import entities.response.GetCommentsResponseEntity
import io.reactivex.Single


interface CommentRepository {
    fun likeComment(id: Int): Single<CreateCommentLikeResponseEntity>?
    fun dislikeComment(id: Int): Single<DeleteLikeResponseEntity>?
    fun getComments(id: Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>?
}