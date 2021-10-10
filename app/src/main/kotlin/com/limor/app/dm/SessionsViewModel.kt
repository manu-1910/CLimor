package com.limor.app.dm

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import javax.inject.Inject

class SessionsViewModel @Inject constructor(
    chatRepository: ChatRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val sessions: LiveData<List<ChatSessionWithUser>> = chatRepository.getSessions().asLiveData()

}