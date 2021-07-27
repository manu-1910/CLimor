package com.limor.app.scenes.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.limor.app.databinding.DialogReportCastP2Binding
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel

class DialogUserReport : DialogFragment() {
    private val model: UserProfileViewModel by viewModels({ activity as UserProfileActivity }) { (activity as UserProfileActivity).viewModelFactory }
    companion object {
        val TAG = DialogUserReport::class.qualifiedName
        private const val CAST_ID_KEY = "CAST_ID_KEY"
        fun newInstance(castId: Int): DialogUserReport {
            return DialogUserReport().apply {
                arguments = bundleOf(CAST_ID_KEY to castId)
            }
        }
    }
    private lateinit var binding : DialogReportCastP2Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogReportCastP2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setOnClicks()
    }

    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { this.dismiss() }


        binding.btnSpamOrScam.setOnClickListener {
            handleReport("Spam")
        }

        binding.btnAbusiveContent.setOnClickListener {
            handleReport("Abusive Content")
        }
    }

    private fun handleReport(reason: String) {
        model.reportUser(reason,arguments?.getInt(CAST_ID_KEY))
        dismiss()
    }

    override fun onDestroyView() {

        super.onDestroyView()
    }
}