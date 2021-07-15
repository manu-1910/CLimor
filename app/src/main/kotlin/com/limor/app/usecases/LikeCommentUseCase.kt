package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LikeCommentUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(commentId: Int, like: Boolean) {
        return withContext(dispatcherProvider.io) {
            if (like) {
                repository.likeComment(commentId)
            } else {
                repository.unLikeComment(commentId)
            }
        }
    }
}