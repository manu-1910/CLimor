package com.limor.app.dm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.limor.app.R
import com.limor.app.databinding.BottomDialogWrapperBinding
import com.limor.app.service.VoiceUploadProgress
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onDismissEvent(event: DismissEvent) {
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomDialogWrapperBinding.inflate(inflater, container, false)

        childFragmentManager.beginTransaction()
            .replace(R.id.content_container, ShareFragment.newInstance(cast))
            .commit()

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
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