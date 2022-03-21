package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.limor.app.apollo.CastsRepository
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.main_new.fragments.DataItem
import com.limor.app.uimodels.FeaturedPodcastGroups
import com.limor.app.uimodels.mapToUIModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.limor.app.apollo.FollowRepository
import com.limor.app.scenes.main_new.pagingsources.HomeFeedPagingSource
import com.limor.app.uimodels.UserUIModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class HomeFeedViewModel @Inject constructor(
    private val repository: GeneralInfoRepository,
    private val castsRepository: CastsRepository,
    private val followRepository: FollowRepository
) : ViewModel() {

    private var lastPagingSourceFactory: HomeFeedPagingSource? = null

    private val _podcastGroups = MutableLiveData<FeaturedPodcastGroups?>()
    var podcastGroups = _podcastGroups

    fun getFeaturedPodcastsGroups(): LiveData<FeaturedPodcastGroups?> {
        viewModelScope.launch {
            try {
                val podcastGroups = castsRepository.getFeaturedPodCastsGroups("HOME_FEED")?.mapToUIModel()
                _podcastGroups.postValue(podcastGroups)
            } catch (throwable: Throwable) {
                _podcastGroups.postValue(null)
            }
        }
        return podcastGroups
    }

    fun getHomeFeed(): Flow<PagingData<DataItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20),
            pagingSourceFactory = { getSource() }
        ).flow.cachedIn(viewModelScope).map { pagingData -> pagingData.map { it as DataItem } }
    }

    private fun getSource(): HomeFeedPagingSource {
        return HomeFeedPagingSource(
            repository = repository,
            castsRepository = castsRepository,
            featuredPodcastGroups = _podcastGroups.value
        ).also {
            lastPagingSourceFactory = it
        }
    }

    fun getRecastedUsers(podcastId: Int): MutableLiveData<List<UserUIModel>> {
        val recasters = MutableLiveData<List<UserUIModel>>()
        viewModelScope.launch {
            val users = repository.getRecastedUsers(podcastId).map { user -> user.mapToUIModel() }
            recasters.postValue(users)
        }
        return recasters
    }

    fun followUser(account: UserUIModel, follow: Boolean) {
        viewModelScope.launch {
            try {
                if(follow){
                    followRepository.followUser(account.id)
                } else{
                    followRepository.unFollowUser(account.id)
                }
            } catch (ex: Exception) {
                Timber.e(ex, "Error while following person: $account")
            }
        }
    }

    fun searchUsers(){

    }

    fun invalidate() {
        lastPagingSourceFactory?.invalidate()
    }
}
