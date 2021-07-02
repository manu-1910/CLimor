package com.limor.app.scenes.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.limor.app.R
import com.limor.app.databinding.DialogOtherUserActionsBinding
import com.limor.app.databinding.DialogReportCastBinding
import com.limor.app.databinding.DialogUserActionsBinding
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.main_new.utils.ArgsConverter
import com.limor.app.scenes.main_new.utils.ReportDialogArgs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class DialogUserProfileActions : DialogFragment() {

    lateinit var binding: DialogOtherUserActionsBinding
    lateinit var args: ReportDialogArgs
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogOtherUserActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        getArgs()
        setOnClicks()
    }

    private fun getArgs() {
        arguments?.let {
            args = ArgsConverter.decodeFeedItemBundleAsReportDialogArgs(requireArguments())
            Timber.d(args.toString())
        }
    }

    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { this.dismiss() }
        binding.btnReportCast.setOnClickListener {
            this.dismiss()
        }
    }
}