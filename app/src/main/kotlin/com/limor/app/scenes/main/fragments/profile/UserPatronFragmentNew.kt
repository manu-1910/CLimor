package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.limor.app.R
import com.limor.app.databinding.FragmnetUserPatronNewBinding
import kotlinx.android.synthetic.main.fragment_waveform.view.*

class UserPatronFragmentNew(): Fragment() {

    lateinit var binding:FragmnetUserPatronNewBinding
    var requested = false
    companion object {
        fun newInstance(newUserId: Int) = UserPatronFragmentNew()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmnetUserPatronNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClicks()

    }

    private fun setOnClicks() {

        binding.inviteButton.setOnClickListener {
            if(!requested){
                binding.inviteButton.isEnabled = false
                binding.inviteButton.text = getString(R.string.requested)
                binding.patronStatusTv.text = getString(R.string.limor_patron_requested)
                binding.prProgress.progress = 0
            }
        }
    }


}