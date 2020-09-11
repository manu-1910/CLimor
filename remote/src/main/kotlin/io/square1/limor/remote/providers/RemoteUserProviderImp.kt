package io.square1.limor.remote.providers


import entities.request.DataLogoutRequest
import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.mappers.asDataEntity
import io.square1.limor.remote.mappers.asRemoteEntity
import io.square1.limor.remote.services.user.UserServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.remote.RemoteUserProvider
import javax.inject.Inject


@ImplicitReflectionSerializer
class RemoteUserProviderImp @Inject constructor(private val provider: UserServiceImp) : RemoteUserProvider {

    override fun userMe(): Single<SignUpResponseEntity> {
        return provider.userMe().asDataEntity()
    }

    override fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity> {
        return provider.logOut(dataLogoutRequest.asRemoteEntity()).asDataEntity()
    }

    override fun feedShow(): Single<FeedResponseEntity> {
        return provider.feedShow().asDataEntity()
    }

    override fun feedShow(limit: Int, offset: Int): Single<FeedResponseEntity> {
        return provider.feedShow(limit, offset).asDataEntity()
    }

    override fun createFriend(id : Int): Single<CreateFriendResponseEntity> {
        return provider.createFriend(id).asDataEntity()
    }

    override fun getNotifications(limit: Int, offset: Int): Single<NotificationsResponseEntity> {
        return provider.getNotifications(limit, offset).asDataEntity()
    }
}


