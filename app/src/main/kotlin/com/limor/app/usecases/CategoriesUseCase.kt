package com.limor.app.usecases

import com.limor.app.apollo.PublishRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.PatronCategoryUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoriesUseCase @Inject constructor(
    private val repository: PublishRepository,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun executeDownloadCategories(): Result<List<PatronCategoryUIModel?>> = runCatching {
        withContext(dispatcherProvider.io) {
            repository.getPatronCategories().map { it?.mapToUIModel() }
        }
    }

    suspend fun executeAddPatronCategories(categorySelectedIdsList: ArrayList<Int>) = runCatching {
        withContext(Dispatchers.IO) {
            repository.addPatronCategories(categorySelectedIdsList)
        }
    }

}