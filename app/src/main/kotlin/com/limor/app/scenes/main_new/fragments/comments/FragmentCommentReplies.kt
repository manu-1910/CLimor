package com.limor.app.scenes.main_new.fragments.comments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.databinding.FragmentCommentRepliesBinding
import com.limor.app.extensions.dismissFragment
import com.limor.app.extensions.highlight
import com.limor.app.extensions.showKeyboard
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.viewmodels.CommentActionType
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main.viewmodels.HandleCommentActionsViewModel
import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentChildItem
import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentParentItem
import com.limor.app.scenes.main_new.fragments.mentions.UserMentionPopup
import com.limor.app.scenes.profile.DialogCommentMoreActions
import com.limor.app.scenes.utils.MissingPermissions
import com.limor.app.scenes.utils.SendData
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.util.requestRecordPermissions
import com.xwray.groupie.GroupieAdapter

class FragmentCommentReplies : UserMentionFragment() {

    companion object {
        val TAG = FragmentCommentReplies::class.qualifiedName
        private const val CAST_ID_KEY = "CAST_ID_KEY"
        private const val CAST_KEY = "CAST_KEY"
        private const val PARENT_COMMENT_KEY = "PARENT_COMMENT_KEY"
        private const val PARENT_COMMENT_ID_KEY = "PARENT_COMMENT_ID_KEY"
        private const val CHILD_REPLY_COMMENT_ID_KEY = "CHILD_REPLY_COMMENT_ID_KEY"
        fun newInstance(
            cast: CastUIModel,
            parentComment: CommentUIModel,
            replyComment: CommentUIModel? = null
        ): FragmentCommentReplies {
            return FragmentCommentReplies().apply {
                arguments = bundleOf(
                    CAST_ID_KEY to cast.id,
                    CAST_KEY to cast,
                    PARENT_COMMENT_KEY to parentComment,
                    PARENT_COMMENT_ID_KEY to parentComment.id,
                    CHILD_REPLY_COMMENT_ID_KEY to replyComment?.id,
                )
            }
        }
    }

    private val viewModel: CommentsViewModel by viewModels { viewModelFactory }

    private val castId: Int by lazy { requireArguments().getInt(CAST_ID_KEY) }
    private val parentCommentId: Int by lazy { requireArguments().getInt(PARENT_COMMENT_ID_KEY) }
    private var replyToCommentId: Int? = null

    private var _binding: FragmentCommentRepliesBinding? = null
    private val binding get() = _binding!!
    lateinit var itemChildComment: CommentChildItem
    lateinit var itemParentComment: CommentParentItem
    private val adapter = GroupieAdapter()
    private val cast: CastUIModel by lazy { requireArguments().getParcelable(CAST_KEY)!! }
    private var castOwnerId = 0

