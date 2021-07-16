package com.limor.app.scenes.main_new.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.limor.app.databinding.DialogPodcastMoreActionsBinding
import com.limor.app.extensions.makeGone
import com.limor.app.extensions.makeInVisible
import com.limor.app.extensions.makeVisible
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.uimodels.CastUIModel

class DialogPodcastMoreActions : DialogFragment() {

    companion object {
        val TAG = DialogPodcastMoreActions::class.qualifiedName
        const val CAST_KEY = "CAST_KEY"

        fun newInstance(cast: CastUIModel): DialogPodcastMoreActions {
            return DialogPodcastMoreActions().apply {
                arguments = bundleOf(CAST_KEY to cast)
            }
        }
    }

    private var _binding: DialogPodcastMoreActionsBinding? = null
    private val binding get() = _binding!!

    private val cast: CastUIModel by lazy { requireArguments().getParcelable(CAST_KEY)!! }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPodcastMoreActionsBinding.inflate(inflater, container, false)
        binding.visibilityGroup.makeInVisible()
        setViewsVisibility()
        return binding.root
    }

    private fun setViewsVisibility() {
        lifecycleScope.launchWhenCreated {
            val currentUserId = JwtChecker.getUserIdFromJwt()

            binding.loadingBar.makeGone()
            binding.visibilityGroup.makeVisible()
            if (currentUserId != cast.owner?.id) {
                // Not current user cast
                binding.btnDeleteCast.makeGone()
                binding.btnEditCast.makeGone()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setOnClicks()
    }

    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnReportCast.setOnClickListener {
            DialogPodcastReportP2.newInstance(cast.id)
                .show(parentFragmentManager, DialogPodcastReportP2.TAG)
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
