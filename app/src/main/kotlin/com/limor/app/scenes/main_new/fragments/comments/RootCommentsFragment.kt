package com.limor.app.scenes.main_new.fragments.comments

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
import com.limor.app.databinding.FragmentRootCommentsBinding
import com.limor.app.uimodels.CastUIModel

class RootCommentsFragment : BottomSheetDialogFragment() {

    companion object {
        private const val CAST_KEY = "CAST_KEY"
        fun newInstance(cast: CastUIModel): RootCommentsFragment {
            return RootCommentsFragment().apply {
                arguments = bundleOf(CAST_KEY to cast)
            }
        }
    }

    private val cast: CastUIModel by lazy { requireArguments().getParcelable(CAST_KEY)!! }

    private var _binding: FragmentRootCommentsBinding? = null
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
        _binding = FragmentRootCommentsBinding.inflate(inflater, container, false)

        childFragmentManager.beginTransaction()
            .replace(R.id.comment_container, FragmentComments.newInstance(cast))
            .commit()

        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show full screen bottom sheet
        dialog?.findViewById<View>(R.id.design_bottom_sheet)
            ?.updateLayoutParams {
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
