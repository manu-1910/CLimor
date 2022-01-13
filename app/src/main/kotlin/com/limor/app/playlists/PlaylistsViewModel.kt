package com.limor.app.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.apollo.CastsRepository
import com.limor.app.playlists.models.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlaylistsViewModel @Inject constructor(
    val castsRepository: CastsRepository
) : ViewModel() {

    fun getPlaylists(): LiveData<List<PlaylistUIModel?>?> {
        val liveData = MutableLiveData<List<PlaylistUIModel?>?>()
        viewModelScope.launch {
            liveData.postValue(
                castsRepository.getPlaylists()?.map { it -> it?.mapToUIModel() })
        }
        return liveData
    }

    fun getDummyPlaylists(): LiveData<List<PlaylistUIModel?>?> {
        val liveData = MutableLiveData<List<PlaylistUIModel?>?>()
        val list = ArrayList<PlaylistUIModel>()
        list.addAll(PlaylistUIModel.dummyList(10))
        liveData.postValue(list)
        return liveData
    }

    fun getCastsInPlaylist(playlistId: Int): LiveData<List<PlaylistCastUIModel>> {
        val liveData = MutableLiveData<List<PlaylistCastUIModel>>()
        viewModelScope.launch {
            val casts = castsRepository.getCastsInPlaylist(playlistId)
                .mapNotNull { it -> it?.mapToUIModel() }
            liveData.postValue(casts)
        }
        return liveData
    }

    fun deleteCastInPlaylist(playlistId: Int, podcastId: Int): LiveData<DeleteCastInPlaylistResponse>{
        val result = MutableLiveData<DeleteCastInPlaylistResponse>()
        viewModelScope.launch {
            try{
                val response = castsRepository.deleteCastInPlaylist(playlistId, podcastId)
                result.postValue(DeleteCastInPlaylistResponse(true, null))
            } catch (e: Exception){
                result.postValue(DeleteCastInPlaylistResponse(false, e.localizedMessage))
            }
        }
        return result
    }

    fun deletePlaylist(playlistId: Int): LiveData<DeletePlaylistResponse>{
        val result = MutableLiveData<DeletePlaylistResponse>()
        viewModelScope.launch {
            try{
                val response = castsRepository.deletePlaylist(playlistId)
                result.postValue(DeletePlaylistResponse(true, null))
            } catch (e: Exception){
                result.postValue(DeletePlaylistResponse(false, e.localizedMessage))
            }
        }
        return result
    }

}