package io.square1.limor.scenes.authentication.fragments


import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.Constants
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.authentication.viewmodels.SignUpViewModel
import io.square1.limor.scenes.main.MainActivity
import kotlinx.android.synthetic.main.component_edit_text.view.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import javax.inject.Inject


class SignUpFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: SignUpViewModel
    private val signUpTrigger = PublishSubject.create<Unit>()
    var app: App? = null


    companion object {
        fun newInstance(bundle: Bundle? = null): SignUpFragment {
            val fragment = SignUpFragment()
            bundle?.let { fragment.arguments = it }
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_sign_up, container, false)


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindViewModel()
        apiCall()
        setMessageWithClickableLink(tvTermsAndConditions)
        listeners()
        app = context?.applicationContext as App
    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModel =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(SignUpViewModel::class.java)
        }
    }


    private fun apiCall() {
        val output = viewModel.transform(
            SignUpViewModel.Input(
                signUpTrigger
            )
        )

        output.response.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) {
                val mainIntent = Intent(context, MainActivity::class.java)
                startActivity(mainIntent)
                (activity as SignActivity).finish()
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (it.errorMessage.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
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

    private fun listeners() {
        btnSignUpFacebook?.onClick {
            view?.hideKeyboard()
            try {
                toast("Signup with Facebook")
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

        btnSignUpJoinLimor?.onClick {
            if (validatedUsername(edtSignUpUsername?.myEdit?.text.toString()) &&
                validatedEmail(edtSignUpEmail?.myEdit?.text.toString()) &&
                validatedPassword(edtSignUpPassword?.myEdit?.text.toString())
                //validatedTermsAndConditions()
            ) {
                saveVariablesIntoViewModel()
                pbSignUp?.visibility = View.VISIBLE
                signUpTrigger.onNext(Unit)
            }

        }

        tvTermsAndConditions?.onClick {

        }
    }

    private fun setMessageWithClickableLink(textView: TextView) {
        val content = getString(R.string.terms)
        val url = Constants.TERMS_URL

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(app!!.applicationContext, R.color.brandPrimary500)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
        }

        val startIndex = content.indexOf("Terms and Conditions")
        val endIndex = startIndex + "Terms and Conditions".length
        val spannableString = SpannableString(content)
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    private fun saveVariablesIntoViewModel() {
        viewModel.userNameViewModel = edtSignUpUsername.myEdit.text.toString()
        viewModel.emailViewModel = edtSignUpEmail.myEdit.text.toString()
        viewModel.passwordViewModel = edtSignUpPassword.myEdit.text.toString()
    }

    //FIELDS VALIDATIONS
    private fun validatedUsername(username: String): Boolean {
        return if (username.trim().isNotBlank()) {
            edtSignUpUsername?.myEditLyt?.isErrorEnabled = false
            edtSignUpUsername?.myEditLyt?.error = null

            true
        } else {
            edtSignUpUsername?.myEditLyt?.isErrorEnabled = true
            edtSignUpUsername?.myEditLyt?.error = getString(R.string.error_empty_field_template, getString(R.string.username))
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