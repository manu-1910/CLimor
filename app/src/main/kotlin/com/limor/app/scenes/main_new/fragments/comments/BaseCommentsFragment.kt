package com.limor.app.scenes.main_new.fragments.comments

import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main.viewmodels.HandleCommentActionsViewModel
import com.limor.app.scenes.main_new.fragments.mentions.UserMentionPopup
import com.limor.app.scenes.main_new.view_model.UserMentionViewModel
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.SendData
import com.limor.app.scenes.utils.TextAndVoiceInput
import com.limor.app.service.VoiceUploadCompletion
import com.limor.app.service.VoiceUploadProgress
import com.limor.app.uimodels.CommentUIModel
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

import org.greenrobot.eventbus.ThreadMode

abstract class UserMentionFragment : BaseFragment(), UserMentionPopup.UserMentionData {

    private lateinit var mentionPopup: UserMentionPopup

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val userModel: UserMentionViewModel by viewModels { viewModelFactory }

    protected val commentsViewModel: CommentsViewModel by viewModels { viewModelFactory }
    protected val actionsViewModel: HandleCommentActionsViewModel by activityViewModels { viewModelFactory }

    protected var textAndVoiceInput: TextAndVoiceInput? = null

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    abstract fun reload()

    protected fun subscribeCommons() {
        commentsViewModel.reload.observe(viewLifecycleOwner) {
            if (it) {
                reload()
                textAndVoiceInput?.reset()
            }
        }
    }

    protected fun commentIsEditable(comment: CommentUIModel): Boolean {
        return isOwnerOf(comment) && comment.type == "text"
    }

    protected fun isOwnerOf(comment: CommentUIModel): Boolean {
        if (BuildConfig.DEBUG) {
            Timber.d("${comment.user?.id}  -- ${PrefsHandler.getCurrentUserId(requireContext())}")
        }
        return comment.user?.id == PrefsHandler.getCurrentUserId(requireContext())
    }

    protected fun editComment(comment: CommentUIModel) {
        textAndVoiceInput?.edit(comment)
        actionsViewModel.resetCommentAction()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(uploadProgress: VoiceUploadProgress) {
        textAndVoiceInput?.progress = uploadProgress.progress
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(completion: VoiceUploadCompletion) {
        if (completion.success) {
            reload()
        } else {
            reportError(getString(R.string.could_not_save_comment))
        }
        textAndVoiceInput?.reset()
    }

    protected fun uploadWithAudio(inputStatus: SendData, castId: Int, ownerId: Int, ownerType: String) {
        commentsViewModel.uploadVoiceComment(
            inputStatus = inputStatus,
            podcastId = castId,
            ownerId = ownerId,
            ownerType = ownerType
        )
    }

    protected fun setUpPopup(editText: EditText, editArea: View) {
        mentionPopup = UserMentionPopup(editText, this)
        userModel.userMentionData.observe(viewLifecycleOwner) { users ->
            mentionPopup.setUsers(users)
        }

        mentionPopup.inputHeight = resources.getDimension(R.dimen.commentFooterHeight).toInt()
    }

    override fun search(text: String) {
        userModel.search(text)
    }

    fun reportError(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadConvertedCommentAudio(convertedFilePath: String, onDone: (audioUrl: String?) -> Unit) {
        Commons.getInstance().uploadAudio(
            context,
            File(convertedFilePath),
            Constants.AUDIO_TYPE_COMMENT,
            object : Commons.AudioUploadCallback {
                override fun onSuccess(audioUrl: String?) {
                    if (BuildConfig.DEBUG) {
                        println("Audio upload to AWS to $audioUrl")
                    }
                    onDone.invoke(audioUrl)
                }

                override fun onProgressChanged(
                    id: Int,
                    bytesCurrent: Long,
                    bytesTotal: Long
                ) {
                }

                override fun onError(error: String?) {
                    if (BuildConfig.DEBUG) {
                        Timber.d("Audio upload to AWS error: $error")
                    }
                    reportError(error ?: getString(R.string.could_not_upload_audio_message))
                }
            })
    }

    protected fun uploadVoiceComment(filePath: String?, onDone: (audioUrl: String?) -> Unit) {
        val path = filePath ?: return
        if (BuildConfig.DEBUG) {
            println("Will upload $filePath")
        }
        uploadConvertedCommentAudio(path, onDone)
    }
}