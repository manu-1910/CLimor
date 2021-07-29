package com.limor.app.usecases

import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.NotiUIMode
import com.limor.app.uimodels.mapToUIModel
import com.limor.app.uimodels.mapToUiModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetNotificationUserCaseNew @Inject constructor(
    private val repository: GeneralInfoRepository,
    private val dispatcherProvider: DispatcherProvider
)
{
    suspend fun execute(
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): Result<List<NotiUIMode>> = kotlin.runCatching {
        withContext(dispatcherProvider.io){
            repository.fetchNotifications(limit,offset).map {
                it.mapToUiModel()
            }
        }
    }

    suspend fun updateReadStatus(
        id: Int,
        read: Boolean
    ): Result<String> = kotlin.runCatching {
        withContext(dispatcherProvider.io){
            repository.updateReadStatus(id,read)
        }
    }
}