package com.limor.app.scenes.patron.manage.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.common.Constants
import com.limor.app.scenes.auth_new.model.UserInfoProvider
import com.limor.app.uimodels.CastUIModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ManagePatronViewModel @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository,
    val userInfoProvider: UserInfoProvider
) : ViewModel() {

    private val buyers = mutableListOf<String>("In", "computer", "programming", "a", "string", "is", "traditionally", "a", "sequence", "of",
            "characters", "either", "as", "a", "literal", "constant", "or", "as", "some", "kind",
        "of", "variable", "The", "latter", "may", "allow", "its", "elements", "to", "be", "mutated", "and", "the", "length", "changed")

    private val _buyersData = MutableLiveData<List<String>>()
    val buyersData: LiveData<List<String>> get() = _buyersData

    private val _earningsData = MutableLiveData<List<String>>()
    val earningsData: LiveData<List<String>> get() = _earningsData

    private val _priceChangeResult = MutableLiveData<Boolean>()
    val priceChangeResult: LiveData<Boolean> get() = _priceChangeResult

    fun loadCastBuyers(offset: Int = 0, limit: Int = 10){
        if(offset < buyers.size){
            _buyersData.postValue(buyers.subList(offset, if(offset + limit < buyers.size) offset + limit else buyers.size).toList())
        }
    }

    fun loadCastEarnings(offset: Int = 0, limit: Int = 10){
        if(offset < buyers.size){
            _earningsData.postValue(buyers.subList(offset, if(offset + limit < buyers.size) offset + limit else buyers.size).toList())
        }
    }

    fun updateCastPrice(){
        viewModelScope.launch {
            _priceChangeResult.postValue(true)
            delay(1000)
            _priceChangeResult.postValue(false)
        }
    }

}