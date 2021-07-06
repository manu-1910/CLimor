package com.limor.app.scenes.main_new.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.limor.app.databinding.DialogReportCastP2Binding

class DialogPodcastReportP2 : DialogFragment() {

    companion object {
        val TAG = DialogPodcastReportP2::class.qualifiedName
        private const val CAST_ID_KEY = "CAST_ID_KEY"
        fun newInstance(castId: Int): DialogPodcastReportP2 {
            return DialogPodcastReportP2().apply {
                arguments = bundleOf(CAST_ID_KEY to castId)
            }
        }
    }
    private var binding : DialogReportCastP2Binding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogReportCastP2Binding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setOnClicks()
    }

    private fun setOnClicks() {
        binding!!.btnCancel.setOnClickListener { this.dismiss() }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}