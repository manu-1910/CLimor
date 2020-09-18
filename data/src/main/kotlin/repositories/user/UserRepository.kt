package repositories.user

import entities.request.DataLogoutRequest
import entities.response.*

import io.reactivex.Single

interface UserRepository {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
    fun feedShow(): Single<FeedResponseEntity>
    fun feedShow(limit: Int, offset: Int): Single<FeedResponseEntity>
    fun createFriend(id : Int) : Single<CreateFriendResponseEntity>
    fun getNotifications(limit: Int, offset: Int): Single<NotificationsResponseEntity>
}