package com.limor.app.scenes.main_new.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.limor.app.databinding.DialogReportCastP2Binding
import com.limor.app.scenes.main_new.utils.ArgsConverter
import com.limor.app.scenes.main_new.utils.ReportDialogArgs
import timber.log.Timber

class DialogPodcastMoreP2 : DialogFragment() {

    lateinit var binding : DialogReportCastP2Binding
    lateinit var args: ReportDialogArgs

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogReportCastP2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setOnClicks()
        getArgs()
    }

    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { this.dismiss() }
    }

    private fun getArgs() {
        arguments?.let {
            args = ArgsConverter.decodeFeedItemBundleAsReportDialogArgs(requireArguments())
            Timber.d(args.toString())
        }
    }
}