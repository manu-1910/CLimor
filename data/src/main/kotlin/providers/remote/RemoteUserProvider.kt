package providers.remote


import entities.request.DataCreateUserReportRequestEntity
import entities.request.DataLogoutRequest
import entities.request.DataUserIDRequest
import entities.response.*
import io.reactivex.Single


interface RemoteUserProvider {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
    fun feedShow(): Single<FeedResponseEntity>
    fun feedShow(limit : Int, offset: Int): Single<FeedResponseEntity>
    fun createFriend(id: Int) : Single<CreateDeleteFriendResponseEntity>
    fun deleteFriend(id: Int) : Single<CreateDeleteFriendResponseEntity>
    fun createBlockedUser(userIDRequest: DataUserIDRequest) : Single<CreateBlockedUserResponseEntity>
    fun deleteBlockedUser(userIDRequest: DataUserIDRequest) : Single<CreateBlockedUserResponseEntity>
    fun reportUser(id: Int, request: DataCreateUserReportRequestEntity) : Single<CreateReportResponseEntity>
}