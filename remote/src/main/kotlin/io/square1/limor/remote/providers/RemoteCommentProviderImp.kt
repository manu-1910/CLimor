package io.square1.limor.remote.providers

import entities.response.CreatePodcastLikeResponseEntity
import entities.response.DeleteLikeResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.mappers.asDataEntity
import io.square1.limor.remote.services.podcast.CommentServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.remote.RemoteCommentProvider
import javax.inject.Inject

@ImplicitReflectionSerializer
class RemoteCommentProviderImp @Inject constructor(private val provider: CommentServiceImp) :
    RemoteCommentProvider {


    override fun likeComment(id: Int): Single<CreatePodcastLikeResponseEntity>? {
        return provider.likeComment(id)?.asDataEntity()
    }

    override fun dislikeComment(id: Int): Single<DeleteLikeResponseEntity>? {
        return provider.dislikeComment(id)?.asDataEntity()
    }

}


