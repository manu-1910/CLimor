package repositories.podcast


import entities.request.DataPublishRequest
import entities.response.CreatePodcastLikeResponseEntity
import entities.response.DeletePodcastLikeResponseEntity
import entities.response.GetCommentsResponseEntity
import entities.response.PublishResponseEntity
import io.reactivex.Single
import providers.remote.RemotePodcastProvider
import javax.inject.Inject


class DataPodcastRepository @Inject constructor(private val remoteProvider: RemotePodcastProvider): PodcastRepository {


    override fun publishPodcast(dataPublishRequest: DataPublishRequest): Single<PublishResponseEntity>? {
        return remoteProvider.publishPodcast(dataPublishRequest)
    }

    override fun likePodcast(id: Int): Single<CreatePodcastLikeResponseEntity>? {
        return remoteProvider.likePodcast(id)
    }

    override fun dislikePodcast(id: Int): Single<DeletePodcastLikeResponseEntity>? {
        return remoteProvider.dislikePodcast(id)
    }

    override fun getComments(id: Int): Single<GetCommentsResponseEntity>? {
        return remoteProvider.getComments(id)
    }


}