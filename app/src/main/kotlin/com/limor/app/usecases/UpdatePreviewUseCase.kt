package com.limor.app.usecases

import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdatePreviewUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(podcastId: Int, previewDuration: Int, startsAt: Int, endsAt: Int): Boolean {
        var success = false
        withContext(dispatcherProvider.io) {
            success = repository.updatePreview(podcastId, previewDuration, startsAt, endsAt)
        }
        return success
    }
}