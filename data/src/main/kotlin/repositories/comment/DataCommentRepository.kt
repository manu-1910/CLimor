package repositories.comment


import entities.request.DataContentRequest
import entities.request.DataCreateCommentRequest
import entities.request.DataCreateReportRequestEntity
import entities.request.DataDropOffRequest
import entities.response.*
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

    override fun reportComment(
        id: Int,
        request: DataCreateReportRequestEntity
    ): Single<CreateReportResponseEntity>? {
        return remoteProvider.reportComment(id, request)
    }

    override fun deleteComment(
        id: Int,
        request: DataContentRequest
    ): Single<DeleteResponseEntity>? {
        return remoteProvider.deleteComment(id, request)
    }

    override fun createDropOff(
        id: Int,
        request: DataDropOffRequest
    ): Single<UpdatedResponseEntity>? {
        return remoteProvider.createDropOff(id, request)
    }

    override fun createComment(
        id: Int,
        request: DataCreateCommentRequest
    ): Single<CreateCommentResponseEntity>? {
        return remoteProvider.createComment(id, request)
    }


}