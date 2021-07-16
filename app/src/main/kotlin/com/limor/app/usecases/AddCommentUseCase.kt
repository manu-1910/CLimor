package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(
        podcastId: Int,
        content: String,
        ownerId: Int,
        ownerType: String,
        audioURI: String? = null,
        duration: Int? = null
    ): Result<Int> {
        return runCatching {
            require(content.isNotBlank() && audioURI == null) {
                "Comment should not be empty"
            }
            withContext(dispatcherProvider.io) {
                if (audioURI != null) {

                    repository.createComment(podcastId, content, ownerId, ownerType, audioURI, duration)!!
                } else {
                    repository.createComment(podcastId, content, ownerId, ownerType)!!
                }
            }
        }
    }
}
