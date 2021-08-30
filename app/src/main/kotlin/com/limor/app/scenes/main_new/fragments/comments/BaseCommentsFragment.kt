package com.limor.app.scenes.main_new.fragments.comments

import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.audio.wav.WavHelper
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main_new.fragments.mentions.UserMentionPopup
import com.limor.app.scenes.main_new.view_model.UserMentionViewModel
import com.limor.app.scenes.utils.Commons
import com.limor.app.uimodels.CommentUIModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

abstract class UserMentionFragment : BaseFragment(), UserMentionPopup.UserMentionData {

    private lateinit var mentionPopup: UserMentionPopup

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val userModel: UserMentionViewModel by viewModels { viewModelFactory }
    protected val commentsViewModel: CommentsViewModel by viewModels { viewModelFactory }

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
            println("Will convert $filePath")
        }
        lifecycleScope.launch {
            val convertedFilePath = WavHelper.convertWavFileToM4aFile(requireContext(), path)?.absolutePath
            if (convertedFilePath.isNullOrEmpty()) {
                reportError(getString(R.string.could_not_convert_audio))
            } else {
                uploadConvertedCommentAudio(convertedFilePath, onDone)
            }
        }
    }
}