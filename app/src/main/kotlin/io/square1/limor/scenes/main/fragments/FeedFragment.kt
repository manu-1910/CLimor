package io.square1.limor.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.main.viewmodels.FeedViewModel

import javax.inject.Inject

class FeedFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelFeed: FeedViewModel


    @Inject
    lateinit var sessionManager: SessionManager

    private val getFeedDataTrigger = PublishSubject.create<Unit>()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = context?.applicationContext as App

        bindViewModel()
        apiCallGetFeed()

        getFeedDataTrigger.onNext(Unit)
    }

    private fun apiCallGetFeed() {
        val output = viewModelFeed.transform(
            FeedViewModel.Input(
                getFeedDataTrigger
            )
        )

        output.response.observe(this, Observer {
            System.out.println("Hemos obtenido datos del feeddddddd")
            System.out.println("Hemos obtenido ${it.data.feed_items.size} items")
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    private fun bindViewModel() {
        activity?.let{ fragmentActivity ->
            viewModelFeed = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(FeedViewModel::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

}