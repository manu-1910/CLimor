package providers.remote

import entities.request.DataCreateCommentRequest
import entities.request.DataPublishRequest
import entities.response.*
import io.reactivex.Single


interface RemotePodcastProvider {
    fun publishPodcast(dataPublishRequest: DataPublishRequest): Single<PublishResponseEntity>?
    fun likePodcast(id : Int): Single<CreatePodcastLikeResponseEntity>?
    fun dislikePodcast(id : Int): Single<DeleteLikeResponseEntity>?
    fun createComment(idPodcast: Int, request: DataCreateCommentRequest): Single<CreateCommentResponseEntity>?
    fun getComments(id : Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>?
}