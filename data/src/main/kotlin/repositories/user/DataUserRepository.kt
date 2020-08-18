package repositories.user


import entities.request.DataCreateFriendRequest
import entities.request.DataLogoutRequest
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

    override fun createFriend(id : Int): Single<CreateFriendResponseEntity> {
        return remoteProvider.createFriend(id)
    }

}