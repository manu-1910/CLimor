package com.limor.app.dm.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.limor.app.R
import com.limor.app.databinding.BottomDialogWrapperBinding
import com.limor.app.dm.ShareResult
import com.limor.app.uimodels.CastUIModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ShareDialog : BottomSheetDialogFragment() {

    class DismissEvent {
        companion object {
            fun dismiss() = EventBus.getDefault().post(DismissEvent())
        }
    }

    private var onShared: ((shareResult: ShareResult) -> Unit)? = null

    private var _binding: BottomDialogWrapperBinding? = null
    private val binding get() = _binding!!
    private val cast: CastUIModel by lazy {
        requireArguments().getParcelable(KEY_PODCAST)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    fun setOnSharedListener(onSharedListener: ((shareResult: ShareResult) -> Unit)?) {
        onShared = onSharedListener
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDismissEvent(event: DismissEvent) {
        _binding?.root?.post {
            dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomDialogWrapperBinding.inflate(inflater, container, false)

        childFragmentManager.beginTransaction()
            .replace(R.id.content_container, ShareFragment.newInstance(cast).also {
                it.setOnSharedListener(onShared)
            })
            .commitNow()

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun adjustHeight(full: Boolean) {
        dialog?.findViewById<View>(R.id.design_bottom_sheet)?.updateLayoutParams {
            height = if (full) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            if (dialog is BottomSheetDialog) {
                val behavior: BottomSheetBehavior<*> = dialog.behavior
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
    }

    companion object {
        private const val KEY_PODCAST = "KEY_PODCAST"
        fun newInstance(cast: CastUIModel): ShareDialog {
            return ShareDialog().apply {
                arguments = bundleOf(KEY_PODCAST to cast)
            }
        }
    }
}