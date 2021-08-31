package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteCommentUseCaseNew @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(commentId: Int) {
        return withContext(dispatcherProvider.io) {
            repository.deleteComment(commentId)
        }
    }
}