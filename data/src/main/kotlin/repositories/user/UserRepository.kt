package repositories.user

import entities.request.DataCreateUserReportRequestEntity
import entities.request.DataLogoutRequest
import entities.request.DataUserIDRequest
import entities.response.*

import io.reactivex.Single

interface UserRepository {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
    fun feedShow(): Single<FeedResponseEntity>
    fun feedShow(limit: Int, offset: Int): Single<FeedResponseEntity>
    fun createFriend(id : Int) : Single<CreateDeleteFriendResponseEntity>
    fun deleteFriend(id : Int) : Single<CreateDeleteFriendResponseEntity>
    fun createUserBlocked(userIDRequest: DataUserIDRequest): Single<CreateBlockedUserResponseEntity>
    fun deleteUserBlocked(userIDRequest: DataUserIDRequest): Single<CreateBlockedUserResponseEntity>
    fun reportUser(id :Int, request: DataCreateUserReportRequestEntity): Single<CreateReportResponseEntity>
    fun getNotifications(limit: Int, offset: Int): Single<NotificationsResponseEntity>
    fun getPodcasts(id: Int, limit: Int, offset: Int): Single<GetPodcastsResponseEntity>
}
