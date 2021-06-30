package com.limor.app.usecases

import com.limor.app.apollo.SearchRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.CategoryUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchCategoriesUseCase @Inject constructor(
    private val repository: SearchRepository,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun execute(
        term: String
    ): Result<List<CategoryUIModel>> = runCatching {
        withContext(dispatcherProvider.io) {
            repository.searchCategories(term)
                .map { tag ->
                    tag.mapToUIModel()
                }
        }
    }
}