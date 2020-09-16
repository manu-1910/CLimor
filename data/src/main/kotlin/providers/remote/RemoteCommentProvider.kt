package providers.remote

import entities.request.DataCreateCommentRequest
import entities.response.*
import io.reactivex.Single

interface RemoteCommentProvider {
    fun createComment(idComment: Int, request: DataCreateCommentRequest): Single<CreateCommentResponseEntity>?
    fun likeComment(id : Int): Single<CreateCommentLikeResponseEntity>?
    fun dislikeComment(id : Int): Single<DeleteResponseEntity>?
    fun getComments(id : Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>?
    fun reportComment(id : Int): Single<CreateReportResponseEntity>?
}