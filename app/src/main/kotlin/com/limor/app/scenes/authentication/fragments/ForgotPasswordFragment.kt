package com.limor.app.scenes.authentication.fragments


import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.authentication.SignActivity
import com.limor.app.scenes.authentication.viewmodels.ForgotPasswordViewModel
import com.limor.app.scenes.authentication.viewmodels.SignViewModel
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.component_edit_text.view.*
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import javax.inject.Inject


class ForgotPasswordFragment : BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ForgotPasswordViewModel
    private lateinit var viewModelSignIn: SignViewModel
    private val resetPassTrigger = PublishSubject.create<Unit>()
    var app: App? = null
    var btnClose : ImageButton? = null



    companion object {
        fun newInstance(bundle: Bundle? = null): ForgotPasswordFragment {
            val fragment = ForgotPasswordFragment()
            bundle?.let { fragment.arguments = it }
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_forgot_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnClose = activity?.findViewById(R.id.btnClose)
    }

    override fun onResume() {
        super.onResume()
        if(activity is SignActivity) {
            (activity as SignActivity).showToolbar()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val userEmail: String? = arguments?.getString("email")
        userEmail?.let {
            edtForgotPasswordEmail?.myEdit?.setText(it)
        }

        app = context?.applicationContext as App

        bindViewModel()
        initView()
        apiCall()
        listeners()

    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModel =
                ViewModelProvider(fragmentActivity, viewModelFactory)
                    .get(ForgotPasswordViewModel::class.java)

            viewModelSignIn =
                ViewModelProvider(fragmentActivity, viewModelFactory)
                    .get(SignViewModel::class.java)
        }
    }


    private fun initView() {
        edtForgotPasswordEmail?.myEdit?.setText(viewModel.emailForgotViewModel)
        viewModel.emailForgotViewModel = viewModelSignIn.emailSavedViewModel
    }


    private fun apiCall() {
        val output = viewModel.transform(
            ForgotPasswordViewModel.Input(
                resetPassTrigger
            )
        )

        output.response.observe(this, Observer {
            pbForgotPassword?.visibility = View.GONE
            view?.hideKeyboard()
            if (it) {
                showOkDialogAndGotoSignInFragment()
            } else {
                toast(getString(R.string.some_error))
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbForgotPassword?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()

                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(getString(R.string.some_error))
                }

                alert(message.toString()) {
                    okButton { }
                }.show()
            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }
        })
    }


    private fun showOkDialogAndGotoSignInFragment(){
        alert(getString(R.string.error_403_forgot_password)) {
            okButton {
                //navigate to sign in and pass the email as argument
                val bundle = Bundle()
                bundle.putString("email", edtForgotPasswordEmail?.myEdit?.text.toString())
                try {
                    findNavController().navigate(R.id.action_forgotPasswordFragment_to_signInFragment, bundle)
                } catch (e: IllegalArgumentException) {
                    // User tried tapping!
                    Timber.e(getString(R.string.cant_open))
                }
            }
        }.show()
    }


    private fun listeners() {
        btnForgotPasswordReset?.onClick {
            if (validatedEmail(edtForgotPasswordEmail?.myEdit?.text.toString())) {
                viewModel.emailForgotViewModel = edtForgotPasswordEmail?.myEdit?.text.toString()
                pbForgotPassword?.visibility = View.VISIBLE
                resetPassTrigger.onNext(Unit)
            }
        }

        btnForgotPasswordAlreadyAccount?.onClick {
            try {
                findNavController().navigate(R.id.action_forgotPasswordFragment_to_signInFragment)
            } catch (e: IllegalArgumentException) {
                // User tried tapping!
                Timber.e(getString(R.string.cant_open))
            }
        }

        btnClose?.onClick {
            findNavController().navigateUp()
        }
    }


    //FIELDS VALIDATIONS
    private fun validatedEmail(email:String):Boolean{
        return if(email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtForgotPasswordEmail?.setError(null)
            true
        }else{
            edtForgotPasswordEmail?.setError(getString(R.string.error_not_valid_email))
            false
        }
    }
}
