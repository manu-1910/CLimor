package io.square1.limor.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.square1.limor.R
import io.square1.limor.common.BaseFragment

class DiscoverHashTagsFragment : BaseFragment() {

    companion object {
        val TAG: String = DiscoverHashTagsFragment::class.java.simpleName
        fun newInstance() = DiscoverHashTagsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover_hashtags, container, false)
    }

    fun setSearchText(text: String){

    }

}