package com.limor.app.scenes.main_new.fragments

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.apollo.CastsRepository
import com.limor.app.apollo.UserRepository
import com.limor.app.databinding.DialogPodcastMoreActionsBinding
import com.limor.app.extensions.makeGone
import com.limor.app.extensions.makeInVisible
import com.limor.app.extensions.makeVisible
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.main.viewmodels.PodcastViewModel
import com.limor.app.scenes.profile.DialogUserReport
import com.limor.app.uimodels.CastUIModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.launch
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import javax.inject.Inject

class DialogPodcastMoreActions : DialogFragment() {

    companion object {
        val TAG = DialogPodcastMoreActions::class.qualifiedName
        const val CAST_KEY = "CAST_KEY"

        fun newInstance(cast: CastUIModel
                        ): DialogPodcastMoreActions {
            return DialogPodcastMoreActions().apply {
                arguments = bundleOf(CAST_KEY to cast)
            }
        }
    }
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val podcastViewModel : PodcastViewModel by viewModels{viewModelFactory}

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
            if(cast.recaster == null){
                if (currentUserId != cast.owner?.id) {
                    // Not current user cast
                    binding.btnDeleteCast.makeGone()
                    binding.btnEditCast.makeGone()
                }else{
                    binding.btnReportCast.makeGone()
                    binding.btnBlockUser.makeGone()
                    binding.btnReportUser.makeGone()
                }
            } else{
                binding.btnDeleteCast.makeGone()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
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
        binding.btnDeleteCast.setOnClickListener {
            alert(getString(R.string.confirmation_delete_podcast)) {
                okButton {
                    podcastViewModel.deleteCastById(cast.id)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("reload_feed", true)
                    dismiss()
                }
                cancelButton {  }
            }.show()

        }

        binding.btnBlockUser.setOnClickListener {
            cast.owner?.id?.let { id ->
                alert(getString(R.string.confirmation_block_user)) {
                    okButton {
                        podcastViewModel.blockUser(id)
                        findNavController().previousBackStackEntry?.savedStateHandle?.set("reload_feed", false)
                        dismiss()
                    }
                    cancelButton {  }
                }.show()

            }

        }

        binding.btnReportUser.setOnClickListener {
            cast.owner?.id?.let {
                DialogUserReport.newInstance(it)
                    .show(parentFragmentManager, DialogUserReport.TAG)
                dismiss()
            }

        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
