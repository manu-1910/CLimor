package io.square1.limor.remote.providers

import entities.request.DataCreateCommentRequest
import entities.request.DataCreateReportRequestEntity
import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.mappers.asDataEntity
import io.square1.limor.remote.mappers.asRemoteEntity
import io.square1.limor.remote.services.comment.CommentServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.remote.RemoteCommentProvider
import javax.inject.Inject

@ImplicitReflectionSerializer
class RemoteCommentProviderImp @Inject constructor(private val provider: CommentServiceImp) :
    RemoteCommentProvider {
    override fun createComment(
        idComment: Int,
        request: DataCreateCommentRequest
    ): Single<CreateCommentResponseEntity>? {
        return provider.createComment(idComment, request.asRemoteEntity()).asDataEntity()
    }


    override fun likeComment(id: Int): Single<CreateCommentLikeResponseEntity>? {
        return provider.likeComment(id)?.asDataEntity()
    }

    override fun dislikeComment(id: Int): Single<DeleteResponseEntity>? {
        return provider.dislikeComment(id)?.asDataEntity()
    }

    override fun getComments(id: Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>? {
        return provider.getComments(id, limit, offset)?.asDataEntity()
    }

    override fun reportComment(
        id: Int,
        request: DataCreateReportRequestEntity
    ): Single<CreateReportResponseEntity>? {
        return provider.reportComment(id, request.asRemoteEntity())?.asDataEntity()
    }

}


