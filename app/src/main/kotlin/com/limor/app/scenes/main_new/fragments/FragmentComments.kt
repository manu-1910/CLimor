package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentCommentsBinding
import com.limor.app.extensions.loadImage
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main_new.adapters.vh.PodcastCommentItem
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.GroupieAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(owner = this) {
            parentFragmentManager
                .beginTransaction()
                .remove(this@FragmentComments)
                .commit()
            isEnabled = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)

        binding.rvComments.adapter = adapter
        viewModel.loadComments(cast.id)

        cast.imageLinks?.large?.let {
            binding.ivPodcastBackground.loadImage(it)
        }

        subscribeForComments()

        return binding.root
    }

    private fun subscribeForComments() {
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            adapter.update(
                comments.map {
                    PodcastCommentItem(it)
                }
            )
        }
    }
}
