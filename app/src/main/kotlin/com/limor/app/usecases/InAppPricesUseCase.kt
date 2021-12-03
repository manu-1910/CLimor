package com.limor.app.usecases

import com.limor.app.apollo.PublishRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.PatronCategoryUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InAppPricesUseCase @Inject constructor(
    private val repository: PublishRepository,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun executeInAppProductTiers(): Result<List<String?>> = runCatching {
        withContext(dispatcherProvider.io) {
            repository.getInAppPricesTiers()
        }
    }

}