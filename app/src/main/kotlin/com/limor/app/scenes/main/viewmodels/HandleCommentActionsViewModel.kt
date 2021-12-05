package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.auth_new.model.UserInfoProvider
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.usecases.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CommentActionType {
    Edit, Delete
}

data class CommentAction(
    val comment: CommentUIModel,
    val type: CommentActionType
)

class HandleCommentActionsViewModel @Inject constructor(
    private val deleteCommentUseCase: DeleteCommentUseCaseNew,
    private val userInfoProvider: UserInfoProvider
): ViewModel() {

    private val _actionDelete = MutableLiveData<CommentUIModel?>()
    val actionDelete: LiveData<CommentUIModel?> get() = _actionDelete

    private val _actionDeleteChild = MutableLiveData<CommentUIModel?>()
    val actionDeleteChild: LiveData<CommentUIModel?> get() = _actionDeleteChild

    private val _actionDeleteParentReply = MutableLiveData<CommentUIModel?>()
    val actionDeleteParentReply: LiveData<CommentUIModel?> get() = _actionDeleteParentReply

    private val _actionDeleteChildReply = MutableLiveData<CommentUIModel?>()
    val actionDeleteChildReply: LiveData<CommentUIModel?> get() = _actionDeleteChildReply

    private val _commentAction = MutableLiveData<CommentAction?>()
    val actionComment: LiveData<CommentAction?> get() = _commentAction

    fun commentAction(comment: CommentUIModel, action: CommentActionType) {
        _commentAction.value = CommentAction(comment, action)
    }

    fun actionDeleteParentComment(args: CommentUIModel) {
        _actionDelete.value = args
    }

    fun actionDeleteChildComment(args: CommentUIModel) {
        _actionDeleteChild.value = args
    }

    fun actionDeleteChildReply(args: CommentUIModel) {
        _actionDeleteChildReply.value = args
    }

    fun actionDeleteParentReply(args: CommentUIModel) {
        _actionDeleteParentReply.value = args
    }

    fun resetCommentAction() {
        _commentAction.value = null;
    }

    fun blockUser(userId: Int): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        viewModelScope.launch {
            userInfoProvider.blockUser(userId)
            liveData.postValue(true)
        }
        return liveData
    }
}