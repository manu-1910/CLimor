package com.limor.app.usecases

import com.limor.app.CategoriesQuery
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.CategoryUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: GeneralInfoRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(): Result<List<CategoryUIModel>> {
        return runCatching {
            withContext(dispatcherProvider.io) {
                repository.fetchCategories()!!.mapNotNull {
                    it.mapToUIModel()
                }
            }
        }
    }

    private fun CategoriesQuery.Category.mapToUIModel(): CategoryUIModel? {
        if (id == null || name == null) {
            return null
        }
        return CategoryUIModel(id, slug, name)
    }
}