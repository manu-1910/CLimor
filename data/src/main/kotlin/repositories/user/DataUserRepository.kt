package repositories.user


import entities.request.DataCreateUserReportRequestEntity
import entities.request.DataLogoutRequest
import entities.request.DataUserIDRequest
import entities.response.*
import io.reactivex.Single
import providers.remote.RemoteUserProvider
import javax.inject.Inject


class DataUserRepository @Inject constructor(private val remoteProvider: RemoteUserProvider): UserRepository {

    override fun userMe(): Single<SignUpResponseEntity> {
        return remoteProvider.userMe()
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

    override fun createFriend(id : Int): Single<CreateFriendResponseEntity> {
        return remoteProvider.createFriend(id)
    }

    override fun createUserBlocked(userIDRequest: DataUserIDRequest): Single<CreateBlockedUserResponseEntity> {
        return remoteProvider.createBlockedUser(userIDRequest)
    }

    override fun deleteUserBlocked(userIDRequest: DataUserIDRequest): Single<CreateBlockedUserResponseEntity> {
        return remoteProvider.deleteBlockedUser(userIDRequest)
    }

    override fun reportUser(
        id: Int,
        request: DataCreateUserReportRequestEntity
    ): Single<CreateReportResponseEntity> {
        return remoteProvider.reportUser(id, request)
    }

}