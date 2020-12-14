package repositories.categories


import entities.response.CategoriesResponseEntity
import entities.response.GetPodcastsResponseEntity
import io.reactivex.Single
import providers.remote.RemoteCategoriesProvider
import javax.inject.Inject


class DataCategoriesRepository @Inject constructor(private val remoteProvider: RemoteCategoriesProvider): CategoriesRepository {

    override fun getCategories(): Single<CategoriesResponseEntity>?{
        return remoteProvider.getCategories()
    }

    override fun getCategories(limit: Int?, offset: Int?): Single<CategoriesResponseEntity>? {
        return remoteProvider.getCategories(limit, offset)
    }

    override fun getPodcastByCategory(
        id: Int,
        limit: Int?,
        offset: Int?
    ): Single<GetPodcastsResponseEntity>? {
        return remoteProvider.getPodcastByCategory(id, limit, offset)
    }

}