package com.limor.app.scenes.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.main_new.pagingsources.NotificationsPagingSource
import com.limor.app.uimodels.NotiUIMode
import com.limor.app.usecases.GetNotificationUserCaseNew
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val getNotificationUserCase: GetNotificationUserCaseNew,
    private val repository: GeneralInfoRepository,
): ViewModel() {

    private val _noti = MutableLiveData<List<NotiUIMode>>()
    val noti: LiveData<List<NotiUIMode>> get() = _noti

    fun loadNotifications() {
        viewModelScope.launch {
            getNotificationUserCase.execute()
                .onSuccess {
                    Timber.d(it.toString())
                    _noti.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while fetching notifications")
                }
        }
    }

    fun getNotifications(): Flow<PagingData<NotiUIMode>> {
        return Pager(
            config = PagingConfig(pageSize = NotificationsPagingSource.NETWORK_PAGE_SIZE),
            pagingSourceFactory = { NotificationsPagingSource(repository = repository) }
        ).flow.cachedIn(viewModelScope)
    }

    fun updateReadStatus(nId: Int, read: Boolean) {
        viewModelScope.launch {
            getNotificationUserCase.updateReadStatus(nId,read)
                .onSuccess {
                    Timber.d("$it Notification read status updated")
                }
                .onFailure {
                    Timber.e(it, "Error while fetching notifications")
                }
        }
    }

}