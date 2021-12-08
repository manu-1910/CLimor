package com.limor.app.scenes.main.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.apollo.PublishRepository
import com.limor.app.common.SingleLiveEvent
import com.limor.app.type.CreatePodcastInput
import com.limor.app.uimodels.*
import com.limor.app.usecases.InAppPricesUseCase
import com.limor.app.usecases.PublishUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


class PublishViewModel @Inject constructor(
    private val publishUseCase: PublishUseCase,
    private val publishRepository: PublishRepository,
    private val generalInfoRepository: GeneralInfoRepository,
    private val inAppPricesUseCase: InAppPricesUseCase,
) : ViewModel() {

    private val _inAppPricesData = MutableLiveData<List<String?>>()
    val inAppPricesData: LiveData<List<String?>>
        get() = _inAppPricesData
    var uiPublishRequest = UIPublishRequest(
        podcast = null
    )
    private val _publishResponseData = MutableLiveData<String?>()
    val publishResponseData: LiveData<String?>
        get() = _publishResponseData
    var categorySelected: String = ""
    var categorySelectedId: Int = -1
    var categorySelectedIdsList: ArrayList<Int> = arrayListOf()
    var categorySelectedNamesList: ArrayList<UISimpleCategory> = arrayListOf()
    var languageSelectedCodesList: ArrayList<String> = arrayListOf()
    var languageSelected: String = ""
    var languageCode: String = ""
    var languageSelectedId: Int = 0
    val tags = arrayListOf<String>()

    private val compositeDispose = CompositeDisposable()

    data class Input(
        val publishTrigger: Observable<Unit>,
    )

    data class Output(
        val response: LiveData<UIPublishResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>,
    )

    fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIPublishResponse>()

        /*input.publishTrigger.subscribe({
            publishUseCase.execute(uiPublishRequest).subscribe({
                //sessionManager.storeToken(it.data.access_token.token.access_token)
                response.value = it
            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? = error.response()?.errorBody()?.parseSuccessResponse(
                        UIErrorResponse.serializer())
                    errorTracker.postValue(errorResponse!!)
                } catch (e: Exception) {
                    //val dataError = UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
                    //val errorResponse = UIErrorResponse(99, dataError.toString())
                    //errorTracker.postValue(errorResponse!!)
                    e.printStackTrace()
                }

            })
        }, {}).addTo(compositeDispose)*/

        return Output(response, backgroundWorkingProgress, errorTracker)
    }

    override fun onCleared() {
        if (!compositeDispose.isDisposed) compositeDispose.dispose()
        super.onCleared()
    }

    suspend fun createPodcast(podcast: CreatePodcastInput): String? {

        return withContext(Dispatchers.IO) {
            try {
                val response = publishRepository.createPodcast(podcast)
                // _publishResponseData.value = response
                response
            } catch (e: Exception) {
                Timber.e(e)
                // _publishResponseData.value = null
                null
            }

        }
    }

    suspend fun updatePodcast(podcastId: Int, title: String, caption: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response = publishRepository.updatePodcast(podcastId, title, caption)
                response
            } catch (e: Exception) {
                null
            }
        }
    }


    fun consumePurchasedSub(purchase: Purchase): LiveData<String?> {
        val liveData = MutableLiveData<String?>()
        viewModelScope.launch(Dispatchers.IO) {
            val status = publishRepository.updateSubscriptionDetails(purchase)
            liveData.postValue(status)
        }
        return liveData
    }

    fun addPatronCategories(): LiveData<String?> {
        val liveData = MutableLiveData<String?>()
        viewModelScope.launch(Dispatchers.IO) {
            val status = publishRepository.addPatronCategories(categorySelectedIdsList)
            liveData.postValue(status)
        }
        return liveData
    }


    fun addPatronLanguages(): LiveData<String?> {
        val liveData = MutableLiveData<String?>()
        viewModelScope.launch(Dispatchers.IO) {
            val status = publishRepository.addPatronLanguages(languageSelectedCodesList)
            liveData.postValue(status)
        }
        return liveData
    }

    fun getPlanIds(): LiveData<List<String>> {
        val liveData = MutableLiveData<List<String>>()
        viewModelScope.launch(Dispatchers.IO) {
            val ids = publishRepository.getPlans()?.mapNotNull { it?.productId }
            liveData.postValue(ids ?: listOf())
        }
        return liveData
    }

    fun getInAppPriceTiers(){
        viewModelScope.launch {
            inAppPricesUseCase.executeInAppProductTiers()
                .onSuccess {
                    _inAppPricesData.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while getting in app prices")
                }
        }
    }


    fun getUserProfile(): LiveData<UserUIModel?> {
        val liveData = MutableLiveData<UserUIModel?>()
        viewModelScope.launch {
            liveData.postValue(generalInfoRepository.getUserProfile())
        }
        return liveData
    }

}