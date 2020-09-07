package io.square1.limor.remote.providers


import entities.response.CategoriesResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.mappers.asDataEntity
import io.square1.limor.remote.services.categories.CategoriesServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.remote.RemoteCategoriesProvider
import javax.inject.Inject


@ImplicitReflectionSerializer
class RemoteCategoriesProviderImp @Inject constructor(private val provider: CategoriesServiceImp) :
    RemoteCategoriesProvider {

    override fun getCategories(): Single<CategoriesResponseEntity>? {
        return provider.getCategories()?.asDataEntity()
    }

}


