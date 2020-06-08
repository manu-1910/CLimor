package repositories.auth


import entities.request.DataMergeFacebookAccountRequest
import entities.request.DataSignUpRequest
import entities.request.DataTokenFBRequest
import entities.response.AuthResponseEntity
import entities.response.SignUpResponseEntity
import io.reactivex.Completable
import io.reactivex.Single
import providers.remote.RemoteAuthProvider
import javax.inject.Inject


class DataAuthRepository @Inject constructor(private val remoteProvider: RemoteAuthProvider):
    AuthRepository {
    override fun signIn(email: String, password: String): Single<AuthResponseEntity> {
        return remoteProvider.signIn(email, password)
    }

    override fun signUp(dataSignUpRequest: DataSignUpRequest): Single<SignUpResponseEntity> {
        return remoteProvider.signUp(dataSignUpRequest)
    }

    override fun forgotPass(email: String): Completable {
        return remoteProvider.forgotPass(email)
    }

    override fun signInFB(dataTokenFBRequest: DataTokenFBRequest): Single<AuthResponseEntity> {
        return remoteProvider.requestTokenFB(dataTokenFBRequest)
    }

    override fun mergeFacebookAccount(dataMergeFacebookAccountRequest: DataMergeFacebookAccountRequest): Single<AuthResponseEntity> {
        return remoteProvider.mergeFBAccount(dataMergeFacebookAccountRequest)
    }
}