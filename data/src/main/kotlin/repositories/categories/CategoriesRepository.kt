package repositories.categories

import entities.response.CategoriesResponseEntity
import entities.response.GetPodcastsResponseEntity
import io.reactivex.Single

interface CategoriesRepository {
    fun getCategories(): Single<CategoriesResponseEntity>?
    fun getCategories(limit: Int?, offset: Int?): Single<CategoriesResponseEntity>?
    fun getPodcastByCategory(id: Int, limit: Int?, offset: Int?): Single<GetPodcastsResponseEntity>?
}