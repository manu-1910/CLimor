package repositories.categories

import entities.response.CategoriesResponseEntity
import io.reactivex.Single

interface CategoriesRepository {
    fun getCategories(): Single<CategoriesResponseEntity>?
}