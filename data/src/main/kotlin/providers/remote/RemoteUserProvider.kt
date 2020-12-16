package providers.remote


import entities.request.*
import entities.response.*
import io.reactivex.Single


interface RemoteUserProvider {
    fun userMe(): Single<GetUserResponseEntity>
    fun userMeUpdate(dataUpdateProfileRequest: DataUpdateProfileRequest): Single<GetUserResponseEntity>
    fun getUser(id: Int): Single<GetUserResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
    fun feedShow(): Single<FeedResponseEntity>
    fun feedShow(limit : Int, offset: Int): Single<FeedResponseEntity>
    fun createFriend(id: Int) : Single<CreateDeleteFriendResponseEntity>
    fun deleteFriend(id: Int) : Single<CreateDeleteFriendResponseEntity>
    fun createBlockedUser(userIDRequest: DataUserIDRequest) : Single<BlockedUserResponseEntity>
    fun deleteBlockedUser(userIDRequest: DataUserIDRequest) : Single<BlockedUserResponseEntity>
    fun reportUser(id: Int, request: DataCreateReportRequestEntity) : Single<CreateReportResponseEntity>
    fun getNotifications(limit : Int, offset: Int): Single<NotificationsResponseEntity>
    fun getPodcasts(id: Int, limit : Int, offset: Int): Single<GetPodcastsResponseEntity>
    fun getPodcastsLiked(id: Int, limit: Int, offset: Int): Single<GetPodcastsResponseEntity>
    fun getBlockedUsers(limit: Int, offset: Int): Single<GetBlockedUsersResponseEntity>
    fun sendUserDevice(userDeviceRequestEntity: DataUserDeviceRequest): Single<UserDeviceResponseEntity>
    fun getFollowings(limit: Int, offset: Int): Single<GetFollowingsUsersResponseEntity>
    fun getFollowers(id: Int, limit: Int, offset: Int): Single<GetFollowersUsersResponseEntity>
}