package io.square1.limor.remote.services.user


import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateFriendRequest
import io.square1.limor.remote.entities.requests.NWLogoutRequest
import io.square1.limor.remote.entities.responses.NWCreateFriendResponse
import io.square1.limor.remote.entities.responses.NWErrorResponse
import io.square1.limor.remote.entities.responses.NWFeedResponse
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
class UserServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<UserService>(UserService::class.java, serviceConfig) {

    fun userMe(): Single<NWSignUpResponse> {
        return service.userMe()
            .map { response -> response.parseSuccessResponse(NWSignUpResponse.serializer()) }
            .doOnSuccess { success -> println("SUCCESS: $success") }
            .doOnError { error -> println("ERROR: $error") }
    }


    fun logOut(nwLogoutRequest: NWLogoutRequest): Single<NWErrorResponse> {
        return service.logOut(
            RequestBody.create(
                MediaType.parse("application/json"),
                Json.nonstrict.stringify(NWLogoutRequest.serializer(), nwLogoutRequest)
            )
        )
            .map { response -> response.parseSuccessResponse(NWErrorResponse.serializer()) }
            .doOnSuccess { success -> println("SUCCESS: $success") }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun feedShow(limit: Int?, offset: Int?): Single<NWFeedResponse> {
        return service.feedShow(limit, offset)
            .map { response -> response.parseSuccessResponse(NWFeedResponse.serializer()) }
            .doOnSuccess { response ->
                run {
                    println("SUCCESS: $response")
                    //println("Hemos recibido ${response.data.feed_items.size} items")
                }
            }
            .doOnError { error ->
                run {
                    println("ERROR: $error")
                }
            }
    }

    fun feedShow(): Single<NWFeedResponse> {
        return service.feedShow()
            .map { response -> response.parseSuccessResponse(NWFeedResponse.serializer()) }
            .doOnSuccess { response ->
                run {
                    println("SUCCESS: $response")
                    //println("Hemos recibido ${response.data.feed_items.size} items")
                }
            }
            .doOnError { error ->
                run {
                    println("ERROR: $error")
                }
            }
    }

    fun createFriend(id: Int): Single<NWCreateFriendResponse> {
        return service.createFriend(id)
            .map { response -> response.parseSuccessResponse(NWCreateFriendResponse.serializer()) }
            .doOnSuccess { response ->
                run {
                    println("SUCCESS: $response")
                    println("Hemos seguido correctamente al user? ${response.data?.followed}")
                }
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

}