    override fun reload() {
        commentsViewModel.loadCommentById(parentCommentId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentRepliesBinding.inflate(inflater, container, false)

        replyToCommentId = requireArguments().getInt(CHILD_REPLY_COMMENT_ID_KEY, -1)
            .takeIf { it != -1 }
        cast.owner?.id?.let {
            castOwnerId = it
        }
        getCurrentUser()
        initViews()
        subscribeForComments()
        subscribeCommons()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpPopup(binding.taviVoice.editText, binding.taviVoice)
    }

    private fun getCurrentUser() {
        lifecycleScope.launchWhenCreated {
            JwtChecker.getUserIdFromJwt(false)?.let {
                PrefsHandler.saveCurrentUserId(requireContext(), it)
            }
        }

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
        textAndVoiceInput = binding.taviVoice
        binding.taviVoice.initListenerStatus {
            when (it) {
                is MissingPermissions -> requestRecordPermissions(requireActivity())
                is SendData -> {
                    if (it.filePath != null) {
                        uploadWithAudio(it, castId, parentCommentId, CommentUIModel.OWNER_TYPE_COMMENT)

                    } else if (it.existingComment != null) {
                        commentsViewModel.updateComment(it.existingComment.id, it.text)

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
                castOwnerId,
                parentComment,
                onReplyClick = ::onReplyClick,
                onLikeClick = { comment, liked ->
                    viewModel.likeComment(comment, liked)
                },
                onThreeDotsClick = { comment, item ->
                    handleThreeDotsClick(comment, cast, item, 0)
                },
                onUserMentionClick = { username, userId ->
                    context?.let { context -> UserProfileActivity.show(context, username, userId) }
                },
                onCommentListen = { commentId ->
                    viewModel.listenComment(commentId)
                }
            )
        )
        parentComment.innerComments.forEach { childComment ->
            adapter.add(
                CommentChildItem(
                    castOwnerId,
                    parentComment,
                    childComment,
                    onReplyClick = { _, replyChildComment -> onReplyClick(replyChildComment) },
                    isSimplified = false,
                    onLikeClick = { comment, liked ->
                        viewModel.likeComment(comment, liked)
                    },
                    onThreeDotsClick = { child, item, position ->
                        handleThreeDotsClick(child, cast, item, position)
                    },
                    onUserMentionClick = { username, userId ->
                        context?.let { context ->
                            UserProfileActivity.show(
                                context,
                                username,
                                userId
                            )
                        }
                    },
                    onCommentListen = { commentId ->
                        viewModel.listenComment(commentId)
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
            textAndVoiceInput?.reset()
        }

        actionsViewModel.actionDeleteChildReply.observe(viewLifecycleOwner, { comment ->
            if (::itemChildComment.isInitialized) {
                /*val pos = adapter.getAdapterPosition(itemChildComment)
                adapter.removeGroupAtAdapterPosition(pos)
                adapter.notifyItemRemoved(pos)*/
                adapter.remove(itemChildComment)

                comment?.let {
                    viewModel.deleteComment(comment)
                }
            }
        })

        actionsViewModel.actionComment.observe(viewLifecycleOwner) { commentAction ->
            val ca = commentAction ?: return@observe
            when(ca.type) {
                CommentActionType.Edit -> editComment(ca.comment)
                else -> {

                }
            }
        }

        actionsViewModel.actionDeleteParentReply.observe(viewLifecycleOwner, { comment ->
            if (::itemParentComment.isInitialized) {
                /*val pos = adapter.getAdapterPosition(itemParentComment)
                adapter.removeGroupAtAdapterPosition(pos)
                adapter.notifyItemRemoved(pos)*/
                //adapter.remove(itemParentComment)
                comment?.let {
                    viewModel.deleteComment(comment)
                }
                dismissFragment()
            }
        })
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

    private fun handleThreeDotsClick(
        comment: CommentUIModel,
        cast: CastUIModel,
        item: CommentParentItem,
        index: Int
    ) {
        if (isOwnerOf(comment) || isOwnerOf(cast)) {
            //If current user is owner of the comment or the cast he can delete the comment
            val bundle = bundleOf(
                DialogCommentMoreActions.COMMENT_KEY to comment,
                DialogCommentMoreActions.FROM to "reply",
                DialogCommentMoreActions.ITEM to "parent",
                DialogCommentMoreActions.KEY_CAN_EDIT_COMMENT to commentIsEditable(comment)
            )

            itemParentComment = item

            findNavController().navigate(R.id.dialogCommentMoreActions, bundle)
        }
    }

    private fun handleThreeDotsClick(
        comment: CommentUIModel,
        cast: CastUIModel,
        item: CommentChildItem,
        index: Int
    ) {
        if (isOwnerOf(comment) || isOwnerOf(cast)) {
            //If current user is owner of the comment or the cast he can delete the comment
            val bundle = bundleOf(
                DialogCommentMoreActions.COMMENT_KEY to comment,
                DialogCommentMoreActions.FROM to "reply",
                DialogCommentMoreActions.ITEM to "child",
                DialogCommentMoreActions.KEY_CAN_EDIT_COMMENT to commentIsEditable(comment)
            )
            itemChildComment = item

            findNavController().navigate(R.id.dialogCommentMoreActions, bundle)
        }
    }

    private fun isOwnerOf(cast: CastUIModel): Boolean {
        return cast.owner?.id == PrefsHandler.getCurrentUserId(requireContext())
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}