package com.limor.app.scenes.patron.manage.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentManagePatronBinding
import com.limor.app.scenes.patron.manage.viewmodels.ManagePatronViewModel
import javax.inject.Inject

class ManagePatronFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: ManagePatronViewModel by activityViewModels { viewModelFactory }

    private lateinit var binding: FragmentManagePatronBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManagePatronBinding.inflate(inflater, container, false)
        initViews()
        setListeners()
        return binding.root
    }

    private fun initViews() {
        binding.toolbar.title.text = resources.getString(R.string.manage_limor_patron_title)
        binding.toolbar.btnNotification.visibility = View.GONE
    }

    private fun setListeners() {
        binding.toolbar.btnBack.setOnClickListener {
            requireActivity().finish()
        }
        binding.changeCastPricesLayout.setOnClickListener {
            findNavController().navigate(R.id.action_manage_patron_fragment_to_dialog_change_price)
        }
        binding.patronMembershipLayout.setOnClickListener {
            findNavController().navigate(R.id.action_manage_patron_fragment_to_dialog_cancel_patron_membership)
        }
    }

}