package io.square1.limor.remote.services.search

import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWLocationsRequest
import io.square1.limor.remote.entities.responses.NWLocationsResponse
import io.square1.limor.remote.entities.responses.NWGetPodcastsResponse
import io.square1.limor.remote.entities.responses.NWPromotedTagsResponse
import io.square1.limor.remote.entities.responses.NWTagsResponse
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject


@ImplicitReflectionSerializer
class SearchServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<SearchService>(SearchService::class.java, serviceConfig) {


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


    fun searchLocations(nwLocationsRequest: NWLocationsRequest): Single<NWLocationsResponse>? {
        return service.searchLocation(RequestBody.create(MediaType.parse("application/json"), Json.nonstrict.stringify(NWLocationsRequest.serializer(), nwLocationsRequest)))
            .map { response -> response.parseSuccessResponse(NWLocationsResponse.serializer()) }
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
        return service.trendingTags()
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


}