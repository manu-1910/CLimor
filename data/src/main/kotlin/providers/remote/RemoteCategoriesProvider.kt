package providers.remote

import entities.response.CategoriesResponseEntity
import io.reactivex.Single


interface RemoteCategoriesProvider {
    fun getCategories(): Single<CategoriesResponseEntity>?
}