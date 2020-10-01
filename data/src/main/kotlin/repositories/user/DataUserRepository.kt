package repositories.user


import entities.request.*
import entities.response.*
import io.reactivex.Single
import providers.remote.RemoteUserProvider
import javax.inject.Inject


class DataUserRepository @Inject constructor(private val remoteProvider: RemoteUserProvider): UserRepository {

    override fun userMe(): Single<SignUpResponseEntity> {
        return remoteProvider.userMe()
    }

    override fun userMeUpdate(dataUpdateProfileRequest: DataUpdateProfileRequest): Single<SignUpResponseEntity> {
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

    override fun createUserBlocked(userIDRequest: DataUserIDRequest): Single<CreateBlockedUserResponseEntity> {
        return remoteProvider.createBlockedUser(userIDRequest)
    }

    override fun deleteUserBlocked(userIDRequest: DataUserIDRequest): Single<CreateBlockedUserResponseEntity> {
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
}