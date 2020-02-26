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
import kotlinx.android.synthetic.main.component_edit_text.view.*
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import javax.inject.Inject

class SignUpFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: SignViewModel
    var app: App? = null

    companion object {
        fun newInstance(bundle: Bundle? = null): SignUpFragment {
            val fragment =
                SignUpFragment()
            bundle?.let { fragment.arguments = it }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_sign_up, container, false)

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
        //    edtSignUpUsername?.myEdit?.textChanges()?.map { it.toString() },
        //    edtSignUpEmail?.myEdit?.textChanges()?.map { it.toString() },
        //    edtSignUpPassword?.myEdit?.textChanges()?.map { it.toString() },
        //    //Only launch onClick function if the field pass the validations
        //    btnSignUpJoinLimor?.clicks()?.filter {
        //        validatedUsername(edtSignUpUsername?.myEdit?.text.toString()) &&
        //        validatedEmail(edtSignUpEmail?.myEdit?.text.toString()) &&
        //                validatedPassword(
        //                    edtSignUpPassword?.myEdit?.text.toString()
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
        btnSignUpFacebook?.onClick {
            view?.hideKeyboard()
            try {
                // Sign with facebook
            } catch (e: IllegalArgumentException) {
                Timber.e(getString(R.string.cant_open))
            }
        }

        btnSignUpAlreadyAccount?.onClick {
            view?.hideKeyboard()
            try {
                findNavController().popBackStack()
            } catch (e: IllegalArgumentException) {
                Timber.e(getString(R.string.cant_open))
            }
        }
    }

    //FIELDS VALIDATIONS
    private fun validatedUsername(username: String): Boolean {
        return if (username.trim().isNotBlank()) {
            edtSignUpUsername?.myEditLyt?.isErrorEnabled = false
            edtSignUpUsername?.myEditLyt?.error = null

            true
        } else {
            edtSignUpUsername?.myEditLyt?.isErrorEnabled = true
            edtSignUpUsername?.myEditLyt?.error =
            getString(R.string.error_empty_field_template, getString(R.string.username))
            edtSignUpUsername?.requestFocus()
            edtSignUpUsername?.myEdit?.background = resources.getDrawable(R.drawable.edittext, null)

            false
        }
    }

    private fun validatedEmail(email: String): Boolean {
        return if (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtSignUpEmail?.myEditLyt?.isErrorEnabled = false
            edtSignUpEmail?.myEditLyt?.error = null

            true
        } else {
            edtSignUpEmail?.myEditLyt?.isErrorEnabled = true
            edtSignUpEmail?.myEditLyt?.error = getString(R.string.error_not_valid_email)
            edtSignUpEmail?.requestFocus()
            edtSignUpEmail?.myEdit?.background = resources.getDrawable(R.drawable.edittext, null)

            false
        }
    }

    private fun validatedPassword(password: String): Boolean {
        return if (password.isNotBlank() && password.count() >= resources.getInteger(R.integer.PASSWORD_MIN_LENGTH)) {
            edtSignUpPassword?.myEditLyt?.isErrorEnabled = false
            edtSignUpPassword?.myEditLyt?.error = null

            true
        } else {
            edtSignUpPassword?.myEditLyt?.isErrorEnabled = true
            edtSignUpPassword?.myEditLyt?.error = getString(R.string.error_not_valid_password)
            edtSignUpPassword?.requestFocus()
            edtSignUpPassword?.myEdit?.background = resources.getDrawable(R.drawable.edittext, null)

            false
        }
    }
}