package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.common.SingleLiveEvent
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.usecases.AddCommentUseCase
import com.limor.app.usecases.GetCommentsForPodcastUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommentsViewModel @Inject constructor(
    private val getCommentsForPodcastUseCase: GetCommentsForPodcastUseCase,
    private val addCommentUseCase: AddCommentUseCase,
) : ViewModel() {

    private val _comments = MutableLiveData<List<CommentUIModel>>()
    val comments: LiveData<List<CommentUIModel>> get() = _comments

    private val _commentAddEvent = SingleLiveEvent<Int>()
    val commentAddEvent: LiveData<Int> get() = _commentAddEvent

    fun loadComments(podcastId: Int, limit: Int = Int.MAX_VALUE, offset: Int = 0) {
        viewModelScope.launch {
            getCommentsForPodcastUseCase.execute(castId = podcastId, limit = limit, offset = offset)
                .onSuccess {
                    _comments.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while fetching comments for castId = $podcastId")
                }
        }
    }

    /**
     * @param ownerId - podcast or parent comment id
     * @param ownerType - [CommentUIModel.OWNER_TYPE_COMMENT] or [CommentUIModel.OWNER_TYPE_PODCAST]
     */
    fun addComment(
        podcastId: Int,
        content: String,
        ownerId: Int,
        ownerType: String
    ) {
        viewModelScope.launch {
            addCommentUseCase.execute(podcastId, content, ownerId, ownerType)
                .onFailure {
                    Timber.e(it, "Error while creating comment")
                }
                .onSuccess {
                    _commentAddEvent.value = it
                }
        }
    }
}