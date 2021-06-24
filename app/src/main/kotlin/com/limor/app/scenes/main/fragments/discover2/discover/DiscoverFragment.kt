package com.limor.app.scenes.main.fragments.discover2.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentDiscoverBinding
import com.limor.app.scenes.main.fragments.discover2.common.casts.GridCastItemDecoration
import com.limor.app.scenes.main.fragments.discover2.discover.list.DiscoverAdapter

class DiscoverFragment : BaseFragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private val discoverAdapter by lazy { DiscoverAdapter(requireContext()) }
    private val viewModel: DiscoverViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        initViews()
        subscribeForEvents()
        return binding.root
    }

    private fun initViews() {
        binding.discoveryList.apply {
            layoutManager = GridLayoutManager(context, discoverAdapter.spanCount).apply {
                spanSizeLookup = discoverAdapter.spanSizeLookup
                adapter = discoverAdapter
                addItemDecoration(GridCastItemDecoration())
            }
        }
    }

    private fun subscribeForEvents() {
        viewModel.categories.observe(viewLifecycleOwner) {
            discoverAdapter.updateCategories(it)
        }
        viewModel.suggestedPeople.observe(viewLifecycleOwner) {
            discoverAdapter.updateSuggestedPeople(it)
        }
        viewModel.featuredCasts.observe(viewLifecycleOwner) {
            discoverAdapter.updateFeaturedCasts(it)
        }
        viewModel.topCasts.observe(viewLifecycleOwner) {
            discoverAdapter.updateTopCasts(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
