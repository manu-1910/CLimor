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
import com.limor.app.R
import com.limor.app.databinding.DialogOtherUserActionsBinding
import com.limor.app.databinding.DialogReportCastBinding
import com.limor.app.databinding.DialogUserActionsBinding
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.main_new.utils.ArgsConverter
import com.limor.app.scenes.main_new.utils.ReportDialogArgs
import com.limor.app.uimodels.UserUIModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DialogUserProfileActions : DialogFragment() {

    companion object{
        const val USER_KEY = "user"
    }
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: UserProfileViewModel by viewModels { viewModelFactory }
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
        }
    }

    private fun setOnClicks() {
        binding.btnCancel.setOnClickListener { this.dismiss() }
        binding.btnBlockUser.setOnClickListener {
            model.blockUser(args.id)

        }
        binding.btnUnBlockUser.setOnClickListener {
            model.unblockUser(args.id)
        }
    }
}