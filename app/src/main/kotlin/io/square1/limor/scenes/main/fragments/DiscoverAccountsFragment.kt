package io.square1.limor.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.viewmodels.DiscoverAccountsViewModel
import io.square1.limor.scenes.main.viewmodels.DiscoverViewModel
import javax.inject.Inject

class DiscoverAccountsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelDiscoverAccounts: DiscoverAccountsViewModel

    companion object {
        val TAG: String = DiscoverAccountsFragment::class.java.simpleName
        fun newInstance() = DiscoverAccountsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover_accounts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViewModel()
    }

    fun setSearchText(text: String){

    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelDiscoverAccounts = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DiscoverAccountsViewModel::class.java)
        }
    }

}