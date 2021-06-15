package io.square1.limor.remote.providers


import entities.response.CategoriesResponseEntity
import entities.response.GetPodcastsResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.mappers.asDataEntity
import io.square1.limor.remote.services.categories.CategoriesServiceImp

import providers.remote.RemoteCategoriesProvider
import javax.inject.Inject



class RemoteCategoriesProviderImp @Inject constructor(private val provider: CategoriesServiceImp) :
    RemoteCategoriesProvider {

    override fun getCategories(): Single<CategoriesResponseEntity>? {
        return provider.getCategories()?.asDataEntity()
    }

    override fun getCategories(limit: Int?, offset: Int?): Single<CategoriesResponseEntity>? {
        return provider.getCategories(limit, offset)?.asDataEntity()
    }

    override fun getPodcastByCategory(
        id: Int,
        limit: Int?,
        offset: Int?
    ): Single<GetPodcastsResponseEntity>? {
        return provider.getPodcastByCategory(id, limit, offset)?.asDataEntity()
    }

}


