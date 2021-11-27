package com.limor.app.scenes.main_new.view

import android.app.Dialog
import android.content.DialogInterface
import android.media.AudioFormat
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.limor.app.R
import com.limor.app.databinding.SheetEditPreviewBinding
import com.limor.app.uimodels.CastUIModel

class BottomSheetEditPreview : BottomSheetDialogFragment() {

    private val cast: CastUIModel by lazy {
        requireArguments().getParcelable(KEY_PODCAST)!!
    }

    private var _binding: SheetEditPreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SheetEditPreviewBinding.inflate(inflater, container, false);
        _binding = binding

        childFragmentManager.beginTransaction()
            .replace(R.id.content_container, EditPreviewFragment.newInstance(cast).also {
                // TODO add callbacks...
            })
            .commitNow()

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
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
        fun newInstance(cast: CastUIModel): BottomSheetEditPreview {
            return BottomSheetEditPreview().apply {
                arguments = bundleOf(KEY_PODCAST to cast)
            }
        }
    }

}