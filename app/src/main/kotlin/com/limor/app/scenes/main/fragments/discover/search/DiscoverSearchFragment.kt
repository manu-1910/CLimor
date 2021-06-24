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

    private val tabs by lazy {
        mapOf(
            Tab.ACCOUNTS to getString(R.string.accounts),
            Tab.CATEGORIES to getString(R.string.categories),
            Tab.HASHTAGS to getString(R.string.title_hashtags),
        )
    }

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
                Toast.makeText(context, "Selected: $tabName", Toast.LENGTH_SHORT).show()
                when(tabs.keys.elementAt(position)) {
                    Tab.ACCOUNTS -> {
                    }
                    Tab.CATEGORIES -> {
                    }
                    Tab.HASHTAGS -> {
                    }
                    else -> {
                        throw IllegalArgumentException()
                    }
                }
            }
        }
    }

    private fun subscribeForEvents() {

    }

    private enum class Tab {
        ACCOUNTS, CATEGORIES, HASHTAGS
    }
}