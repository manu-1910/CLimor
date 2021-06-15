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
import kotlinx.serialization.json.Json
import kotlinx.serialization.properties.Properties
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class AuthServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<AuthService>(AuthService::class.java, serviceConfig) {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    fun login(email:String, password:String): Single<NWAuthResponse> {

        val loginRequest = NWLoginRequest()
        loginRequest.client_id = serviceConfig.client_id
        loginRequest.client_secret = serviceConfig.client_secret
        loginRequest.grant_type = "password"
        loginRequest.scopes = "user"
        loginRequest.username = email
        loginRequest.password = password

        val map = Properties.encodeToMap(NWLoginRequest.serializer(), loginRequest)
        return service.login(map)
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .map { response ->
                response.parseSuccessResponse(NWAuthResponse.serializer())
            }
    }


    fun register(nwSignUpRequest: NWSignUpRequest): Single<NWSignUpResponse> {
        return service.registerBody(RequestBody.create(MediaType.parse("application/json"), json.encodeToString(NWSignUpRequest.serializer(), nwSignUpRequest)))
            .map { response -> response.parseSuccessResponse(NWSignUpResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }


    fun registerFB(nwSignUpFacebookRequest: NWSignUpFacebookRequest): Single<NWSignUpResponse> {
        return service.registerBody(RequestBody.create(MediaType.parse("application/json"), json.encodeToString(NWSignUpFacebookRequest.serializer(), nwSignUpFacebookRequest)))
            .map { response -> response.parseSuccessResponse(NWSignUpResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }


    fun forgotPassword(nWForgotPasswordRequest: NWForgotPasswordRequest): Completable {
        return service.forgotPassword(Properties.encodeToMap(NWForgotPasswordRequest.serializer(), nWForgotPasswordRequest))
            .doOnSuccess { println("SUCCESS: $it") }
            .doOnError { println("error: $it") }
            .ignoreElement()
    }


    fun requestTokenFacebook(nwTokenFBRequest: NWTokenFBRequest): Single<NWAuthResponse> {
        return service.requestTokenFacebook(Properties.encodeToMap(NWTokenFBRequest.serializer(), nwTokenFBRequest))
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .map { response ->
                response.parseSuccessResponse(NWAuthResponse.serializer())
            }
    }


    fun mergeAccounts(nwMergeFacebookAccountRequest: NWMergeFacebookAccountRequest): Single<NWAuthResponse> {
        return service.mergeAccounts(Properties.encodeToMap(NWMergeFacebookAccountRequest.serializer(), nwMergeFacebookAccountRequest))
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .map { response ->
                response.parseSuccessResponse(NWAuthResponse.serializer())
            }
    }



    fun changePassword(nwChangePasswordRequest: NWChangePasswordRequest): Single<NWChangePasswordResponse> {
        return service.changePassword(RequestBody.create(MediaType.parse("application/json"), json.encodeToString(NWChangePasswordRequest.serializer(), nwChangePasswordRequest)))
            .map { response -> response.parseSuccessResponse(NWChangePasswordResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }


}