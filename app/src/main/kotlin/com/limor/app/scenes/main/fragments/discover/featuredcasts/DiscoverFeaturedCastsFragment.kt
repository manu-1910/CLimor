package com.limor.app.scenes.main.fragments.discover.featuredcasts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentDiscoverFeaturedCastsBinding
import com.limor.app.extensions.px
import com.limor.app.scenes.main.fragments.discover.common.casts.GridCastItemDecoration
import com.limor.app.scenes.main.fragments.discover.featuredcasts.list.DiscoverFeaturedCastsAdapter
import com.limor.app.scenes.utils.recycler.TopGridSpacingItemDecoration
import javax.inject.Inject

class DiscoverFeaturedCastsFragment : BaseFragment() {

    private var _binding: FragmentDiscoverFeaturedCastsBinding? = null
    private val binding get() = _binding!!

    private val discoverFeaturedCastsAdapter by lazy { DiscoverFeaturedCastsAdapter() }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: DiscoverFeaturedCastsViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverFeaturedCastsBinding.inflate(inflater)
        initViews()
        subscribeForEvents()
        return binding.root
    }

    private fun initViews() {
        binding.list.apply {
            layoutManager =
                GridLayoutManager(context, discoverFeaturedCastsAdapter.spanCount).apply {
                    spanSizeLookup = discoverFeaturedCastsAdapter.spanSizeLookup
                    adapter = discoverFeaturedCastsAdapter
                    addItemDecoration(GridCastItemDecoration())
                    addItemDecoration(TopGridSpacingItemDecoration(16.px))
                }
        }

        binding.toolbar.btnBack.setOnClickListener {
            it.findNavController().popBackStack()
        }

        binding.toolbar.title.setText(R.string.featured_casts)
    }

    private fun subscribeForEvents() {
        viewModel.featuredCasts.observe(viewLifecycleOwner) {
            discoverFeaturedCastsAdapter.updateFeaturedCasts(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
