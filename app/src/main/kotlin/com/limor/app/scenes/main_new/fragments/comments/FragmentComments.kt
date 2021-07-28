package com.limor.app.scenes.main_new.fragments.comments

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
import com.limor.app.databinding.FragmentCommentsBinding
import com.limor.app.extensions.dismissFragment
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main_new.fragments.comments.list.ParentCommentSection
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.SendData
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.GroupieAdapter
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FragmentComments : BaseFragment() {

    companion object {
        val TAG = FragmentComments::class.qualifiedName
        private const val CAST_KEY = "CAST_KEY"
        fun newInstance(cast: CastUIModel): FragmentComments {
            return FragmentComments().apply {
                arguments = bundleOf(CAST_KEY to cast)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: CommentsViewModel by viewModels { viewModelFactory }

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
        viewModel.loadComments(cast.id)
        initViews()
        subscribeForComments()
        return binding.root
    }

    private fun initViews() {
        binding.commentsList.adapter = adapter
        binding.closeBtn.setOnClickListener {
            parentFragment?.dismissFragment()
        }
        binding.taviVoice.initListenerStatus {
            when(it) {
                is SendData -> {

                    if (it.filePath != null) {
                        Commons.getInstance().uploadAudio(
                            context,
                            File(it.filePath),
                            Constants.AUDIO_TYPE_COMMENT,
                            object : Commons.AudioUploadCallback {
                                override fun onSuccess(audioUrl: String?) {
                                    println("Audio upload to AWS succesfully")
                                    viewModel.addComment(
                                        cast.id,
                                        content = it.text,
                                        ownerId = cast.id,
                                        ownerType = CommentUIModel.OWNER_TYPE_PODCAST,
                                        audioURI = audioUrl,
                                        duration = it.duration
                                    )
                                }

                                override fun onProgressChanged(
                                    id: Int,
                                    bytesCurrent: Long,
                                    bytesTotal: Long
                                ) {
                                }

                                override fun onError(error: String?) {
                                    Timber.d("Audio upload to AWS error: $error")
                                }
                            })
                    } else {
                        viewModel.addComment(
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
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
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
                            viewModel.likeComment(comment, liked)
                        }
                    )
                }
            )
        }

        viewModel.commentAddEvent.observe(viewLifecycleOwner) {
            viewModel.loadComments(cast.id)
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
