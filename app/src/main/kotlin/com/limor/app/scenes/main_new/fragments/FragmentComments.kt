package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.GetCommentsByPodcastsQuery
import com.limor.app.databinding.FragmentCommentsBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.main_new.adapters.PodcastCommentsAdapter
import com.limor.app.scenes.main_new.view_model.PodcastFullPlayerViewModel
import javax.inject.Inject

class FragmentComments : FragmentWithLoading(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PodcastFullPlayerViewModel by viewModels {viewModelFactory }

    private lateinit var binding: FragmentCommentsBinding
    private var podcastId: Int = 0
    override fun load(){
        model.loadComments(podcastId)
    }

    override val errorLiveData: LiveData<String>
        get() = model.commentsErrorData

    override fun onCreate(savedInstanceState: Bundle?) {
        getArgs()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getArgs() {
        arguments?.let {
            podcastId = it.getInt(PODCAST_ID_EXTRA, 0)
        }
    }


    override fun subscribeToViewModel() {
        super.subscribeToViewModel()
        model.commentsLiveData.observe(this) {
            it?.let {
                switchCommonVisibility(isLoading = false)
                createCommentAdapter(it)
            }
        }
    }

    private fun createCommentAdapter(list: List<GetCommentsByPodcastsQuery.GetCommentsByPodcast>) {
        val adapter = binding.rvComments.adapter
        if (adapter == null) {
            val layoutManager = LinearLayoutManager(requireContext())
            binding.rvComments.layoutManager = layoutManager
            binding.rvComments.adapter =
                PodcastCommentsAdapter().apply { submitList(list) }
        } else
            (binding.rvComments.adapter as PodcastCommentsAdapter).submitList(list)
    }

    companion object {
        const val PODCAST_ID_EXTRA = "podcast_id_extra"
    }
}