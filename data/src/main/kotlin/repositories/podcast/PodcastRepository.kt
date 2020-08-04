package repositories.podcast


import entities.request.DataPublishRequest
import entities.response.PublishResponseEntity
import io.reactivex.Single

interface PodcastRepository {
    fun publishPodcast(dataPublishRequest: DataPublishRequest): Single<PublishResponseEntity>?
}