package io.square1.limor.remote.services.search

import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWSearchTermRequest
import io.square1.limor.remote.entities.requests.NWUserSearchRequest
import io.square1.limor.remote.entities.responses.NWLocationsResponse
import io.square1.limor.remote.entities.responses.NWGetPodcastsResponse
import io.square1.limor.remote.entities.responses.NWPromotedTagsResponse
import io.square1.limor.remote.entities.responses.NWTagsResponse
import io.square1.limor.remote.entities.responses.NWSuggestedUsersResponse
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig

import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import javax.inject.Inject



class SearchServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<SearchService>(SearchService::class.java, serviceConfig) {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    fun searchTag(tag: String): Single<NWTagsResponse>? {
        return service.searchTag(tag)
            .map { response -> response.parseSuccessResponse(NWTagsResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }


    fun searchLocations(nwSearchTermRequest: NWSearchTermRequest): Single<NWLocationsResponse>? {
        return service.searchLocation(RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.encodeToString(NWSearchTermRequest.serializer(), nwSearchTermRequest)
        ))
            .map { response -> response.parseSuccessResponse(NWLocationsResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun searchUsers(usersRequest: NWSearchTermRequest): Single<NWSuggestedUsersResponse>? {
        return service.searchUsers(RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.encodeToString(NWSearchTermRequest.serializer(), usersRequest)
        ))
            .map { response -> response.parseSuccessResponse(NWSuggestedUsersResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }


    fun trendingTags(): Single<NWTagsResponse>? {
        return service.trendingTags()
            .map { response -> response.parseSuccessResponse(NWTagsResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }


    fun promotedTags(): Single<NWPromotedTagsResponse>? {
        return service.promotedTags()
            .map { response -> response.parseSuccessResponse(NWPromotedTagsResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun podcastsByTag(limit: Int, offset: Int, tag: String): Single<NWGetPodcastsResponse>? {
        return service.podcastsByTag(limit, offset, tag)
            .map { response -> response.parseSuccessResponse(NWGetPodcastsResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }

    fun getSuggestedUsers(): Single<NWSuggestedUsersResponse>? {
        return service.getSuggestedUsers()
            .map { response -> response.parseSuccessResponse(NWSuggestedUsersResponse.serializer()) }
            .doOnSuccess {
                    success -> println("SUCCESS: $success")
            }
            .doOnError{
                    error -> println("ERROR: $error")
            }
    }


}