package com.limor.app.scenes.main_new.fragments.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.limor.app.R
import com.limor.app.databinding.FragmentCommentsBinding
import com.limor.app.di.Injectable
import com.limor.app.extensions.dismissFragment
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main_new.fragments.comments.list.ParentCommentSection
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.GroupieAdapter
import javax.inject.Inject

class FragmentComments : BottomSheetDialogFragment(), Injectable {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
        requireActivity().onBackPressedDispatcher.addCallback(owner = this) {
            dismissFragment()
            isEnabled = false
        }
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomSheetBehavior.from(
            dialog!!.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        ).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initViews() {
        binding.commentsList.adapter = adapter
        binding.closeBtn.setOnClickListener {
            dismissFragment()
        }
    }

    private fun subscribeForComments() {
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            val _comments = comments + comments + comments + comments + comments + comments + comments + comments + comments + comments + comments + comments
            adapter.update(
                _comments.map {
                    ParentCommentSection(
                        comment = it,
                        onReplyClick = { parentComment, childComment ->

                        },
                        onViewMoreCommentsClick = { comment ->

                        }
                    )
                }
            )
        }
    }
}
