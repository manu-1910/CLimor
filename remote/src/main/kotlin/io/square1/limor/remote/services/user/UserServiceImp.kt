package io.square1.limor.remote.services.user


import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWCreateUserReportRequest
import io.square1.limor.remote.entities.requests.NWLogoutRequest
import io.square1.limor.remote.entities.requests.NWUserIDRequest
import io.square1.limor.remote.entities.responses.*
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject


@ImplicitReflectionSerializer
class UserServiceImp @Inject constructor(serviceConfig: RemoteServiceConfig) :
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





    fun createBlockedUser(userIDRequest: NWUserIDRequest): Single<NWCreateBlockedUserResponse> {
        val requestString = Json.nonstrict.stringify(NWUserIDRequest.serializer(), userIDRequest)
        val request = RequestBody.create(
            MediaType.parse("application/json"),
            requestString
        )
        return service.createBlockedUser(request)
            .map { response ->
                response.parseSuccessResponse(NWCreateBlockedUserResponse.serializer())
            }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun deleteBlockedUser(userIDRequest: NWUserIDRequest): Single<NWCreateBlockedUserResponse> {
        return service.deleteBlockedUser(
            RequestBody.create(
                MediaType.parse("application/json"),
                Json.nonstrict.stringify(NWUserIDRequest.serializer(), userIDRequest)
            )
        )
            .map { response -> response.parseSuccessResponse(NWCreateBlockedUserResponse.serializer()) }
            .doOnSuccess { success -> println("SUCCESS: $success") }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }



    fun reportUser(id:Int, createReportRequest: NWCreateUserReportRequest): Single<NWCreateReportResponse> {
        val requestString = Json.nonstrict.stringify(NWCreateUserReportRequest.serializer(), createReportRequest)
        val request = RequestBody.create(
            MediaType.parse("application/json"),
            requestString
        )
        return service.reportUser(id, request)
            .map { response ->
                response.parseSuccessResponse(NWCreateReportResponse.serializer())
            }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }


}