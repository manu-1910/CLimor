package providers.remote

import entities.response.CreateCommentLikeResponseEntity
import entities.response.DeleteLikeResponseEntity
import io.reactivex.Single

interface RemoteCommentProvider {
    fun likeComment(id : Int): Single<CreateCommentLikeResponseEntity>?
    fun dislikeComment(id : Int): Single<DeleteLikeResponseEntity>?
}