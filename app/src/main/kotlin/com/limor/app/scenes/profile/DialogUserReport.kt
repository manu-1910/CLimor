package com.limor.app.scenes.profile

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import com.limor.app.databinding.DialogReportCastP2Binding
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel
import dagger.android.support.AndroidSupportInjection
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class DialogUserReport : DialogFragment() {


    companion object {
        val TAG = DialogUserReport::class.qualifiedName

        private const val DATA_TYPE_COMMENT = "comment"
        private const val DATA_TYPE_USER = "user"

        private const val KEY_DATA_ID = "DATA_ID"
        private const val KEY_DATA_TYPE = "DATA_TYPE"

        fun reportUser(userId: Int): DialogUserReport {
            return DialogUserReport().apply {
                arguments = bundleOf(
                    KEY_DATA_ID to userId,
                    KEY_DATA_TYPE to DATA_TYPE_USER
                )
            }
        }

        fun reportComment(commentId: Int): DialogUserReport {
            return DialogUserReport().apply {
                arguments = bundleOf(
                    KEY_DATA_ID to commentId,
                    KEY_DATA_TYPE to DATA_TYPE_COMMENT
                )
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: UserProfileViewModel by viewModels{ viewModelFactory }
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
    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
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
        val type = arguments?.getString(KEY_DATA_TYPE, null) ?: return
        val dataId = arguments?.getInt(KEY_DATA_ID, -1)
        if (dataId == -1) {
            return
        }

        if (DATA_TYPE_COMMENT == type) {
            model.reportComment(reason, dataId)

        } else if (DATA_TYPE_USER == type) {
            model.reportUser(reason, dataId)

        }

        getString(R.string.reported_successfully__with_format, type.replaceFirstChar {
            it.uppercase()
        }).also {
            toast(it)
        }

        dismiss()
    }

    override fun onDestroyView() {

        super.onDestroyView()
    }
}