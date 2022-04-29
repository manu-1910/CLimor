package com.limor.app.scenes.main.fragments.profile.casts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.limor.app.apollo.CastsRepository
import com.limor.app.scenes.main_new.pagingsources.UserCastsPagingSource
import com.limor.app.scenes.main_new.pagingsources.UserCastsPagingSource.Companion.PATRON_CASTS
import com.limor.app.scenes.main_new.pagingsources.UserCastsPagingSource.Companion.PURCHASED_CASTS
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.GetPatronPodcastsUseCase
import com.limor.app.usecases.GetPodcastsByUserUseCase
import com.limor.app.usecases.LikePodcastUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserPodcastsViewModel @Inject constructor(
    private val likePodcastUseCase: LikePodcastUseCase,
    private val getPodcastsByUserUseCase: GetPodcastsByUserUseCase,
    private val getPatronPodcastsUseCase: GetPatronPodcastsUseCase,
    private val castsRepository: CastsRepository
) : ViewModel() {

    private val _casts = MutableLiveData<List<CastUIModel>>()
    val casts: LiveData<List<CastUIModel>> get() = _casts

    private val _patronCasts = MutableLiveData<List<CastUIModel>>()
    val patronCasts: LiveData<List<CastUIModel>> get() = _patronCasts

    private val _purchasedCasts = MutableLiveData<List<CastUIModel>>()
    val purchasedCasts: LiveData<List<CastUIModel>> get() = _purchasedCasts

    private var userPatronCastsPagingSource: UserCastsPagingSource? = null
    private var userCastsPagingSource: UserCastsPagingSource? = null
    private var purchasedCastsPagingSource: UserCastsPagingSource? = null

    fun loadCasts(
        userId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ) {
        viewModelScope.launch {
            Timber.d("casts -> $userId $limit $offset")
            getPodcastsByUserUseCase.execute(userId, limit, offset)
                .onSuccess {
                    _casts.value = it
                }
                .onFailure {
                    _casts.value = emptyList()
                    Timber.e(it, "Error while loading user casts")
                }
        }
    }

    fun loadPatronCasts(
        userId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ) {
        viewModelScope.launch {
            Timber.d("patron casts -> $userId $limit $offset")
            getPatronPodcastsUseCase.execute(userId, limit, offset)
                .onSuccess {
                    Timber.d("patron casts -> $it")
                    _patronCasts.value = it
                }
                .onFailure {
                    _patronCasts.value = emptyList()
                    Timber.e(it, "Error while loading user casts")
                }
        }
    }

    fun likeCast(cast: CastUIModel, like: Boolean) {
        viewModelScope.launch {
            runCatching {
                likePodcastUseCase.execute(cast.id, like)
            }.onFailure {
                Timber.e(it, "Error while liking cast")
            }
        }
    }

    fun loadPurchasedCasts(
        userId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ){
        viewModelScope.launch {
            getPodcastsByUserUseCase.getPurchasedCasts(userId, limit, offset)
                .onSuccess {
                    _purchasedCasts.postValue(it)
                }
                .onFailure {
                    _purchasedCasts.postValue(emptyList())
                    Timber.e(it, "Error while loading purchased casts")
                }
        }
    }

    fun getUserCasts(userId: Int): Flow<PagingData<CastUIModel>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { getSource(userId) }
        ).flow.cachedIn(viewModelScope)
    }

    fun getPatronCasts(userId: Int): Flow<PagingData<CastUIModel>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { getSource(userId, castType = PATRON_CASTS) }
        ).flow.cachedIn(viewModelScope)
    }

    fun getPurchases(userId: Int): Flow<PagingData<CastUIModel>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { getSource(userId, castType = PURCHASED_CASTS) }
        ).flow.cachedIn(viewModelScope)
    }

    private fun getSource(userId: Int, castType: Int = 1): UserCastsPagingSource {
        return UserCastsPagingSource(
            userId = userId,
            castsRepository = castsRepository,
            castsType = castType
        ).also {
            when(castType){
                PATRON_CASTS -> userPatronCastsPagingSource = it
                PURCHASED_CASTS -> purchasedCastsPagingSource = it
                else -> userCastsPagingSource = it
            }
        }
    }

    fun invalidateUserCasts() {
        userCastsPagingSource?.invalidate()
    }

    fun invalidatePatronCasts(){
        userPatronCastsPagingSource?.invalidate()
    }

    fun invalidatePurchasedCasts(){
        purchasedCastsPagingSource?.invalidate()
    }

}
