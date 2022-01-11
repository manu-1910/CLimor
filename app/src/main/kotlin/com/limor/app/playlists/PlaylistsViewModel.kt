package com.limor.app.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.apollo.CastsRepository
import com.limor.app.playlists.models.PlaylistUIModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlaylistsViewModel @Inject constructor(
    val castsRepository: CastsRepository
): ViewModel() {

    private var _errorLiveData =
        MutableLiveData<String>()
    val errorLiveData: LiveData<String>
        get() = _errorLiveData

    private var _createPlayListResponse =
        MutableLiveData<String?>()
    val createPlaylistResponse: LiveData<String?>
        get() = _createPlayListResponse

    private var customPlaylists = mutableListOf<PlaylistUIModel>()

    fun getPlaylists(): LiveData<List<PlaylistUIModel>> {
        val liveData = MutableLiveData<List<PlaylistUIModel>>()
        val list = ArrayList<PlaylistUIModel>()
        list.addAll(customPlaylists)
        list.addAll(PlaylistUIModel.dummyList(10))
        liveData.postValue(list)
        return liveData
    }

    fun addToCustomPlaylist(model: PlaylistUIModel){
        customPlaylists.add(model)
    }

    fun createPlaylist(title: String, podcastId: Int){
        viewModelScope.launch {
            try {
                val response = castsRepository.createPlaylist(title, podcastId)
                _createPlayListResponse.postValue(response)
            } catch (e: Exception) {
                _errorLiveData.postValue(e.localizedMessage)
            }
        }
    }

}