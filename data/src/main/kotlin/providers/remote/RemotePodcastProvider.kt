package providers.remote

import entities.request.DataPublishRequest
import entities.response.CreatePodcastLikeResponseEntity
import entities.response.DeleteLikeResponseEntity
import entities.response.GetCommentsResponseEntity
import entities.response.PublishResponseEntity
import io.reactivex.Single


interface RemotePodcastProvider {
    fun publishPodcast(dataPublishRequest: DataPublishRequest): Single<PublishResponseEntity>?
    fun likePodcast(id : Int): Single<CreatePodcastLikeResponseEntity>?
    fun dislikePodcast(id : Int): Single<DeleteLikeResponseEntity>?
    fun getComments(id : Int): Single<GetCommentsResponseEntity>?
}