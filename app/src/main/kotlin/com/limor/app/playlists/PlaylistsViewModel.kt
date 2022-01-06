package com.limor.app.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.playlists.models.PlaylistUIModel
import javax.inject.Inject

class PlaylistsViewModel @Inject constructor(

): ViewModel() {

    fun getPlaylists(): LiveData<List<PlaylistUIModel>> {
        val liveData = MutableLiveData<List<PlaylistUIModel>>()
        liveData.postValue(PlaylistUIModel.dummyList(10))
        return liveData
    }

}