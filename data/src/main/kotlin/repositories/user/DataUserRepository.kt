package repositories.user


import entities.request.DataLogoutRequest
import entities.response.ErrorResponseEntity
import entities.response.SignUpResponseEntity
import entities.response.UserEntity
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

}