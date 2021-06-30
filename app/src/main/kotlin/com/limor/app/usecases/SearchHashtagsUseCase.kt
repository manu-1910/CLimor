package com.limor.app.usecases

import com.limor.app.apollo.SearchRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.TagUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchHashtagsUseCase @Inject constructor(
    private val repository: SearchRepository,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun execute(
        term: String,
        limit: Int = -1,
        offset: Int = 0
    ): Result<List<TagUIModel>> = runCatching {
        withContext(dispatcherProvider.io) {
            repository.searchHashtags(term, limit, offset)
                .map { tag ->
                    tag.mapToUIModel()
                }
        }
    }
}