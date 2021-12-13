package com.limor.app.scenes.patron.manage.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import com.limor.app.scenes.utils.CommonsKt
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

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.toolbar.title.text = resources.getString(R.string.manage_limor_patron_title)
        binding.toolbar.btnNotification.visibility = View.GONE
    }

    private fun setListeners() {
        binding.toolbar.btnBack.setOnClickListener {
            requireActivity().finish()
        }
        binding.invitationsLayout.setOnClickListener {
            if(CommonsKt.user?.availableInvitations?:0 > 0){
                findNavController().navigate(R.id.action_manage_patron_fragment_to_fragment_invite_users)
            }
        }
        binding.changeCastPricesLayout.setOnClickListener {
            val intent = Intent(requireActivity(), ChangePriceActivity::class.java)
            intent.putExtra(ChangePriceActivity.CHANGE_PRICE_FOR_ALL_CASTS, true)
            intent.putExtra(ChangePriceActivity.CAST_ID, -1)
            startActivity(intent)
        }
        binding.catagoriesLayout.setOnClickListener {
            findNavController().navigate(
                R.id.action_manage_patron_fragment_to_fragment_patron_categories
            )
        }
        binding.earningsLayout.setOnClickListener {
            findNavController().navigate(R.id.action_manage_patron_fragment_to_fragment_my_earnings)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.invitationsCountTextView.text  = "${CommonsKt.user?.availableInvitations?:0} Left"
    }
}