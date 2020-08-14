package io.square1.limor.scenes.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.common.ViewModelFactory
import io.square1.limor.scenes.main.viewmodels.FeedViewModel
import io.square1.limor.uimodels.UIFeed
import javax.inject.Inject

class FeedFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: FeedViewModel


    @Inject
    lateinit var sessionManager: SessionManager

    private var feedList: ArrayList<UIFeed> = ArrayList()
    var app: App? = null

    var rootView: View? = null


    companion object {
        val TAG: String = FeedFragment::class.java.simpleName
        fun newInstance() = FeedFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_feed, container, false)
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }


    private fun getFeed() {
        activity?.let {

        }
    }

}