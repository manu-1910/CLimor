package com.limor.app.scenes.main_new.fragments.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentCommentRepliesBinding
import com.limor.app.extensions.dismissFragment
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentChildItem
import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentParentItem
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.GroupieAdapter
import javax.inject.Inject

class FragmentCommentReplies : BaseFragment() {

    companion object {
        val TAG = FragmentCommentReplies::class.qualifiedName
        private const val PARENT_COMMENT_ID_KEY = "PARENT_COMMENT_ID_KEY"
        private const val CHILD_REPLY_COMMENT_ID_KEY = "CHILD_REPLY_COMMENT_ID_KEY"
        fun newInstance(
            parentCommentId: Int,
            replyToCommentId: Int? = null
        ): FragmentCommentReplies {
            return FragmentCommentReplies().apply {
                arguments = bundleOf(
                    PARENT_COMMENT_ID_KEY to parentCommentId,
                    CHILD_REPLY_COMMENT_ID_KEY to replyToCommentId,
                )
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: CommentsViewModel by viewModels { viewModelFactory }

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

    private fun initViews() {
        binding.commentsList.adapter = adapter
        binding.closeBtn.setOnClickListener {
            parentFragment?.dismissFragment()
        }
        binding.backBtn.setOnClickListener {
            dismissFragment()
        }
        viewModel.loadCommentById(parentCommentId)
    }

    private fun fillList(parentComment: CommentUIModel) {
        adapter.clear()
        adapter.add(
            CommentParentItem(
                parentComment,
                onReplyClick = ::onReplyClick,
                onLikeClick = { comment, liked ->
                    viewModel.likeComment(comment, liked)
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
                    }
                )
            )
        }
    }

    private fun subscribeForComments() {
        viewModel.comment.observe(viewLifecycleOwner) { updatedComment ->
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
    }

    private fun onReplyClick(comment: CommentUIModel) {
        // TODO @Maksym focus on editText
        ToastMaker.showToast(requireContext(), "Replying to ${comment.user?.getFullName()}")
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
