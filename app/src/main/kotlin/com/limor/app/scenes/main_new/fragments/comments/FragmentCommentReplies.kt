package com.limor.app.scenes.main_new.fragments.comments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentCommentRepliesBinding
import com.limor.app.extensions.dismissFragment
import com.limor.app.extensions.highlight
import com.limor.app.extensions.showKeyboard
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.extensions.userMentionPattern
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentChildItem
import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentParentItem
import com.limor.app.scenes.main_new.fragments.mentions.UserMentionPopup
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.MissingPermissions
import com.limor.app.scenes.utils.SendData
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.util.requestRecordPermissions
import com.xwray.groupie.GroupieAdapter
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FragmentCommentReplies : UserMentionFragment() {

    companion object {
        val TAG = FragmentCommentReplies::class.qualifiedName
        private const val CAST_ID_KEY = "CAST_ID_KEY"
        private const val PARENT_COMMENT_ID_KEY = "PARENT_COMMENT_ID_KEY"
        private const val CHILD_REPLY_COMMENT_ID_KEY = "CHILD_REPLY_COMMENT_ID_KEY"
        fun newInstance(
            castId: Int,
            parentCommentId: Int,
            replyToCommentId: Int? = null
        ): FragmentCommentReplies {
            return FragmentCommentReplies().apply {
                arguments = bundleOf(
                    CAST_ID_KEY to castId,
                    PARENT_COMMENT_ID_KEY to parentCommentId,
                    CHILD_REPLY_COMMENT_ID_KEY to replyToCommentId,
                )
            }
        }
    }

    private val castId: Int by lazy { requireArguments().getInt(CAST_ID_KEY) }
    private val parentCommentId: Int by lazy { requireArguments().getInt(PARENT_COMMENT_ID_KEY) }
    private var replyToCommentId: Int? = null

    private var _binding: FragmentCommentRepliesBinding? = null
    private val binding get() = _binding!!

    private val adapter = GroupieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentRepliesBinding.inflate(inflater, container, false)

        replyToCommentId = requireArguments().getInt(CHILD_REPLY_COMMENT_ID_KEY, -1)
            .takeIf { it != -1 }

        initViews()
        subscribeForComments()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpPopup(binding.taviVoice.editText, binding.taviVoice)
    }

    private fun initViews() {
        binding.commentsList.adapter = adapter
        binding.closeBtn.setOnClickListener {
            parentFragment?.dismissFragment()
        }
        binding.backBtn.setOnClickListener {
            dismissFragment()
        }
        binding.tvCancel.setOnClickListener {
            dismissFragment()
        }
        binding.taviVoice.initListenerStatus {
            when (it) {
                is MissingPermissions -> requestRecordPermissions(requireActivity())
                is SendData -> {
                    if (it.filePath != null) {
                        uploadVoiceComment(it.filePath) { audioUrl ->
                            commentsViewModel.addComment(
                                castId,
                                it.text,
                                ownerId = parentCommentId,
                                ownerType = CommentUIModel.OWNER_TYPE_COMMENT,
                                audioURI = audioUrl,
                                duration = it.duration
                            )
                        }
                    } else {
                        commentsViewModel.addComment(
                            castId,
                            it.text,
                            ownerId = parentCommentId,
                            ownerType = CommentUIModel.OWNER_TYPE_COMMENT
                        )
                    }

                }
                else -> {

                }
            }
        }
        commentsViewModel.loadCommentById(parentCommentId)
    }

    private fun fillList(parentComment: CommentUIModel) {
        adapter.clear()
        adapter.add(
            CommentParentItem(
                parentComment,
                onReplyClick = ::onReplyClick,
                onLikeClick = { comment, liked ->
                    viewModel.likeComment(comment, liked)
                },
                onUserMentionClick = { username, userId ->
                    context?.let { context -> UserProfileActivity.show(context, username, userId) }
                }
            )
        )
        parentComment.innerComments.forEach { childComment ->
            adapter.add(
                CommentChildItem(
                    parentComment,
                    childComment,
                    onReplyClick = { _, replyChildComment -> onReplyClick(replyChildComment) },
                    isSimplified = false,
                    onLikeClick = { comment, liked ->
                        viewModel.likeComment(comment, liked)
                    },
                    onUserMentionClick = { username, userId ->
                        context?.let { context -> UserProfileActivity.show(context, username, userId) }
                    }
                )
            )
        }
    }

    private fun subscribeForComments() {
        commentsViewModel.comment.observe(viewLifecycleOwner) { updatedComment ->
            if (updatedComment != null) {
                fillList(updatedComment)

                if (replyToCommentId != null) {
                    onReplyClick(
                        if (updatedComment.id == replyToCommentId) {
                            updatedComment
                        } else {
                            updatedComment.innerComments.first { it.id == replyToCommentId }
                        }
                    )
                    replyToCommentId = null
                }
            } else {
                dismissFragment()
            }
        }

        commentsViewModel.commentAddEvent.observe(viewLifecycleOwner) {
            if (it == -1) {
                reportError(getString(R.string.could_not_save_comment))
            } else {
                commentsViewModel.loadCommentById(parentCommentId)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onReplyClick(comment: CommentUIModel) {
        val username = comment.user?.username ?: return

        binding.tvName.text = username
        binding.taviVoice.editText.apply {
            val newText = if (text.isNotEmpty()) "$text @$username " else "@$username "
            setText(newText)
            highlight(UserMentionPopup.userMentionPattern, R.color.waveFormColor)
            setSelection(newText.length)
            requestFocus()
            showKeyboard()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
