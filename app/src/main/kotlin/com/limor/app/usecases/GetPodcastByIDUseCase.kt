package com.limor.app.usecases

import com.limor.app.CreateReportsMutation
import com.limor.app.apollo.CastsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetPodcastByIDUseCase @Inject constructor(
    private val repository: CastsRepository,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun execute(
        castId: Int
    ): Result<CastUIModel> = runCatching {
        withContext(dispatcherProvider.io) {
            repository.getCastById(castId)?.mapToUIModel()
                ?: throw IllegalArgumentException("Cast with ID: $castId is not found")
        }
    }

    suspend fun deleteCast(id: Int) {
        repository.deleteCastById(id)
    }

    suspend fun reportCast(s: String, id: Int?) {
        repository.reportCast(s,id)
    }
}