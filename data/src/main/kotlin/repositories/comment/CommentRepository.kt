package repositories.comment

import entities.response.CreatePodcastLikeResponseEntity
import entities.response.DeleteLikeResponseEntity
import io.reactivex.Single


interface CommentRepository {
    fun likeComment(id: Int): Single<CreatePodcastLikeResponseEntity>?
    fun dislikeComment(id: Int): Single<DeleteLikeResponseEntity>?
}