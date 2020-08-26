package repositories.comment


import entities.response.CreateCommentLikeResponseEntity
import entities.response.DeleteLikeResponseEntity
import io.reactivex.Single
import providers.remote.RemoteCommentProvider
import javax.inject.Inject


class DataCommentRepository @Inject constructor(private val remoteProvider: RemoteCommentProvider) :
    CommentRepository {


    override fun likeComment(id: Int): Single<CreateCommentLikeResponseEntity>? {
        return remoteProvider.likeComment(id)
    }

    override fun dislikeComment(id: Int): Single<DeleteLikeResponseEntity>? {
        return remoteProvider.dislikeComment(id)
    }


}