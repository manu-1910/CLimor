package com.limor.app.usecases

import com.limor.app.apollo.FollowRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.SuggestedPersonUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FollowPersonUseCase @Inject constructor(
    private val repository: FollowRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(person: SuggestedPersonUIModel) {
        return withContext(dispatcherProvider.io) {
            if (person.isFollowed) {
                repository.unFollowUser(person.id)
            } else {
                repository.followUser(person.id)
            }
        }
    }
}