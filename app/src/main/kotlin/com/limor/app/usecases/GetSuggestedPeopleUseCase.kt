package com.limor.app.usecases

import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.UserUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetSuggestedPeopleUseCase @Inject constructor(
    private val repository: GeneralInfoRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(): Result<List<UserUIModel>> {
        return runCatching {
            withContext(dispatcherProvider.io) {
                repository.getSuggestedPeople()?.map {
                    it.mapToUIModel()
                } ?: emptyList()
            }
        }
    }
}