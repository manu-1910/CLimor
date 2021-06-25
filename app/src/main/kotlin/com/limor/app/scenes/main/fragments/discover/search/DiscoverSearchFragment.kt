package com.limor.app.scenes.main.fragments.discover.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.components.tabselector.TabSelectorView
import com.limor.app.databinding.FragmentDiscoverSearchBinding

class DiscoverSearchFragment : BaseFragment() {

    private var _binding: FragmentDiscoverSearchBinding? = null
    private val binding get() = _binding!!

        //private val viewModel:

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
        binding.tabSelectorView.apply {
            setMode(TabSelectorView.Mode.FIXED)
            setTabs(tabs.values.toList())
            setOnTabSelectedListener { tabName, position ->
                selectedTab = tabs.keys.elementAt(position)
            }
        }
    }

    private fun subscribeForEvents() {

    }

    private enum class Tab {
        ACCOUNTS, CATEGORIES, HASHTAGS
    }
}