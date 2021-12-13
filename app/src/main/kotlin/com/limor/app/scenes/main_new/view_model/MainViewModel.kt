package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.GetAppVersionsQuery
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.common.Constants
import com.limor.app.uimodels.CastUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val generalInfoRepository: GeneralInfoRepository,
) : ViewModel() {

    fun checkAppVersion(platform:String): LiveData<GetAppVersionsQuery.GetAppVersions?> {
        val liveData = MutableLiveData<GetAppVersionsQuery.GetAppVersions?>()
        viewModelScope.launch {
            liveData.value = withContext(Dispatchers.IO){
                generalInfoRepository.checkAppVersion(platform)
            }
        }
        return liveData
    }
}
