package io.square1.limor.scenes.authentication.fragments


import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.viewmodels.SignViewModel
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.component_edit_text.view.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import javax.inject.Inject

class SignInFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: SignViewModel
    var app: App? = null

    companion object {
        fun newInstance(bundle: Bundle? = null): SignInFragment {
            val fragment =
                SignInFragment()
            bundle?.let { fragment.arguments = it }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_sign_in, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindViewModel()
        apiCall()
        listeners()
        app = context?.applicationContext as App
    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModel =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(SignViewModel::class.java)
        }
    }

    private fun apiCall() {
        //val output = viewModel.transform(SignViewModel.Input(
        //    edtSignInEmail?.myEdit?.textChanges()?.map { it.toString() },
        //    edtSignInPassword?.myEdit?.textChanges()?.map { it.toString() },
        //    //Only launch onClick function if the field pass the validations
        //    btnSignIn?.clicks()?.filter {
        //        validatedEmail(edtSignInEmail?.myEdit?.text.toString()) &&
        //                validatedPassword(
        //                    edtSignInPassword?.myEdit?.text.toString()
        //                )
        //    }
        //))

       // output.response.observe(this, Observer {
       //     if (it) {
       //         val mainIntent = Intent(context, MainActivity::class.java)
       //         startActivity(mainIntent)
       //         (activity as SignActivity).finish()
       //     }
       // })

       // output.backgroundWorkingProgress.observe(this, Observer {
       //     trackBackgroudProgress(it)
       //     if (it)
       //         pbSignIn?.visibility = View.VISIBLE
       //     else {
       //         pbSignIn?.visibility = View.GONE
       //         view?.hideKeyboard()
       //     }
       // })

       // output.errorMessage.observe(this, Observer {
       //     if (app!!.merlinsBeard!!.isConnected) {
       //         val message: StringBuilder = StringBuilder()

       //         if (it.data.errors.isNotEmpty()) {
       //             it.data.errors.forEachIndexed { index, _ ->
       //                 if ((it.data.errors.size - 1) == index)
       //                     message.append(it.data.errors[index])
       //                 else
       //                     message.append(it.data.errors[index] + "\n")
       //             }
       //         } else {
       //             message.append(R.string.some_error)
       //         }
//
       //         alert(message.toString()) {
       //             okButton { }
       //         }.show()
       //     } else {
       //         alert(getString(R.string.default_no_internet)) {
       //             okButton {}
       //         }.show()
       //     }
       // })
    }

    private fun listeners() {
        btnSignInForgotPassword?.onClick {
            view?.hideKeyboard()
            try {
          //      findNavController().navigate(R.id.action_authSignInFragment_to_signInForgotPasswordFragment)
            } catch (e: IllegalArgumentException) {
                Timber.e(getString(R.string.cant_open))
            }
        }

        btnSignUp?.onClick {
            view?.hideKeyboard()
            try {
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            } catch (e: IllegalArgumentException) {
                Timber.e(getString(R.string.cant_open))
            }
        }
    }

    //FIELDS VALIDATIONS
    private fun validatedEmail(email: String): Boolean {
        return if (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtSignInEmail?.myEditLyt?.isErrorEnabled = false
            edtSignInEmail?.myEditLyt?.error = null

            true
        } else {
            edtSignInEmail?.myEditLyt?.isErrorEnabled = true
            edtSignInEmail?.myEditLyt?.error = getString(R.string.error_not_valid_email)
            edtSignInEmail?.requestFocus()
            edtSignInEmail?.myEdit?.background = resources.getDrawable(R.drawable.edittext, null)

            false
        }
    }

    private fun validatedPassword(password: String): Boolean {
        return if (password.isNotBlank() && password.count() >= resources.getInteger(R.integer.PASSWORD_MIN_LENGTH)) {
            edtSignInPassword?.myEditLyt?.isErrorEnabled = false
            edtSignInPassword?.myEditLyt?.error = null

            true
        } else {
            edtSignInPassword?.myEditLyt?.isErrorEnabled = true
            edtSignInPassword?.myEditLyt?.error = getString(R.string.error_not_valid_password)
            edtSignInPassword?.requestFocus()
            edtSignInPassword?.myEdit?.background = resources.getDrawable(R.drawable.edittext, null)

            false
        }
    }
}