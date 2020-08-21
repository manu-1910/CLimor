package repositories.podcast


import entities.request.DataPublishRequest
import entities.response.CreatePodcastLikeResponseEntity
import entities.response.DeletePodcastLikeResponseEntity
import entities.response.PublishResponseEntity
import io.reactivex.Single

interface PodcastRepository {
    fun publishPodcast(dataPublishRequest: DataPublishRequest): Single<PublishResponseEntity>?
    fun likePodcast(id: Int): Single<CreatePodcastLikeResponseEntity>?
    fun dislikePodcast(id: Int): Single<DeletePodcastLikeResponseEntity>?
}