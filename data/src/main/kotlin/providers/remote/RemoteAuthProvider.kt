package providers.remote

import entities.request.*
import entities.response.AuthResponseEntity
import entities.response.ChangePasswordResponseEntity
import entities.response.SignUpResponseEntity
import io.reactivex.Completable
import io.reactivex.Single


interface RemoteAuthProvider {
    fun signIn(email: String, password: String): Single<AuthResponseEntity>
    fun signUp(dataSignUpRequest: DataSignUpRequest): Single<SignUpResponseEntity>
    fun signUpFB(dataSignUpFacebookRequest: DataSignUpFacebookRequest): Single<SignUpResponseEntity>
    fun forgotPass(email: String): Completable
    fun requestTokenFB(dataTokenFBRequest: DataTokenFBRequest): Single<AuthResponseEntity>
    fun mergeFBAccount(dataMergeFacebookAccountRequest: DataMergeFacebookAccountRequest): Single<AuthResponseEntity>
    fun changePassword(dataChangePasswordRequest: DataChangePasswordRequest): Single<ChangePasswordResponseEntity>
}