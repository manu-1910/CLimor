package com.limor.app.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.apollo.CastsRepository
import com.limor.app.playlists.models.*
import com.limor.app.playlists.models.AddCastToPlaylistResponse
import com.limor.app.playlists.models.CreatePlaylistResponse
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.playlists.models.mapToUIModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlaylistsViewModel @Inject constructor(
    val castsRepository: CastsRepository
) : ViewModel() {

    fun getPlaylists(): LiveData<List<PlaylistUIModel?>?> {
        val liveData = MutableLiveData<List<PlaylistUIModel?>?>()
        viewModelScope.launch {
            try {
                val playlists = castsRepository.getPlaylists()?.map { it?.mapToUIModel() }
                liveData.postValue(playlists)
            } catch (throwable: Throwable) {
                liveData.postValue(listOf())
            }
        }
        return liveData
    }

    private var _playlistsResponse =
        MutableLiveData<PlaylistUIModel>()
    val playlistResponse: LiveData<PlaylistUIModel>
        get() = _playlistsResponse

    private var customPlaylists = mutableListOf<PlaylistUIModel>()

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

    fun editPlaylist(title: String, playlistId: Int): LiveData<EditPlaylistResponse>{
        val result = MutableLiveData<EditPlaylistResponse>()
        viewModelScope.launch {
            try{
                val response = castsRepository.editPlaylist(title, playlistId)
                result.postValue(EditPlaylistResponse(true, null))
            } catch (e: Exception){
                result.postValue(EditPlaylistResponse(false, e.localizedMessage))
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

    var playlistNewlySelectedIdsList: ArrayList<Int> = arrayListOf()
    var previouslySelectedPlaylistIds: ArrayList<Int> = arrayListOf()
    var playlistUnSelectedIdsList: ArrayList<Int> = arrayListOf()

    fun addCastToPlaylists(podcastId: Int): LiveData<AddCastToPlaylistResponse>{
        val liveData = MutableLiveData<AddCastToPlaylistResponse>()
        viewModelScope.launch {
            try{
                val response = castsRepository.addCastToPlaylist(podcastId, playlistNewlySelectedIdsList)
                liveData.postValue(AddCastToPlaylistResponse(true, null))
            } catch (e: Exception){
                liveData.postValue(AddCastToPlaylistResponse(false, e.localizedMessage))
            }
        }
        return liveData
    }

    fun handleCastInPlaylists(podcastId: Int): LiveData<AddCastToPlaylistResponse>{
        val liveData = MutableLiveData<AddCastToPlaylistResponse>()
        viewModelScope.launch {
            try{
                val response = castsRepository.handleCastInPlaylists(podcastId, playlistNewlySelectedIdsList, playlistUnSelectedIdsList)
                liveData.postValue(AddCastToPlaylistResponse(true, null))
            } catch (e: Exception){
                liveData.postValue(AddCastToPlaylistResponse(false, e.localizedMessage))
            }
        }
        return liveData
    }

}