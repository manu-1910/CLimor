package com.limor.app.scenes.patron.manage.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.auth_new.model.UserInfoProvider
import javax.inject.Inject

class ManagePatronViewModel @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository,
    val userInfoProvider: UserInfoProvider
) : ViewModel() {

}