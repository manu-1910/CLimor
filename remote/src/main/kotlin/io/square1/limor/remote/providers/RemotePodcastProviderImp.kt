package io.square1.limor.remote.providers

import entities.request.DataCreateCommentRequest
import entities.request.DataPublishRequest
import entities.response.*
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

    override fun dislikePodcast(id : Int): Single<DeleteLikeResponseEntity>? {
        return provider.dislikePodcast(id)?.asDataEntity()
    }

    override fun createComment(
        idPodcast: Int,
        request: DataCreateCommentRequest
    ): Single<CreateCommentResponseEntity>? {
        return provider.createComment(idPodcast, request.asRemoteEntity()).asDataEntity()
    }

    override fun getComments(id: Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>? {
        return provider.getComments(id, limit, offset)?.asDataEntity()
    }

}


