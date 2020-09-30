package providers.remote


import entities.request.DataCreateReportRequestEntity
import entities.request.DataLogoutRequest
import entities.request.DataUserIDRequest
import entities.response.*
import io.reactivex.Single


interface RemoteUserProvider {
    fun userMe(): Single<GetUserResponseEntity>
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
}