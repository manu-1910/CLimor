package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.usecases.*
import javax.inject.Inject

class HandleCommentActionsViewModel @Inject constructor(
    private val deleteCommentUseCase: DeleteCommentUseCaseNew,
): ViewModel() {

    private val _actionDelete = MutableLiveData<CommentUIModel?>()
    val actionDelete: LiveData<CommentUIModel?> get() = _actionDelete

    private val _actionDeleteChild = MutableLiveData<CommentUIModel?>()
    val actionDeleteChild: LiveData<CommentUIModel?> get() = _actionDeleteChild

    private val _actionDeleteParentReply = MutableLiveData<CommentUIModel?>()
    val actionDeleteParentReply: LiveData<CommentUIModel?> get() = _actionDeleteParentReply

    private val _actionDeleteChildReply = MutableLiveData<CommentUIModel?>()
    val actionDeleteChildReply: LiveData<CommentUIModel?> get() = _actionDeleteChildReply


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
}