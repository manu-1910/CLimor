package com.limor.app.usecases

import com.limor.app.BuildConfig
import com.limor.app.apollo.PodcastInteractionsRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UpdatePreviewUseCase @Inject constructor(
    private val repository: PodcastInteractionsRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(podcastId: Int, previewDuration: Int, startsAt: Int, endsAt: Int): Boolean {
        var success = false
        if (BuildConfig.DEBUG) {
            Timber.d("Updating the preview for podcast ID $podcastId, starting at $startsAt and ending at $endsAt with a duration of $previewDuration");
        }
        withContext(dispatcherProvider.io) {
            success = repository.updatePreview(podcastId, previewDuration, startsAt, endsAt)
        }
        return success
    }
}