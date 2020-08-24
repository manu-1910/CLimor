package io.square1.limor.remote.providers

import entities.request.DataPublishRequest
import entities.response.CreatePodcastLikeResponseEntity
import entities.response.DeletePodcastLikeResponseEntity
import entities.response.GetCommentsResponseEntity
import entities.response.PublishResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.mappers.asDataEntity
import io.square1.limor.remote.mappers.asRemoteEntity
import io.square1.limor.remote.services.podcast.PodcastServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.remote.RemotePodcastProvider
import javax.inject.Inject


@ImplicitReflectionSerializer
class RemotePodcastProviderImp @Inject constructor(private val provider: PodcastServiceImp) : RemotePodcastProvider {

    override fun publishPodcast(dataPublishRequest: DataPublishRequest): Single<PublishResponseEntity>? {
        return provider.publishPodcast(dataPublishRequest.asRemoteEntity())?.asDataEntity()
    }

    override fun likePodcast(id : Int): Single<CreatePodcastLikeResponseEntity>? {
        return provider.likePodcast(id)?.asDataEntity()
    }

    override fun dislikePodcast(id : Int): Single<DeletePodcastLikeResponseEntity>? {
        return provider.dislikePodcast(id)?.asDataEntity()
    }

    override fun getComments(id: Int): Single<GetCommentsResponseEntity>? {
        return provider.getComments(id)?.asDataEntity()
    }

}


