package io.square1.limor.remote.services.categories

import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWCategoriesResponse
import io.square1.limor.remote.entities.responses.NWGetPodcastsResponse
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.remote.services.RemoteService
import io.square1.limor.remote.services.RemoteServiceConfig

import javax.inject.Inject



class CategoriesServiceImp @Inject constructor(private val serviceConfig: RemoteServiceConfig) :
    RemoteService<CategoriesService>(CategoriesService::class.java, serviceConfig) {

    fun getCategories(): Single<NWCategoriesResponse>? {
        return service.getCategories()
            .map { response -> response.parseSuccessResponse(NWCategoriesResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun getCategories(limit: Int?, offset: Int?): Single<NWCategoriesResponse>? {
        return service.getCategories(limit, offset)
            .map { response -> response.parseSuccessResponse(NWCategoriesResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

    fun getPodcastByCategory(id: Int, limit: Int?, offset: Int?): Single<NWGetPodcastsResponse>? {
        return service.getPodcastByCategory(id, limit, offset)
            .map { response -> response.parseSuccessResponse(NWGetPodcastsResponse.serializer()) }
            .doOnSuccess { success ->
                println("SUCCESS: $success")
            }
            .doOnError { error ->
                println("ERROR: $error")
            }
    }

}