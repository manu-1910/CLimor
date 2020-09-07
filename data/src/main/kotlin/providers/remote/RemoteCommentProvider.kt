package providers.remote

import entities.request.DataCreateCommentRequest
import entities.response.CreateCommentLikeResponseEntity
import entities.response.CreateCommentResponseEntity
import entities.response.DeleteLikeResponseEntity
import entities.response.GetCommentsResponseEntity
import io.reactivex.Single

interface RemoteCommentProvider {
    fun createComment(idComment: Int, request: DataCreateCommentRequest): Single<CreateCommentResponseEntity>?
    fun likeComment(id : Int): Single<CreateCommentLikeResponseEntity>?
    fun dislikeComment(id : Int): Single<DeleteLikeResponseEntity>?
    fun getComments(id : Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>?
}