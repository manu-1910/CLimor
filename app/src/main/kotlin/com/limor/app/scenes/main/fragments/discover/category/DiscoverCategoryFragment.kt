package com.limor.app.scenes.main.fragments.discover.category

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
import com.limor.app.databinding.FragmentDiscoverCategoryBinding
import com.limor.app.scenes.main.fragments.discover.category.list.DiscoverCategoryAdapter
import com.limor.app.scenes.main.fragments.discover.common.casts.GridCastItemDecoration
import com.limor.app.uimodels.CategoryUIModel
import javax.inject.Inject

class DiscoverCategoryFragment : BaseFragment() {

    companion object {
        const val CATEGORY_KEY = "CATEGORY_KEY"
    }

    private var _binding: FragmentDiscoverCategoryBinding? = null
    private val binding get() = _binding!!

    private val category: CategoryUIModel by lazy { requireArguments().getParcelable(CATEGORY_KEY)!! }
    private val discoverCategoryAdapter by lazy {
        DiscoverCategoryAdapter(
            requireContext(),
            findNavController()
        )
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: DiscoverCategoryViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverCategoryBinding.inflate(inflater)

        viewModel.loadCasts(category.id)

        initViews()
        subscribeForEvents()
        return binding.root
    }

    private fun initViews() {
        binding.list.apply {
            layoutManager = GridLayoutManager(context, discoverCategoryAdapter.spanCount).apply {
                spanSizeLookup = discoverCategoryAdapter.spanSizeLookup
                adapter = discoverCategoryAdapter
                addItemDecoration(GridCastItemDecoration())
            }
        }

        binding.toolbar.title.text = category.name
        binding.toolbar.btnBack.setOnClickListener {
            it.findNavController().popBackStack()
        }
    }

    private fun subscribeForEvents() {
        viewModel.featuredCasts.observe(viewLifecycleOwner) {
            discoverCategoryAdapter.updateFeaturedCasts(it)
        }
        viewModel.topCasts.observe(viewLifecycleOwner) {
            discoverCategoryAdapter.updateTopCasts(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
