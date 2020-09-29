package io.square1.limor.remote.services.auth


import io.reactivex.Completable
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.*
import io.square1.limor.remote.entities.responses.NWAuthResponse
import io.square1.limor.remote.entities.responses.NWChangePasswordResponse
import io.square1.limor.remote.entities.responses.NWSignUpResponse
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Mapper
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject


@ImplicitReflectionSerializer
class AuthServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<AuthService>(AuthService::class.java, serviceConfig) {

    fun login(email:String, password:String): Single<NWAuthResponse> {

        val loginRequest = NWLoginRequest()
        loginRequest.client_id = serviceConfig.client_id
        loginRequest.client_secret = serviceConfig.client_secret
        loginRequest.grant_type = "password"
        loginRequest.scopes = "user"
        loginRequest.username = email
        loginRequest.password = password

        return service.login(Mapper.map(loginRequest))
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .map { response ->
                response.parseSuccessResponse(NWAuthResponse.serializer())
            }
    }


    fun register(nwSignUpRequest: NWSignUpRequest): Single<NWSignUpResponse> {
        return service.registerBody(RequestBody.create(MediaType.parse("application/json"), Json.nonstrict.stringify(NWSignUpRequest.serializer(), nwSignUpRequest)))
            .map { response -> response.parseSuccessResponse(NWSignUpResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }


    fun forgotPassword(NWForgotPasswordRequest: NWForgotPasswordRequest): Completable {
        return service.forgotPassword(Mapper.map(NWForgotPasswordRequest))
            .doOnSuccess { println("SUCCESS: $it") }
            .doOnError { println("error: $it") }
            .ignoreElement()
    }


    fun requestTokenFacebook(nwTokenFBRequest: NWTokenFBRequest): Single<NWAuthResponse> {
        return service.requestTokenFacebook(Mapper.map(nwTokenFBRequest))
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .map { response ->
                response.parseSuccessResponse(NWAuthResponse.serializer())
            }
    }


    fun mergeAccounts(nwMergeFacebookAccountRequest: NWMergeFacebookAccountRequest): Single<NWAuthResponse> {
        return service.mergeAccounts(Mapper.map(nwMergeFacebookAccountRequest))
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .map { response ->
                response.parseSuccessResponse(NWAuthResponse.serializer())
            }
    }



    fun changePassword(nwChangePasswordRequest: NWChangePasswordRequest): Single<NWChangePasswordResponse> {
        return service.changePassword(RequestBody.create(MediaType.parse("application/json"), Json.nonstrict.stringify(NWChangePasswordRequest.serializer(), nwChangePasswordRequest)))
            .map { response -> response.parseSuccessResponse(NWChangePasswordResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }


}