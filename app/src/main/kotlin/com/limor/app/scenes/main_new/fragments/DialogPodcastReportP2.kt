package com.limor.app.scenes.main_new.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.databinding.DialogReportCastP2Binding
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel
import com.limor.app.scenes.main.viewmodels.PodcastViewModel
import com.limor.app.scenes.profile.DialogUserReport
import dagger.android.support.AndroidSupportInjection
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

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

    private var binding: DialogReportCastP2Binding? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PodcastViewModel by viewModels { viewModelFactory }

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

        binding?.btnAbusiveContent?.setOnClickListener {
            handleReport("Abusive content")
            this.dismiss()
        }
        binding?.btnSpamOrScam?.setOnClickListener {
            handleReport("Spam")
            this.dismiss()
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    private fun handleReport(s: String) {
        model.reportCast(s, arguments?.getInt(CAST_ID_KEY))
        toast("Reported Successfully")
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}