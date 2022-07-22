package com.limor.app.scenes.main_new.fragments.comments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.limor.app.App
import com.limor.app.R
import com.limor.app.databinding.FragmentRootCommentsBinding
import com.limor.app.extensions.dismissFragment
import com.limor.app.uimodels.CastUIModel

class RootCommentsFragment : BottomSheetDialogFragment() {

    companion object {
        private const val CAST_KEY = "CAST_KEY"
        private const val COMMENT_KEY = "COMMENT_KEY"
        private const val CHILD_COMMENT_KEY = "CHILD_COMMENT_KEY"
        fun newInstance(cast: CastUIModel, commentId: Int = -1, childCommentId: Int = -1): RootCommentsFragment {
            return RootCommentsFragment().apply {
                arguments = bundleOf(CAST_KEY to cast, COMMENT_KEY to commentId, CHILD_COMMENT_KEY to childCommentId)
            }
        }
    }

    private val cast: CastUIModel by lazy { requireArguments().getParcelable(CAST_KEY)!! }
    private val commentId: Int by lazy { requireArguments().getInt(COMMENT_KEY) }
    private val childCommentId: Int by lazy { requireArguments().getInt(CHILD_COMMENT_KEY) }

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
            .replace(R.id.comment_container, FragmentComments.newInstance(cast, commentId,childCommentId))
            .commit()

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            if (dialog is BottomSheetDialog) {
                val behavior: BottomSheetBehavior<*> = dialog.behavior
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(App.instance.playerBinder.isPlayingComment()){
            App.instance.playerBinder.stop()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if(App.instance.playerBinder.isPlayingComment()){
            App.instance.playerBinder.stop()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
