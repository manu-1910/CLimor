package com.limor.app.usecases

import com.limor.app.apollo.SearchRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.UserUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val repository: SearchRepository,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun execute(
        term: String,
        limit: Int = -1,
        offset: Int = 0
    ): Result<List<UserUIModel>> = runCatching {
        withContext(dispatcherProvider.io) {
            repository.searchUsers(term, limit, offset)
                .map { user ->
                    user.mapToUIModel()
                }
        }
    }
}