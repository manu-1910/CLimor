package com.limor.app.usecases

import com.limor.app.apollo.FollowRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.UserUIModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FollowPersonUseCase @Inject constructor(
    private val repository: FollowRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(person: UserUIModel, follow: Boolean) {
        return withContext(dispatcherProvider.io) {
            if (follow) {
                repository.followUser(person.id)
            } else {
                repository.unFollowUser(person.id)
            }
        }
    }
}