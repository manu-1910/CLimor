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
        private const val PARENT_COMMENT_KEY = "PARENT_COMMENT_KEY"
        private const val CHILD_REPLY_COMMENT_KEY = "CHILD_REPLY_COMMENT_KEY"
        fun newInstance(
            parentComment: CommentUIModel,
            replyToComment: CommentUIModel? = null
        ): FragmentCommentReplies {
            return FragmentCommentReplies().apply {
                arguments = bundleOf(
                    PARENT_COMMENT_KEY to parentComment,
                    CHILD_REPLY_COMMENT_KEY to replyToComment,
                )
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: CommentsViewModel by viewModels { viewModelFactory }

    private val parentComment: CommentUIModel by lazy {
        requireArguments().getParcelable(
            PARENT_COMMENT_KEY
        )!!
    }
    private val replyToComment: CommentUIModel? by lazy {
        requireArguments().getParcelable(
            CHILD_REPLY_COMMENT_KEY
        )
    }

    private var _binding: FragmentCommentRepliesBinding? = null
    private val binding get() = _binding!!

    private val adapter = GroupieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentRepliesBinding.inflate(inflater, container, false)
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
        fillList(parentComment)

        if (replyToComment != null) {
            onReplyClick(replyToComment!!)
        }
    }

    private fun fillList(parentComment: CommentUIModel) {
        adapter.clear()
        adapter.add(
            CommentParentItem(parentComment, onReplyClick = ::onReplyClick)
        )
        parentComment.innerComments.forEach { childComment ->
            adapter.add(
                CommentChildItem(
                    parentComment,
                    childComment,
                    onReplyClick = { _, replyChildComment -> onReplyClick(replyChildComment) },
                    isSimplified = false,
                )
            )
        }
    }

    private fun subscribeForComments() {
        viewModel.comment.observe(viewLifecycleOwner) { updatedComment ->
            if (updatedComment != null) {
                fillList(parentComment)
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
