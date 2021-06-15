package io.square1.limor.remote.providers


import entities.request.*
import entities.response.AuthResponseEntity
import entities.response.ChangePasswordResponseEntity
import entities.response.SignUpResponseEntity
import io.reactivex.Completable
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWForgotPasswordRequest
import io.square1.limor.remote.mappers.asDataEntity
import io.square1.limor.remote.mappers.asRemoteEntity
import io.square1.limor.remote.services.auth.AuthServiceImp

import providers.remote.RemoteAuthProvider
import javax.inject.Inject




class RemoteAuthProviderImp @Inject constructor(private val provider: AuthServiceImp) : RemoteAuthProvider {

    override fun signIn(email: String, password: String): Single<AuthResponseEntity> {
        return provider.login(email, password).asDataEntity()
    }

    override fun signUp(dataSignUpRequest: DataSignUpRequest): Single<SignUpResponseEntity> {
        return provider.register(dataSignUpRequest.asRemoteEntity()).asDataEntity()
    }

    override fun signUpFB(dataSignUpFacebookRequest: DataSignUpFacebookRequest): Single<SignUpResponseEntity> {
        return provider.registerFB(dataSignUpFacebookRequest.asRemoteEntity()).asDataEntity()
    }

    override fun forgotPass(email: String): Completable {
        return provider.forgotPassword(NWForgotPasswordRequest(email))
    }


    override fun requestTokenFB(dataTokenFBRequest: DataTokenFBRequest): Single<AuthResponseEntity> {
        return provider.requestTokenFacebook(dataTokenFBRequest.asRemoteEntity()).asDataEntity()
    }


    override fun mergeFBAccount(dataMergeFacebookAccountRequest: DataMergeFacebookAccountRequest): Single<AuthResponseEntity> {
        return provider.mergeAccounts(dataMergeFacebookAccountRequest.asRemoteEntity()).asDataEntity()
    }


    override fun changePassword(dataChangePasswordRequest: DataChangePasswordRequest): Single<ChangePasswordResponseEntity> {
        return provider.changePassword(dataChangePasswordRequest.asRemoteEntity()).asDataEntity()
    }

}


