package com.limor.app.scenes.main.fragments.discover.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.components.tabselector.TabSelectorView
import com.limor.app.databinding.FragmentDiscoverSearchBinding
import com.limor.app.extensions.makeVisible
import com.limor.app.extensions.px
import com.limor.app.scenes.main.fragments.discover.category.DiscoverCategoryFragment
import com.limor.app.scenes.main.fragments.discover.search.DiscoverSearchViewModel.SearchResult
import com.limor.app.scenes.main.fragments.discover.search.list.DiscoverSearchAdapter
import com.limor.app.scenes.utils.recycler.VerticalSpacingItemDecoration
import javax.inject.Inject

class DiscoverSearchFragment : BaseFragment() {

    private var _binding: FragmentDiscoverSearchBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: DiscoverSearchViewModel by viewModels { viewModelFactory }

    private val resultAdapter = DiscoverSearchAdapter(
        onFollowUserClick = { account, follow ->
            viewModel.followUser(account, follow)
        },
        onCategoryClick = { category ->
            findNavController().navigate(
                R.id.action_discoverSearchFragment_to_discoverCategoryFragment,
                bundleOf(DiscoverCategoryFragment.CATEGORY_KEY to category)
            )
        }
    )

    private val tabs by lazy {
        mapOf(
            Tab.ACCOUNTS to getString(R.string.accounts),
            Tab.CATEGORIES to getString(R.string.categories),
            Tab.HASHTAGS to getString(R.string.title_hashtags),
        )
    }

    private var selectedTab: Tab = Tab.ACCOUNTS

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverSearchBinding.inflate(inflater)
        initViews()
        subscribeForEvents()
        return binding.root
    }

    private fun initViews() {
        binding.resultList.adapter = resultAdapter
        binding.resultList.addItemDecoration(VerticalSpacingItemDecoration(16.px))
        binding.tabSelectorView.apply {
            setOnTabSelectedListener { tabName, position ->
                selectedTab = tabs.keys.elementAt(position)
                resultAdapter.clear()
                performSearch(binding.searchItem.searchBar.getCurrentSearchQuery())
            }
            setMode(TabSelectorView.Mode.FIXED)
            setTabs(tabs.values.toList())
        }

        binding.searchItem.searchBar.apply {
            setOnQueryTextListener(
                onQueryTextChange = {
                    performSearch(it)
                },
                onQueryTextSubmit = {
                    performSearch(it)
                },
                onQueryTextBlank = {
                    resultAdapter.clear()
                }
            )
            // Automatically open keyboard
            requestFocusOnText()
        }

        binding.searchItem.btnBack.apply {
            makeVisible()
            setOnClickListener {
                it.findNavController().popBackStack()
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            resultAdapter.clear()
        } else if(query.length>2){
            viewModel.search(query, selectedTab)
        }
    }

    private fun subscribeForEvents() {
        viewModel.searchResult.observe(viewLifecycleOwner) { result ->
            if (isResultForTheCurrentTab(result)) {
                resultAdapter.updateSearchResult(result)
            }
        }
    }

    private fun isResultForTheCurrentTab(result: SearchResult): Boolean {
        return when (result) {
            is SearchResult.Accounts -> selectedTab == Tab.ACCOUNTS
            is SearchResult.Categories -> selectedTab == Tab.CATEGORIES
            is SearchResult.Hashtags -> selectedTab == Tab.HASHTAGS
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class Tab {
        ACCOUNTS, CATEGORIES, HASHTAGS
    }
}