package com.limor.app.usecases

import com.limor.app.apollo.CastsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPodcastsByUserUseCase @Inject constructor(
    private val repository: CastsRepository,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun execute(
        userId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): Result<List<CastUIModel>> = runCatching {
        withContext(dispatcherProvider.io) {
            repository.getCastsByUser(userId, limit, offset)
                .map { cast ->
                    cast.mapToUIModel()
                }
        }
    }
}