package io.square1.limor.remote.services.user


import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWLogoutRequest
import io.square1.limor.remote.entities.responses.NWErrorResponse
import io.square1.limor.remote.entities.responses.NWSignUpResponse
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject


@ImplicitReflectionSerializer
class UserServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) : RemoteService<UserService>(UserService::class.java, serviceConfig) {

    fun userMe(): Single<NWSignUpResponse> {
        return service.userMe()
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .map { response ->
                response.parseSuccessResponse(NWSignUpResponse.serializer())
            }
    }




    fun logOut(nwLogoutRequest: NWLogoutRequest): Single<NWErrorResponse> {
        return service.logOut(RequestBody.create(MediaType.parse("application/json"), Json.nonstrict.stringify(NWLogoutRequest.serializer(), nwLogoutRequest)))
            .map { response -> response.parseSuccessResponse(NWErrorResponse.serializer()) }
            .doOnSuccess { success -> println("SUCCESS: $success") }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

}