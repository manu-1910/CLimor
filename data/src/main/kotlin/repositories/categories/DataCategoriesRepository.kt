package repositories.categories


import entities.response.CategoriesResponseEntity
import io.reactivex.Single
import providers.remote.RemoteCategoriesProvider
import javax.inject.Inject


class DataCategoriesRepository @Inject constructor(private val remoteProvider: RemoteCategoriesProvider): CategoriesRepository {

    override fun getCategories(): Single<CategoriesResponseEntity>?{
        return remoteProvider.getCategories()
    }

}