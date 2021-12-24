package com.limor.app.scenes.main_new.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.limor.app.EditCastActivity
import com.limor.app.R
import com.limor.app.databinding.DialogPodcastMoreActionsBinding
import com.limor.app.extensions.makeGone
import com.limor.app.extensions.makeInVisible
import com.limor.app.extensions.makeVisible
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.viewmodels.PodcastViewModel
import com.limor.app.scenes.profile.DialogUserProfileActions
import com.limor.app.scenes.profile.DialogUserReport
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayerBinder
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToAudioTrack
import dagger.android.support.AndroidSupportInjection
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
    private lateinit var updatePodcastListener : UpdatePodcastListener

    private val cast: CastUIModel by lazy { requireArguments().getParcelable(CAST_KEY)!! }

    @Inject
    lateinit var playerBinder: PlayerBinder

    var launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            updatePodcastListener.update()
        }
        dismiss()
    }

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

    fun setUpdatePodcastListener(listener: UpdatePodcastListener){
        updatePodcastListener = listener
    }

    private fun setViewsVisibility() {
        val currentUserId = PrefsHandler.getCurrentUserId(requireContext())
        val isOwner = cast.owner?.id == currentUserId

        lifecycleScope.launchWhenCreated {
            binding.loadingBar.makeGone()
            binding.visibilityGroup.makeVisible()

            if(cast.recaster == null) {
                if (isOwner) {
                    binding.btnReportCast.makeGone()
                    binding.btnBlockUser.makeGone()
                    binding.btnReportUser.makeGone()
                    binding.btnEditCast.makeVisible()

                    if (cast.maturedContent == false) {
                        binding.delimiterMatureContent.makeVisible()
                        binding.btnMatureContent.makeVisible()
                    }

                } else {
                    // Not current user cast
                    binding.btnDeleteCast.makeGone()
                    binding.btnEditCast.makeGone()
                }

            } else {
                binding.btnDeleteCast.makeGone()
            }

            if (cast.patronCast == true && isOwner) {
                binding.btnPlayPreview.makeVisible()
                binding.btnPlayPreview.setOnClickListener {
                    playPreview()
                }
            }
        }
    }

    private fun playPreview() {
        val audioTrack = cast.audio?.mapToAudioTrack() ?: return
        val startPosition = cast.patronDetails?.startsAt ?: return
        val endPosition = cast.patronDetails?.endsAt ?: return
        val playerManager = activity as? PlayerViewManager ?: return

        playerManager.playPreview(audioTrack, startPosition, endPosition)
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
            cast.owner?.let { user ->
                DialogUserProfileActions.showBlockDialog(
                    user,
                    requireContext(),
                    true,
                    this::onBlockUser
                )
            }
        }

        binding.btnReportUser.setOnClickListener {
            cast.owner?.id?.let {
                DialogUserReport.reportUser(it).show(parentFragmentManager, DialogUserReport.TAG)
                dismiss()
            }

        }

        binding.btnEditCast.setOnClickListener {
            activity?.let {
                val intent = Intent(it, EditCastActivity::class.java)
                intent.putExtra(EditCastActivity.TITLE, cast.title)
                intent.putExtra(EditCastActivity.CAPTION_TAGS, cast.caption)
                intent.putExtra(EditCastActivity.ID, cast.id)
                launcher.launch(intent)
            }
        }

        binding.btnMatureContent.setOnClickListener {
            showMatureContentDialog()
        }
    }

    private fun showMatureContentDialog() {
        LimorDialog(layoutInflater).apply {
            setTitle(R.string.mature_content)
            setMessage(R.string.mature_content_description)
            setIcon(R.drawable.ic_change_price)
            addButton(R.string.yes_title, false) { markAsMature() }
            addButton(R.string.no_title, true)
        }.show()
    }

    private fun markAsMature() {
        cast.maturedContent = true
        binding.btnMatureContent.makeGone()
        binding.delimiterMatureContent.makeGone()
        podcastViewModel.markAsMature(cast.id)
    }

    private fun onBlockUser() {
        val user = cast.owner ?: return
        podcastViewModel.blockUser(user.id).observe(viewLifecycleOwner) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set("reload_feed", false)
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    interface UpdatePodcastListener{
        fun update()
    }

}
