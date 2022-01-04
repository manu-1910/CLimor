package com.limor.app.scenes.patron.manage.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.databinding.ViewCastPublishedBinding

class CancelPatronMembershipFragment : Fragment() {

    private lateinit var binding : ViewCastPublishedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ViewCastPublishedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListeners()
    }

    private fun initView(){
        binding.titleTV.text = resources.getString(R.string.cancel_patron_membership_success_title)
        binding.descriptionTV.text = resources.getString(R.string.cancel_patron_membership_success_description)
        binding.btnDone.text = resources.getString(R.string.continue_button)
    }

    private fun setListeners(){
        binding.btnDone.setOnClickListener {
            findNavController().navigateUp()
        }
    }

}