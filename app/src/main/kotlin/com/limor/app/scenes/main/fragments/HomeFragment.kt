package com.limor.app.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limor.app.R
import com.limor.app.common.BaseFragment


class HomeFragment : BaseFragment() {


    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
        fun newInstance() = HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //listeners()
        bindViewModel()
        //setVersion()
        checkIfFollowingSomeone()
    }

    private fun checkIfFollowingSomeone() {

    }

    private fun bindViewModel() {
        /* activity?.let { fragmentActivity ->
             mainViewModel = ViewModelProviders
                 .of(fragmentActivity, viewModelFactory)
                 .get(MainViewModel::class.java)
         }*/
    }

    /*
    mainViewModel.isFromFaqFlag = true
    val bundle = Bundle()
    bundle.putString(getString(R.string.moreWebViewKey), getString(R.string.faq_url))
    findNavController().navigate(R.id.action_navigation_more_to_navigation_more_web_view, bundle)
    */


}
