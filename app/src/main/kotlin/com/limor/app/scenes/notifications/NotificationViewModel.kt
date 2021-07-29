package com.limor.app.scenes.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.NotiUIMode
import com.limor.app.usecases.GetNotificationUserCaseNew
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val getNotificationUserCase: GetNotificationUserCaseNew
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