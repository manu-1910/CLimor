package com.limor.app.scenes.profile

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.databinding.DialogCommentMoreActionsBinding
import com.limor.app.databinding.DialogOtherUserActionsBinding
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel
import com.limor.app.scenes.main.fragments.settings.SettingsActivity
import com.limor.app.scenes.main.viewmodels.CommentActionType
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main.viewmodels.HandleCommentActionsViewModel
import com.limor.app.scenes.main_new.fragments.DialogPodcastReportP2
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.uimodels.UserUIModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DialogCommentMoreActions : DialogFragment() {

    companion object {
        const val COMMENT_KEY = "comment"
        const val FROM = "from"
        const val ITEM = "item"
        const val KEY_CAN_EDIT_COMMENT = "KEY_CAN_EDIT_COMMENT"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: HandleCommentActionsViewModel by activityViewModels { viewModelFactory }
    lateinit var binding: DialogCommentMoreActionsBinding
    lateinit var args: CommentUIModel
    lateinit var from: String
    lateinit var actionItem: String

    private var canEdit = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        binding.btnEditComment.visibility = if (canEdit) View.VISIBLE else View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }
    private fun getArgs() {
        arguments?.let {
            args = it.getParcelable(COMMENT_KEY)!!
            from = it.getString(FROM)!!
            actionItem = it.getString(ITEM)!!
            canEdit = it.getBoolean(KEY_CAN_EDIT_COMMENT)
            Timber.d(args.toString())
        }
    }


    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { this.dismiss() }

        binding.btnEditComment.setOnClickListener {
            model.commentAction(args, CommentActionType.Edit)
            this.dismiss()
        }

        binding.btnDeleteComment.setOnClickListener {
            when (from) {
                "comment" -> {
                    when (actionItem) {
                        "parent" -> model.actionDeleteParentComment(args)
                        "child" -> model.actionDeleteChildComment(args)
                    }
                }
                "reply" -> {
                    when (actionItem) {
                        "parent" -> model.actionDeleteParentReply(args)
                        "child" -> model.actionDeleteChildReply(args)
                    }
                }
            }

            dismissAllowingStateLoss()

        }
        binding.btnReportUser.setOnClickListener {
            DialogUserReport.newInstance(args.id)
                .show(parentFragmentManager, DialogUserReport.TAG)
            dismiss()
        }
    }
}