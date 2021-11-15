package com.limor.app.scenes.profile

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.databinding.DialogCommentMoreActionsBinding
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.viewmodels.CommentActionType
import com.limor.app.scenes.main.viewmodels.HandleCommentActionsViewModel
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import dagger.android.support.AndroidSupportInjection
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import timber.log.Timber
import javax.inject.Inject

class DialogCommentMoreActions : DialogFragment() {

    companion object {
        const val KEY_COMMENT = "comment"
        const val KEY_PODCAST = "podcast"
        const val FROM = "from"
        const val ITEM = "item"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: HandleCommentActionsViewModel by activityViewModels { viewModelFactory }
    lateinit var binding: DialogCommentMoreActionsBinding

    lateinit var from: String
    lateinit var actionItem: String

    lateinit var comment: CommentUIModel
    lateinit var cast: CastUIModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCommentMoreActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        getArgs()
        setOnClicks()
        setViews()
    }

    private fun setViews() {
        binding.btnEditComment.visibility = if (commentIsEditable()) View.VISIBLE else View.GONE

        binding.btnDeleteComment.visibility = if (isOwnerOfComment() || isOwnerOfPodcast()) View.VISIBLE else View.GONE

        // The visibility of menu items when showing the menu for another user's comment
        val otherVisibility = if (isOwnerOfComment()) View.GONE else View.VISIBLE

        binding.btnBlockUser.visibility = otherVisibility
        binding.btnReportUser.visibility = otherVisibility
        binding.btnReportComment.visibility = otherVisibility

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    private fun getArgs() {
        arguments?.let {
            comment = it.getParcelable(KEY_COMMENT)!!
            cast = it.getParcelable(KEY_PODCAST)!!

            from = it.getString(FROM)!!
            actionItem = it.getString(ITEM)!!

            if (BuildConfig.DEBUG) {
                Timber.d("$it")
            }
        }
    }

    private fun onDeleteComment() {
        when (from) {
            "comment" -> {
                when (actionItem) {
                    "parent" -> model.actionDeleteParentComment(comment)
                    "child" -> model.actionDeleteChildComment(comment)
                }
            }
            "reply" -> {
                when (actionItem) {
                    "parent" -> model.actionDeleteParentReply(comment)
                    "child" -> model.actionDeleteChildReply(comment)
                }
            }
        }

        dismissAllowingStateLoss()
    }

    private fun showDeleteCommentAlert() {
        val message = if (isOwnerOfComment())
            getString(R.string.delete_comment_confirmation)
        else
            getString(
                R.string.delete_comment_confirmation__with_format,
                comment.user?.username ?: "user"
            )

        LimorDialog(layoutInflater).apply {
            // UI
            setTitle(R.string.delete_comment_title)
            setMessage(message)
            setMessageColor(ContextCompat.getColor(requireContext(), R.color.error_stroke_color))
            setIcon(R.drawable.ic_delete_cast)

            // Actions
            addButton(R.string.dialog_yes_button, false) { onDeleteComment() }
            addButton(R.string.cancel, true)
        }.show()
    }

    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { this.dismiss() }

        binding.btnEditComment.setOnClickListener {
            model.commentAction(comment, CommentActionType.Edit)
            this.dismiss()
        }

        binding.btnDeleteComment.setOnClickListener {
            showDeleteCommentAlert()
        }

        binding.btnReportUser.setOnClickListener {
            comment.user?.let {
                DialogUserReport.reportUser(it.id).show(parentFragmentManager, DialogUserReport.TAG)
            }

            dismiss()
        }

        binding.btnReportComment.setOnClickListener {
            DialogUserReport.reportComment(comment.id).show(parentFragmentManager, DialogUserReport.TAG)

            dismiss()
        }

        binding.btnBlockUser.setOnClickListener {
            blockUser()
        }
    }

    fun blockUser() {
        val userId = comment.user?.id ?: return

        alert(getString(R.string.confirmation_block_user)) {
            okButton {
                model.blockUser(userId)
                dismiss()
            }
            cancelButton {

            }
        }.show()
    }

    private fun commentIsEditable(): Boolean {
        return isOwnerOfComment() && comment.type == "text"
    }

    private fun isOwnerOfPodcast(): Boolean {
        return cast.owner?.id == PrefsHandler.getCurrentUserId(requireContext())
    }

    private fun isOwnerOfComment(): Boolean {
        if (BuildConfig.DEBUG) {
            Timber.d("${comment.user?.id}  -- ${PrefsHandler.getCurrentUserId(requireContext())}")
        }
        return comment.user?.id == PrefsHandler.getCurrentUserId(requireContext())
    }
}