package repositories.user

import entities.request.DataLogoutRequest
import entities.request.DataUserIDRequest
import entities.response.*

import io.reactivex.Single

interface UserRepository {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
    fun feedShow(): Single<FeedResponseEntity>
    fun feedShow(limit: Int, offset: Int): Single<FeedResponseEntity>
    fun createFriend(id : Int) : Single<CreateFriendResponseEntity>
    fun createUserBlocked(userIDRequest: DataUserIDRequest): Single<CreateBlockedUserResponseEntity>
    fun deleteUserBlocked(userIDRequest: DataUserIDRequest): Single<CreateBlockedUserResponseEntity>
}
