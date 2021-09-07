package com.limor.app.scenes.main.fragments.discover.hashtag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentDiscoverHashtagBinding
import com.limor.app.scenes.main.fragments.discover.common.casts.GridCastItemDecoration
import com.limor.app.scenes.main.fragments.discover.hashtag.list.DiscoverHashtagAdapter
import com.limor.app.uimodels.TagUIModel
import javax.inject.Inject

class DiscoverHashtagFragment: BaseFragment() {
    companion object {
        const val HASHTAG_KEY = "HASHTAG_KEY"
        const val KEY_SHOW_NOTIFICATION_ICON = "KEY_SHOW_NOTIFICATION_ICON"
    }

    private var _binding: FragmentDiscoverHashtagBinding? = null
    private val binding get() = _binding!!

    private val hashtag: TagUIModel by lazy { requireArguments().getParcelable(HASHTAG_KEY)!! }
    private val showNotificationIcon: Boolean by lazy {
        requireArguments().getBoolean(KEY_SHOW_NOTIFICATION_ICON, true)
    }
    private val discoverHashtagAdapter by lazy {
        DiscoverHashtagAdapter(
            requireContext(),
            findNavController()
        )
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: DiscoverHashtagViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverHashtagBinding.inflate(inflater)

        viewModel.loadCasts(hashtag.id)

        initViews()
        subscribeForEvents()
        return binding.root
    }

    private fun initViews() {
        binding.list.apply {
            layoutManager = GridLayoutManager(context, discoverHashtagAdapter.spanCount).apply {
                spanSizeLookup = discoverHashtagAdapter.spanSizeLookup
                adapter = discoverHashtagAdapter
                addItemDecoration(GridCastItemDecoration())
            }
        }

        binding.toolbar.btnNotification.visibility = if (showNotificationIcon) View.VISIBLE else View.GONE
        val tagText = "#${hashtag.tag}"
        binding.toolbar.title.text = tagText
        binding.toolbar.btnBack.setOnClickListener {
            it.findNavController().popBackStack()
        }

        discoverHashtagAdapter.updatePostsCount(hashtag)
    }

    private fun subscribeForEvents() {
        viewModel.topCasts.observe(viewLifecycleOwner) {
            discoverHashtagAdapter.updateTopCasts(it)
        }
        viewModel.recentCasts.observe(viewLifecycleOwner) {
            discoverHashtagAdapter.updateRecentCasts(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
