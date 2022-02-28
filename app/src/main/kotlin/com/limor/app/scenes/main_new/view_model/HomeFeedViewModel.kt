package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.common.Constants
import com.limor.app.scenes.main_new.pagingsources.HomeFeedPagingSource
import com.limor.app.scenes.main_new.pagingsources.NotificationsPagingSource
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.NotiUIMode
import com.limor.app.usecases.GetHomeFeedCastsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class HomeFeedViewModel @Inject constructor(
    private val repository: GeneralInfoRepository,
) : ViewModel() {

    private var lastPagingSourceFactory: HomeFeedPagingSource? = null

    fun getHomeFeed(): Flow<PagingData<CastUIModel>> {
        return Pager(
            config = PagingConfig(pageSize = NotificationsPagingSource.NETWORK_PAGE_SIZE),
            pagingSourceFactory = { getSource() }
        ).flow.cachedIn(viewModelScope)
    }

    private fun getSource(): HomeFeedPagingSource {
        return HomeFeedPagingSource(repository = repository).also {
            lastPagingSourceFactory = it
        }
    }

    fun invalidate() {
        lastPagingSourceFactory?.invalidate()
    }
}
