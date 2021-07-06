package com.limor.app.scenes.main_new.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.limor.app.databinding.DialogPodcastMoreActionsBinding

class DialogPodcastMoreActions : DialogFragment() {

    companion object {
        val TAG = DialogPodcastMoreActions::class.qualifiedName
        const val CAST_ID_KEY = "CAST_ID_KEY"

        fun newInstance(castId: Int): DialogPodcastMoreActions {
            return DialogPodcastMoreActions().apply {
                arguments = bundleOf(CAST_ID_KEY to castId)
            }
        }
    }

    private var binding: DialogPodcastMoreActionsBinding? = null

    private val castId: Int by lazy { requireArguments().getInt(CAST_ID_KEY) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPodcastMoreActionsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setOnClicks()
    }

    private fun setOnClicks() {
        binding!!.btnCancel.setOnClickListener { dismiss() }
        binding!!.btnReportCast.setOnClickListener {
            DialogPodcastReportP2.newInstance(castId)
                .show(parentFragmentManager, DialogPodcastReportP2.TAG)
            dismiss()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
