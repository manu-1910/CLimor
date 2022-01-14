package com.limor.app.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.apollo.CastsRepository
import com.limor.app.playlists.models.AddCastToPlaylistResponse
import com.limor.app.playlists.models.CreatePlaylistResponse
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.playlists.models.mapToUIModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlaylistsViewModel @Inject constructor(
    val castsRepository: CastsRepository
): ViewModel() {

    private var _playlistsResponse =
        MutableLiveData<PlaylistUIModel>()
    val playlistResponse: LiveData<PlaylistUIModel>
        get() = _playlistsResponse

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

    fun createPlaylist(title: String, podcastId: Int): LiveData<CreatePlaylistResponse>{
        val result = MutableLiveData<CreatePlaylistResponse>()
        viewModelScope.launch {
            try{
                val response = castsRepository.createPlaylist(title, podcastId)
                result.postValue(CreatePlaylistResponse(true, null))
            } catch (e: Exception){
                result.postValue(CreatePlaylistResponse(false, e.localizedMessage))
            }
        }
        return result
    }

    fun getPlaylistsOfCasts(podcastId: Int): LiveData<List<PlaylistUIModel?>?>{
        val liveData = MutableLiveData<List<PlaylistUIModel?>?>()
        viewModelScope.launch {
            liveData.postValue(
                castsRepository.getCastsOfPlaylist(podcastId)?.map { it -> it?.mapToUIModel() })
        }
        return liveData
    }

    var playlistSelectedIdsList: ArrayList<Int> = arrayListOf()

    fun addCastToPlaylists(podcastId: Int): LiveData<AddCastToPlaylistResponse>{
        val liveData = MutableLiveData<AddCastToPlaylistResponse>()
        viewModelScope.launch {
            try{
                val response = castsRepository.addCastToPlaylist(podcastId, playlistSelectedIdsList)
                liveData.postValue(AddCastToPlaylistResponse(true, null))
            } catch (e: Exception){
                liveData.postValue(AddCastToPlaylistResponse(false, e.localizedMessage))
            }
        }
        return liveData
    }

}