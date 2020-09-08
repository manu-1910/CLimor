package repositories.comment


import entities.request.DataCreateCommentRequest
import entities.response.CreateCommentLikeResponseEntity
import entities.response.CreateCommentResponseEntity
import entities.response.DeleteResponseEntity
import entities.response.GetCommentsResponseEntity
import io.reactivex.Single
import providers.remote.RemoteCommentProvider
import javax.inject.Inject


class DataCommentRepository @Inject constructor(private val remoteProvider: RemoteCommentProvider) :
    CommentRepository {


    override fun likeComment(id: Int): Single<CreateCommentLikeResponseEntity>? {
        return remoteProvider.likeComment(id)
    }

    override fun dislikeComment(id: Int): Single<DeleteResponseEntity>? {
        return remoteProvider.dislikeComment(id)
    }

    override fun getComments(id: Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>? {
        return remoteProvider.getComments(id, limit, offset)
    }

    override fun createComment(
        id: Int,
        request: DataCreateCommentRequest
    ): Single<CreateCommentResponseEntity>? {
        return remoteProvider.createComment(id, request)
    }


}