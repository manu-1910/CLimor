package com.limor.app.scenes.main_new.fragments.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentCommentsBinding
import com.limor.app.extensions.dismissFragment
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main_new.fragments.comments.list.ParentCommentSection
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.MissingPermissions
import com.limor.app.scenes.utils.SendData
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.util.requestRecordPermissions
import com.xwray.groupie.GroupieAdapter
import timber.log.Timber
import java.io.File

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

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!
    private val adapter = GroupieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        commentsViewModel.loadComments(cast.id)
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

        binding.taviVoice.initListenerStatus {
            when(it) {
                is MissingPermissions -> requestRecordPermissions(requireActivity())
                is SendData -> {

                    if (it.filePath != null) {
                        uploadVoiceComment(it.filePath) { audioUrl ->
                            commentsViewModel.addComment(
                                cast.id,
                                content = it.text,
                                ownerId = cast.id,
                                ownerType = CommentUIModel.OWNER_TYPE_PODCAST,
                                audioURI = audioUrl,
                                duration = it.duration
                            )
                        }

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

    private fun subscribeForComments() {
        commentsViewModel.comments.observe(viewLifecycleOwner) { comments ->
            adapter.update(
                comments.map {
                    ParentCommentSection(
                        comment = it,
                        onReplyClick = { parentComment, replyToComment ->
                            goToReplies(parentComment, replyToComment)
                        },
                        onViewMoreCommentsClick = { comment ->
                            goToReplies(comment)
                        },
                        onLikeClick = { comment, liked ->
                            commentsViewModel.likeComment(comment, liked)
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
        }
    }

    private fun goToReplies(
        parentComment: CommentUIModel,
        replyToComment: CommentUIModel? = null
    ) {
        FragmentCommentReplies.newInstance(cast.id, parentComment.id, replyToComment?.id)
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
