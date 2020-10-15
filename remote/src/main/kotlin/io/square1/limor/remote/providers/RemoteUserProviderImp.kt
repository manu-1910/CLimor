package io.square1.limor.remote.providers


import entities.request.*
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

    override fun userMe(): Single<GetUserResponseEntity> {
        return provider.userMe().asDataEntity()
    }

    override fun getUser(id: Int): Single<GetUserResponseEntity> {
        return provider.getUser(id).asDataEntity()
    }

    override fun userMeUpdate(dataUpdateProfileRequest: DataUpdateProfileRequest): Single<GetUserResponseEntity> {
        return provider.userMeUpdate(dataUpdateProfileRequest.asRemoteEntity()).asDataEntity()
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

    override fun createFriend(id : Int): Single<CreateDeleteFriendResponseEntity> {
        return provider.createFriend(id).asDataEntity()
    }

    override fun deleteFriend(id : Int): Single<CreateDeleteFriendResponseEntity> {
        return provider.deleteFriend(id).asDataEntity()
    }

    override fun createBlockedUser(userIDRequest: DataUserIDRequest): Single<BlockedUserResponseEntity> {
        return provider.createBlockedUser(userIDRequest.asRemoteEntity()).asDataEntity()
    }

    override fun deleteBlockedUser(userIDRequest: DataUserIDRequest): Single<BlockedUserResponseEntity> {
        return provider.deleteBlockedUser(userIDRequest.asRemoteEntity()).asDataEntity()
    }

    override fun reportUser(
        id: Int,
        request: DataCreateReportRequestEntity
    ): Single<CreateReportResponseEntity> {
        return provider.reportUser(id, request.asRemoteEntity()).asDataEntity()
    }

    override fun getNotifications(limit: Int, offset: Int): Single<NotificationsResponseEntity> {
        return provider.getNotifications(limit, offset).asDataEntity()
    }

    override fun getPodcasts(id: Int, limit: Int, offset: Int): Single<GetPodcastsResponseEntity> {
        return provider.getPodcasts(id, limit, offset).asDataEntity()
    }

    override fun getPodcastsLiked(id: Int, limit: Int, offset: Int): Single<GetPodcastsResponseEntity> {
        return provider.getPodcastsLiked(id, limit, offset).asDataEntity()
    }

    override fun getBlockedUsers(limit: Int, offset: Int): Single<GetBlockedUsersResponseEntity> {
        return provider.getBlockedUsers(limit, offset).asDataEntity()
    }

    override fun sendUserDevice(userDeviceRequestEntity: DataUserDeviceRequest): Single<UserDeviceResponseEntity> {
        return provider.sendUserDevice(userDeviceRequestEntity.asRemoteEntity()).asDataEntity()!!
    }
}


