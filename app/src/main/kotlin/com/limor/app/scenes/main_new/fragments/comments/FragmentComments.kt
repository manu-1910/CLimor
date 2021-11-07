package com.limor.app.scenes.main_new.fragments.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.databinding.FragmentCommentsBinding
import com.limor.app.extensions.dismissFragment
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.viewmodels.CommentActionType
import com.limor.app.scenes.main.viewmodels.HandleCommentActionsViewModel
import com.limor.app.scenes.main_new.fragments.comments.list.ParentCommentSection
import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentChildItem
import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentParentItem
import com.limor.app.scenes.profile.DialogCommentMoreActions
import com.limor.app.scenes.utils.MissingPermissions
import com.limor.app.scenes.utils.SendData
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.util.requestRecordPermissions
import com.xwray.groupie.GroupieAdapter
import timber.log.Timber

class FragmentComments : UserMentionFragment() {

    companion object {
        val TAG = FragmentComments::class.qualifiedName
        private const val CAST_KEY = "CAST_KEY"
        fun newInstance(cast: CastUIModel): FragmentComments {
            return FragmentComments().apply {
                arguments = bundleOf(CAST_KEY to cast)
            }
        }
    }

    private val cast: CastUIModel by lazy { requireArguments().getParcelable(CAST_KEY)!! }
    lateinit var itemChildComment: CommentChildItem
    lateinit var itemParentComment: CommentParentItem
    var section: ParentCommentSection? = null
    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!
    private val adapter = GroupieAdapter()
    private var castOwnerId = 0

    override fun reload() {
        commentsViewModel.loadComments(cast.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        reload()
        cast.owner?.id?.let {
            castOwnerId = it
        }
        getCurrentUser()
        initViews()
        subscribeForComments()
        subscribeCommons()
        return binding.root
    }

    private fun getCurrentUser() {
        lifecycleScope.launchWhenCreated {
            JwtChecker.getUserIdFromJwt(false)?.let {
                PrefsHandler.saveCurrentUserId(requireContext(), it)
            }
        }

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

        textAndVoiceInput = binding.taviVoice
        binding.taviVoice.initListenerStatus {
            when (it) {
                is MissingPermissions -> requestRecordPermissions(requireActivity())
                is SendData -> {

                    if (it.filePath != null) {
                        uploadWithAudio(it, cast.id, cast.id, CommentUIModel.OWNER_TYPE_PODCAST)

                    } else if (it.existingComment != null) {
                        commentsViewModel.updateComment(it.existingComment.id, it.text)

                    } else {
                        commentsViewModel.addComment(
                            cast.id,
                            it.text,
                            ownerId = cast.id,
                            ownerType = CommentUIModel.OWNER_TYPE_PODCAST
                        )
                    }
                }
                else -> {

                }
            }
        }
    }

    private fun setInfoControls(hasComments: Boolean) {
        binding.apply {
            progressBar.visibility = View.GONE
            noCommentsPlaceholder.visibility = if (hasComments) View.GONE else View.VISIBLE
        }
    }

    private fun subscribeForComments() {
        commentsViewModel.comments.observe(viewLifecycleOwner) { comments ->
            setInfoControls(comments.isNotEmpty())

            adapter.update(
                comments.map { it ->
                    ParentCommentSection(
                        castOwnerId,
                        comment = it,
                        onReplyClick = { parentComment, replyToComment ->
                            goToReplies(parentComment, replyToComment)
                        },
                        onViewMoreCommentsClick = { comment ->
                            goToReplies(comment)
                        },
                        onLikeClick = { comment, liked ->
                            commentsViewModel.likeComment(comment, liked)
                        },
                        onThreeDotsClick = { comment, item, section ->
                            handleThreeDotsClick(comment, cast, item, section)
                        },
                        onChildThreeDotsClick = { comment, item, section ->
                            handleThreeDotsClick(comment, cast, item, section)
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
                            commentsViewModel.listenComment(commentId)
                        }
                    )
                }
            )
        }

        commentsViewModel.commentAddEvent.observe(viewLifecycleOwner) {
            if (it == -1) {
                reportError(getString(R.string.could_not_save_comment))
            } else {
                commentsViewModel.loadComments(cast.id)
            }
            textAndVoiceInput?.reset()
        }

        actionsViewModel.actionComment.observe(viewLifecycleOwner) { commentAction ->
            val ca = commentAction ?: return@observe
            when (ca.type) {
                CommentActionType.Edit -> editComment(ca.comment)
                else -> {

                }
            }
        }

        actionsViewModel.actionDelete.observe(viewLifecycleOwner, { comment ->
            Timber.d("Remove parent comment $section")
            if (::itemParentComment.isInitialized) {
                section?.remove(itemParentComment)
                comment?.let {
                    commentsViewModel.deleteComment(comment)
                }
            }
        })
        actionsViewModel.actionDeleteChild.observe(viewLifecycleOwner, { comment ->
            if (::itemChildComment.isInitialized) {
                section?.remove(itemChildComment)
                comment?.let {
                    commentsViewModel.deleteComment(comment)
                }
            }

        })
    }

    private fun handleThreeDotsClick(
        comment: CommentUIModel,
        cast: CastUIModel,
        item: CommentParentItem,
        section: ParentCommentSection
    ) {
        Timber.d("${isOwnerOf(comment)} comment owner and ${isOwnerOf(cast)} cast owner ")
        if (isOwnerOf(comment) || isOwnerOf(cast)) {
            //If current user is owner of the comment or the cast he can delete the comment

            itemParentComment = item
            this.section = section

            val bundle = bundleOf(
                DialogCommentMoreActions.COMMENT_KEY to comment,
                DialogCommentMoreActions.FROM to "comment",
                DialogCommentMoreActions.ITEM to "parent",
                DialogCommentMoreActions.KEY_CAN_EDIT_COMMENT to commentIsEditable(comment)
            )
            findNavController().navigate(R.id.dialogCommentMoreActions, bundle)
        }
    }

    private fun handleThreeDotsClick(
        comment: CommentUIModel,
        cast: CastUIModel,
        item: CommentChildItem,
        section: ParentCommentSection
    ) {
        Timber.d("${isOwnerOf(comment)} comment owner and ${isOwnerOf(cast)} cast owner ")
        if (isOwnerOf(comment) || isOwnerOf(cast)) {
            //If current user is owner of the comment or the cast he can delete the comment
            itemChildComment = item
            this.section = section

            val bundle = bundleOf(
                DialogCommentMoreActions.COMMENT_KEY to comment,
                DialogCommentMoreActions.FROM to "comment",
                DialogCommentMoreActions.ITEM to "child",
                DialogCommentMoreActions.KEY_CAN_EDIT_COMMENT to commentIsEditable(comment)
            )
            findNavController().navigate(R.id.dialogCommentMoreActions, bundle)
        }
    }

    private fun isOwnerOf(cast: CastUIModel): Boolean {
        return cast.owner?.id == PrefsHandler.getCurrentUserId(requireContext())
    }

    private fun goToReplies(
        parentComment: CommentUIModel,
        replyToComment: CommentUIModel? = null
    ) {
        FragmentCommentReplies.newInstance(cast, parentComment, replyToComment)
            .also {
                parentFragmentManager.beginTransaction()
                    .add(R.id.comment_container, it)
                    .commit()
            }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
