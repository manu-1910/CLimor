package com.limor.app.scenes.profile

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
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
import dagger.android.support.AndroidSupportInjection
import org.jetbrains.anko.layoutInflater
import timber.log.Timber

class DialogUserProfileActions : DialogFragment() {

    companion object{
        const val USER_KEY = "user"

        fun showBlockDialog(user: UserUIModel, context: Context, block: Boolean, onConfirmed: (() -> Unit)? = null) {
            val userName = user.username ?: ""
            val messageId = if (block) R.string.confirmation_block_user__with_format else
                R.string.confirmation_unblock_user__with_format
            val message = context.getString(messageId, userName)
            val spannable = SpannableString(message)

            // - 1 because of the question mark at the end
            val start = message.length - userName.length - 1
            val end = start + userName.length
            val flags = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

            // Black
            spannable.setSpan(ForegroundColorSpan(Color.BLACK), start, end, flags)

            // Bold
            spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, flags)

            LimorDialog(context.layoutInflater).apply {
                setTitle(if (block) R.string.block_user else R.string.unblock_user)
                setMessage(spannable)
                setIcon(R.drawable.ic_block_user_full)
                addButton(if (block) R.string.block else R.string.unblock, false) {
                    onConfirmed?.invoke()
                }
                addButton(R.string.cancel, true)
            }.show()
        }
    }


    private val model: UserProfileViewModel by viewModels({ activity as UserProfileActivity }) { (activity as UserProfileActivity).viewModelFactory }
    lateinit var binding: DialogOtherUserActionsBinding
    lateinit var user: UserUIModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogOtherUserActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        getArgs()
        setOnClicks()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    private fun getArgs() {
        arguments?.let {
            user = it.getParcelable(USER_KEY)!!
            Timber.d(user.toString())
            setUserActions(user)
        }
    }

    private fun setUserActions(user: UserUIModel) {
        binding.btnUnfollowUser.visibility = if (user.isFollowed == true) View.VISIBLE else View.GONE
        binding.separatorBtnUnfollowUser.visibility = binding.btnUnfollowUser.visibility

        if (user.isBlocked == true) {
            binding.btnUnBlockUser.visibility = View.VISIBLE
            binding.btnBlockUser.visibility = View.GONE
        } else {
            binding.btnUnBlockUser.visibility = View.GONE
            binding.btnBlockUser.visibility = View.VISIBLE
        }

        binding.separatorBtnUnBlockUser.visibility = binding.btnUnBlockUser.visibility
    }

    private fun onBlockUser() {
        model.blockUser(user.id)
        navigateAfterBlock(true)
    }

    private fun onUnBlockUser(){
        model.unblockUser(user.id)
        navigateAfterBlock(false)
    }

    private fun navigateAfterBlock(blocked: Boolean) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set("blocked", blocked)
        findNavController().popBackStack()
    }

    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { this.dismiss() }
        binding.btnBlockUser.setOnClickListener {
            showBlockDialog(user, requireContext(), true, this::onBlockUser)
        }
        binding.btnUnBlockUser.setOnClickListener {
            showBlockDialog(user, requireContext(), false, this::onUnBlockUser)
        }

        binding.btnReportUser.setOnClickListener {
            DialogUserReport.reportUser(user.id)
                .show(parentFragmentManager, DialogUserReport.TAG)
            dismiss()
        }

        binding.btnUnfollowUser.setOnClickListener {
            user = user.copy(isFollowed = false)
            model.unFollow(user.id)
            findNavController().previousBackStackEntry?.savedStateHandle?.set("followed", false)
            findNavController().popBackStack()
        }
    }
}