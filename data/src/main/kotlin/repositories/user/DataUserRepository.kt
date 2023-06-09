package repositories.user


import entities.request.*
import entities.response.*
import io.reactivex.Single
import providers.remote.RemoteUserProvider
import javax.inject.Inject


class DataUserRepository @Inject constructor(private val remoteProvider: RemoteUserProvider): UserRepository {

    override fun userMe(): Single<GetUserResponseEntity> {
        return remoteProvider.userMe()
    }

    override fun getUser(id: Int): Single<GetUserResponseEntity> {
        return remoteProvider.getUser(id)
    }
    override fun userMeUpdate(dataUpdateProfileRequest: DataUpdateProfileRequest): Single<GetUserResponseEntity> {
        return remoteProvider.userMeUpdate(dataUpdateProfileRequest)
    }


    override fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity> {
        return remoteProvider.logOut(dataLogoutRequest)
    }

    override fun feedShow(): Single<FeedResponseEntity> {
        return remoteProvider.feedShow()
    }

    override fun feedShow(limit: Int, offset: Int): Single<FeedResponseEntity> {
        return remoteProvider.feedShow(limit, offset)
    }

    override fun createFriend(id : Int): Single<CreateDeleteFriendResponseEntity> {
        return remoteProvider.createFriend(id)
    }

    override fun deleteFriend(id: Int): Single<CreateDeleteFriendResponseEntity> {
        return remoteProvider.deleteFriend(id)
    }

    override fun createUserBlocked(userIDRequest: DataUserIDRequest): Single<BlockedUserResponseEntity> {
        return remoteProvider.createBlockedUser(userIDRequest)
    }

    override fun deleteUserBlocked(userIDRequest: DataUserIDRequest): Single<BlockedUserResponseEntity> {
        return remoteProvider.deleteBlockedUser(userIDRequest)
    }

    override fun reportUser(
        id: Int,
        request: DataCreateReportRequestEntity
    ): Single<CreateReportResponseEntity> {
        return remoteProvider.reportUser(id, request)
    }

    override fun getNotifications(limit: Int, offset: Int): Single<NotificationsResponseEntity> {
        return remoteProvider.getNotifications(limit, offset)
    }

    override fun getPodcasts(id: Int, limit: Int, offset: Int): Single<GetPodcastsResponseEntity> {
        return remoteProvider.getPodcasts(id, limit, offset)
    }

    override fun getPodcastsLiked(id: Int, limit: Int, offset: Int): Single<GetPodcastsResponseEntity> {
        return remoteProvider.getPodcastsLiked(id, limit, offset)
    }

    override fun getBlockedUsers(
        limit: Int,
        offset: Int
    ): Single<GetBlockedUsersResponseEntity> {
        return remoteProvider.getBlockedUsers(limit, offset)
    }

    override fun sendUserDevice(dataUserDeviceRequest: DataUserDeviceRequest): Single<UserDeviceResponseEntity> {
        return remoteProvider.sendUserDevice(dataUserDeviceRequest)
    }

    override fun getFollowings(id: Int, limit: Int, offset: Int): Single<GetFollowingsUsersResponseEntity> {
        return remoteProvider.getFollowings(id, limit, offset)
    }

    override fun getFollowers(id: Int, limit: Int, offset: Int): Single<GetFollowersUsersResponseEntity> {
        return remoteProvider.getFollowers(id, limit, offset)
    }

}