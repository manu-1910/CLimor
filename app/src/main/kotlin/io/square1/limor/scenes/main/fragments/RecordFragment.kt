package io.square1.limor.scenes.main.fragments

import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class RecordFragment : BaseFragment() {

    companion object {
        val TAG: String = RecordFragment::class.java.simpleName
        fun newInstance() = RecordFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //listeners()
        bindViewModel()
        //setVersion()
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
