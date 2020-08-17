package io.square1.limor.remote.providers


import entities.request.DataCreateFriendRequest
import entities.request.DataLogoutRequest
import entities.response.CreateFriendResponseEntity
import entities.response.ErrorResponseEntity
import entities.response.FeedResponseEntity
import entities.response.SignUpResponseEntity
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

    override fun createFriend(dataCreateFriendRequest: DataCreateFriendRequest): Single<CreateFriendResponseEntity> {
        return provider.createFriend(dataCreateFriendRequest.asRemoteEntity()).asDataEntity()
    }
}


