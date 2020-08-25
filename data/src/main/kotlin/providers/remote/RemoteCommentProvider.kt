package providers.remote

import entities.response.CreatePodcastLikeResponseEntity
import entities.response.DeleteLikeResponseEntity
import io.reactivex.Single

interface RemoteCommentProvider {
    fun likeComment(id : Int): Single<CreatePodcastLikeResponseEntity>?
    fun dislikeComment(id : Int): Single<DeleteLikeResponseEntity>?
}