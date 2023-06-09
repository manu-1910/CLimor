package com.limor.app.scenes.patron.manage.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.databinding.DialogErrorPublishCastBinding
import com.limor.app.scenes.patron.manage.viewmodels.ManagePatronViewModel
import javax.inject.Inject

class  DialogCancelPatronMembership: DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: ManagePatronViewModel by activityViewModels{ viewModelFactory }
    private lateinit var binding : DialogErrorPublishCastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogErrorPublishCastBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClicks()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    private fun initViews(){
        binding.textTitle.text = resources.getString(R.string.cancel_patron_membership)
        binding.textDescription.text = resources.getString(R.string.cancel_patron_membership_description)
        binding.okButton.text = resources.getString(R.string.continue_button)
        binding.okButton.background = null
        binding.cancelButton.text = resources.getString(R.string.cancel)
        binding.cancelButton.visibility = View.VISIBLE
        binding.cancelButton.background = resources.getDrawable(R.drawable.bg_round_yellow_ripple_new)
    }

    private fun setOnClicks() {
        binding.cancelButton.setOnClickListener {
            this.dismiss()
        }
        binding.okButton.setOnClickListener {
            findNavController().navigate(R.id.dialog_cancel_patron_membership_to_cancel_patron_membership_success_fragment)
        }
    }

}