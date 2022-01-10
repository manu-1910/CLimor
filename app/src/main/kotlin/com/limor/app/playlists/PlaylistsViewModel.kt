package com.limor.app.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.playlists.models.PlaylistUIModel
import javax.inject.Inject

class PlaylistsViewModel @Inject constructor(

): ViewModel() {

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

}