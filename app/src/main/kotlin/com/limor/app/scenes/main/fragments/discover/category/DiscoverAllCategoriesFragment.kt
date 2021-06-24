package com.limor.app.scenes.main.fragments.discover.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.chip.Chip
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentDiscoverAllCategoriesBinding
import com.limor.app.extensions.makeGone
import javax.inject.Inject

class DiscoverAllCategoriesFragment : BaseFragment() {

    private var _binding: FragmentDiscoverAllCategoriesBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: DiscoverAllCategoriesViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverAllCategoriesBinding.inflate(inflater, container, false)
        initViews()
        subscribeForEvents()
        return binding.root
    }

    private fun initViews() {
        binding.toolbar.title.setText(R.string.categories)
        binding.toolbar.btnNotification.makeGone()
        binding.toolbar.btnBack.setOnClickListener {
            it.findNavController().popBackStack()
        }
    }

    private fun subscribeForEvents() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.categoriesGroup.removeAllViews()

            categories.map { name ->
                (layoutInflater.inflate(
                    R.layout.item_chip_category,
                    null
                ) as Chip).apply {
                    text = name
                    setOnClickListener {
                        it.findNavController().navigate(
                            R.id.action_discoverAllCategoriesFragment_to_discoverCategoryFragment,
                            bundleOf(DiscoverCategoryFragment.CATEGORY_KEY to name)
                        )
                    }
                }
            }.forEach {
                binding.categoriesGroup.addView(it)
            }
        }
    }
}
