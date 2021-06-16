package com.limor.app.scenes.main.fragments.discover2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentDiscover2Binding
import com.limor.app.scenes.main.fragments.discover2.list.DiscoverAdapter

const val BUNDLE_KEY_SEARCH_TEXT = "BUNDLE_KEY_SEARCH_TEXT"

class DiscoverFragment2 : BaseFragment() {

    private var _binding: FragmentDiscover2Binding? = null
    private val binding get() = _binding!!

    private val adapter = DiscoverAdapter(

    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscover2Binding.inflate(inflater, container, false)
        initViews()
        subscribeForEvents()
        return binding.root
    }

    private fun initViews() {
        binding.discoveryList.adapter = adapter
    }

    private fun subscribeForEvents() {
        TODO("Not yet implemented")
    }

    private fun onSearchViewTextChange(text: String) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
