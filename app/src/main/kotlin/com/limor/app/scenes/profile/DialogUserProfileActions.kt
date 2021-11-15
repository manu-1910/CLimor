package com.limor.app.scenes.profile

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.databinding.DialogOtherUserActionsBinding
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.uimodels.UserUIModel
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import timber.log.Timber

class DialogUserProfileActions : DialogFragment() {

    companion object{
        const val USER_KEY = "user"
    }
    private val model: UserProfileViewModel by viewModels({ activity as UserProfileActivity }) { (activity as UserProfileActivity).viewModelFactory }
    lateinit var binding: DialogOtherUserActionsBinding
    lateinit var user: UserUIModel
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
            user = it.getParcelable(USER_KEY)!!
            Timber.d(user.toString())
            setUserActions(user)
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

    private fun showBlockDialog() {
        val userName = user.username ?: ""
        val message = getString(R.string.confirmation_block_user__with_format, userName)
        val spannable = SpannableString(message)

        // - 1 because of the question mark at the end
        val start = message.length - userName.length - 1
        val end = start + userName.length
        val flags = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

        // Black
        spannable.setSpan(ForegroundColorSpan(Color.BLACK), start, end, flags)

        // Bold
        spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, flags)

        LimorDialog(layoutInflater).apply {
            setTitle(R.string.block_user)
            setMessage(spannable)
            setIcon(R.drawable.ic_block_user_full)
            addButton(R.string.block, false) { onBlockUser() }
            addButton(R.string.cancel, true)
        }.show()
    }

    private fun onBlockUser() {
        model.blockUser(user.id)
        findNavController().previousBackStackEntry?.savedStateHandle?.set("blocked", true)
        findNavController().popBackStack()
    }

    private fun showUnBlockUserDialog(){
        val userName = user.username ?: ""
        val message = getString(R.string.confirmation_unblock_user__with_format, userName)
        val spannable = SpannableString(message)

        // - 1 because of the question mark at the end
        val start = message.length - userName.length - 1
        val end = start + userName.length
        val flags = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

        // Black
        spannable.setSpan(ForegroundColorSpan(Color.BLACK), start, end, flags)

        // Bold
        spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, flags)

        LimorDialog(layoutInflater).apply {
            setTitle(R.string.unblock_user)
            setMessage(spannable)
            setIcon(R.drawable.ic_block_user_full)
            addButton(R.string.unblock, false) { onUnBlockUser() }
            addButton(R.string.cancel, true)
        }.show()
    }

    private fun onUnBlockUser(){
        model.unblockUser(user.id)
        findNavController().previousBackStackEntry?.savedStateHandle?.set("blocked", false)
        findNavController().popBackStack()
    }

    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { this.dismiss() }
        binding.btnBlockUser.setOnClickListener {
            showBlockDialog()
        }
        binding.btnUnBlockUser.setOnClickListener {
            showUnBlockUserDialog()
        }

        binding.btnReportUser.setOnClickListener {
            DialogUserReport.newInstance(user.id)
                .show(parentFragmentManager, DialogUserReport.TAG)
            dismiss()
        }
    }
}