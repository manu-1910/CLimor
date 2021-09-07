package com.limor.app.scenes.profile

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
import com.limor.app.databinding.DialogOtherUserActionsBinding
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel
import com.limor.app.scenes.main.fragments.settings.SettingsActivity
import com.limor.app.scenes.main_new.fragments.DialogPodcastReportP2
import com.limor.app.uimodels.UserUIModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import timber.log.Timber
import javax.inject.Inject

class DialogUserProfileActions : DialogFragment() {

    companion object{
        const val USER_KEY = "user"
    }
    private val model: UserProfileViewModel by viewModels({ activity as UserProfileActivity }) { (activity as UserProfileActivity).viewModelFactory }
    lateinit var binding: DialogOtherUserActionsBinding
    lateinit var args: UserUIModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogOtherUserActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        getArgs()
        setOnClicks()
    }

    private fun getArgs() {
        arguments?.let {
            args = it.getParcelable(USER_KEY)!!
            Timber.d(args.toString())
            setUserActions(args)
        }
    }

    private fun setUserActions(user: UserUIModel) {
        if(user.isBlocked == true){
            binding.btnUnBlockUser.visibility = View.VISIBLE
            binding.btnBlockUser.visibility = View.GONE
        }else{
            binding.btnUnBlockUser.visibility = View.GONE
            binding.btnBlockUser.visibility = View.VISIBLE
        }
    }

    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { this.dismiss() }
        binding.btnBlockUser.setOnClickListener {
            alert(getString(R.string.confirmation_block_user)) {
                okButton {
                    model.blockUser(args.id)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("blocked", true)
                    findNavController().popBackStack()
                }
                cancelButton {  }
            }.show()


        }
        binding.btnUnBlockUser.setOnClickListener {
            alert(getString(R.string.confirmation_unblock_user)) {
                okButton {
                    model.unblockUser(args.id)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("blocked", false)
                    findNavController().popBackStack()
                }
                cancelButton {  }
            }.show()

        }

        binding.btnReportUser.setOnClickListener {
            DialogUserReport.newInstance(args.id)
                .show(parentFragmentManager, DialogUserReport.TAG)
            dismiss()
        }
    }
}