package providers.remote

import entities.request.DataPublishRequest
import entities.response.PublishResponseEntity
import io.reactivex.Single


interface RemotePodcastProvider {
    fun publishPodcast(dataPublishRequest: DataPublishRequest): Single<PublishResponseEntity>?
}