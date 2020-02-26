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
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import javax.inject.Inject

class ForgotPasswordFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: SignViewModel
    var app: App? = null

    companion object {
        fun newInstance(bundle: Bundle? = null): ForgotPasswordFragment {
            val fragment =
                ForgotPasswordFragment()
            bundle?.let { fragment.arguments = it }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_forgot_password, container, false)

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
        //    edtForgotPasswordEmail?.myEdit?.textChanges()?.map { it.toString() }},
        //    //Only launch onClick function if the field pass the validations
        //    btnForgotPasswordReset?.clicks()?.filter {
        //        validatedEmail(edtForgotPasswordEmail?.myEdit?.text.toString())
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
       //         pbForgotPassword?.visibility = View.VISIBLE
       //     else {
       //         pbForgotPassword?.visibility = View.GONE
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
        btnForgotPasswordAlreadyAccount?.onClick {
            view?.hideKeyboard()
            try {
                findNavController().popBackStack()
            } catch (e: IllegalArgumentException) {
                Timber.e(getString(R.string.cant_open))
            }
        }
    }

    //FIELDS VALIDATIONS
    private fun validatedEmail(email: String): Boolean {
        return if (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtForgotPasswordEmail?.myEditLyt?.isErrorEnabled = false
            edtForgotPasswordEmail?.myEditLyt?.error = null

            true
        } else {
            edtForgotPasswordEmail?.myEditLyt?.isErrorEnabled = true
            edtForgotPasswordEmail?.myEditLyt?.error = getString(R.string.error_not_valid_email)
            edtForgotPasswordEmail?.requestFocus()
            edtForgotPasswordEmail?.myEdit?.background = resources.getDrawable(R.drawable.edittext, null)

            false
        }
    }
}