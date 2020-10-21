package com.limor.app.scenes.authentication.fragments


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
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.common.SessionManager
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.authentication.SignActivity
import com.limor.app.scenes.authentication.viewmodels.MergeFacebookAccountViewModel
import com.limor.app.scenes.authentication.viewmodels.SignFBViewModel
import com.limor.app.scenes.authentication.viewmodels.SignUpFBViewModel
import com.limor.app.scenes.authentication.viewmodels.SignUpViewModel
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.viewmodels.CreateFriendViewModel
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.component_edit_text.view.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.json.JSONException
import timber.log.Timber
import javax.inject.Inject


class SignUpFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var viewModel: SignUpViewModel
    private var viewModelSignInFB: SignFBViewModel? = null
    private lateinit var viewModelSignUpFB: SignUpFBViewModel
    private lateinit var viewModelMergeFacebookAccount: MergeFacebookAccountViewModel
    private lateinit var viewModelCreateFriend : CreateFriendViewModel

    private val signUpTrigger = PublishSubject.create<Unit>()
    private val mergeFacebookAccountTrigger = PublishSubject.create<Unit>()
    private val createFriendTrigger = PublishSubject.create<Unit>()
    private val loginFBTrigger = PublishSubject.create<Unit>()

    private var callbackManager: CallbackManager? = null

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

    override fun onResume() {
        super.onResume()
        if(activity is SignActivity) {
            (activity as SignActivity).hideToolbar()
        }
    }




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        app = context?.applicationContext as App
        if(viewModelSignInFB == null) {
            bindViewModel()
            apiCall()
            apiCallSignInWithFacebook()
            apiCallMergeFacebookAccount()
            initApiCallAutofollowLimor()
        }

        setMessageWithClickableLink(tvTermsAndConditions)
        listeners()
    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModel =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(SignUpViewModel::class.java)

            viewModelSignInFB =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(SignFBViewModel::class.java)

            viewModelSignUpFB =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(SignUpFBViewModel::class.java)

            viewModelMergeFacebookAccount =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(MergeFacebookAccountViewModel::class.java)

            viewModelCreateFriend =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(CreateFriendViewModel::class.java)
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
                viewModelCreateFriend.idNewFriend = Constants.LIMOR_ACCOUNT_ID
                createFriendTrigger.onNext(Unit)

//                val mainIntent = Intent(context, MainActivity::class.java)
//                startActivity(mainIntent)
//                (activity as SignActivity).finish()
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
                if (it.errorMessage!!.isNotEmpty() && it.errorMessage!!.isNotBlank()) {
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


    private fun initApiCallAutofollowLimor() {
        val output = viewModelCreateFriend.transform(
            CreateFriendViewModel.Input(
                createFriendTrigger
            )
        )

        output.response.observe(this, Observer {
            view?.hideKeyboard()
            goToMainActivity()
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()

            // TODO: maybe we should do some checks
            goToMainActivity()
        })
    }


    private fun listeners() {
        btnSignUpFacebook?.onClick {
            view?.hideKeyboard()
            getUserDataFromGraph()
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

        tvTermsAndConditions?.onClick {}
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
    //end Region


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }


    //Region Methods for Facebook SignIn
    private fun getUserDataFromGraph() {
        // Login
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {

                    val request = GraphRequest.newMeRequest(loginResult.accessToken) { data, response ->
                        try {
                            //here is the data that you want
                            Timber.d("FBSIGNUP_JSON_RES $data")

                            if (data.has("id")) {
                                Timber.d("Facebook token: $loginResult.accessToken.token ")
                                //startActivity(Intent(context, MainActivity::class.java))
                                val fbUid: String = AccessToken.getCurrentAccessToken().userId
                                val fbToken: String = AccessToken.getCurrentAccessToken().token
                                var firstName = ""
                                var lastName = ""
                                var email = ""
                                var userImageUrl = ""
                                try {
                                    firstName = data.getString("first_name")
                                    lastName = data.getString("last_name")
                                    email = data.getString("email")
                                    userImageUrl = "https://graph.facebook.com/$fbUid/picture?type=large"
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }

                                //Setup viewmodel fields if we need a signup
                                viewModelSignUpFB.fbUidViewModel = fbUid
                                viewModelSignUpFB.fbAccessTokenViewModel = fbToken
                                viewModelSignUpFB.firstnameViewModel = firstName
                                viewModelSignUpFB.lastnameViewModel = lastName
                                viewModelSignUpFB.emailViewModel = email
                                viewModelSignUpFB.userimageViewModel = userImageUrl

                                //Setup viewmodel fields if we need a signin
                                viewModelSignInFB?.fbUidViewModel = fbUid
                                viewModelSignInFB?.fbAccessTokenViewModel = fbToken
                                viewModelSignInFB?.firstnameViewModel = firstName
                                viewModelSignInFB?.lastnameViewModel = lastName
                                viewModelSignInFB?.emailViewModel = email
                                viewModelSignInFB?.userimageViewModel = userImageUrl


                                tryLoginWithFacebook()

                            } else {
                                Timber.e("FBLOGIN_FAILD $data")
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            //dismissDialogLogin()
                        }
                    }

                    val parameters = Bundle()
                    parameters.putString("fields", "first_name, last_name, email")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    Timber.d("Facebook onCancel.")
                }

                override fun onError(error: FacebookException) {
                    error.printStackTrace()
                    toast(error.message.toString())
                    Timber.d("Facebook onError.")

                }
            })

    }


    private fun tryLoginWithFacebook() {
        loginFBTrigger.onNext(Unit)
    }
    private fun apiCallSignInWithFacebook() {
        val output = viewModelSignInFB?.transform(
            SignFBViewModel.Input(
                loginFBTrigger
            )
        )

        output?.response?.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()

            var token = ""
            try{
                token = it.data.token.access_token
                viewModelSignInFB?.tokenInApp = token
            }catch (e: Exception){
                token = ""
                e.printStackTrace()
            }

            if(it.message == "Success"){
                goToMainActivity()
            }else{
                if (it.code == Constants.ERROR_CODE_FACEBOOK_USER_EXISTS) {
                    alert(it.message) {
                        okButton {
                            mergeAccounts(
                                viewModelSignInFB!!.fbUidViewModel,
                                viewModelSignInFB!!.fbAccessTokenViewModel,
                                token
                            )
                        }
                    }.show()
                }
            }

        })

        output?.backgroundWorkingProgress?.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output?.errorMessage?.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {
                if (it.code == Constants.ERROR_CODE_FACEBOOK_USER_DOES_NOT_EXISTS) {

                    val bundle = Bundle()
                    bundle.putString("fbUid", viewModelSignInFB?.fbUidViewModel)
                    bundle.putString("fbToken", viewModelSignInFB?.fbAccessTokenViewModel)
                    bundle.putString("firstName", viewModelSignInFB?.firstnameViewModel)
                    bundle.putString("lastName", viewModelSignInFB?.lastnameViewModel)
                    bundle.putString("email", viewModelSignInFB?.emailViewModel)
                    bundle.putString("userImageUrl", viewModelSignInFB?.userimageViewModel)
                    findNavController().navigate(R.id.action_signUpFragment_to_facebookAuthFragment, bundle)


                }else{
                    val message: StringBuilder = StringBuilder()
                    if (it.errorMessage!!.isNotEmpty()) {
                        message.append(it.errorMessage)
                    } else {
                        message.append(getString(R.string.some_error))
                    }
                    alert(message.toString()) {
                        okButton { }
                    }.show()
                }

            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }
        })
    }


    private fun mergeAccounts(fbUid: String, fbToken: String, temporaryAccessToken: String) {
        viewModelMergeFacebookAccount.fbAccessTokenViewModel = fbToken
        viewModelMergeFacebookAccount.fbUidViewModel = fbUid

        mergeFacebookAccountTrigger.onNext(Unit)
    }
    private fun apiCallMergeFacebookAccount() {
        val output = viewModelMergeFacebookAccount.transform(
            MergeFacebookAccountViewModel.Input(
                mergeFacebookAccountTrigger
            )
        )

        output.response.observe(this, Observer {
            pbSignIn?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.message == "Success") {
                viewModelCreateFriend.idNewFriend = Constants.LIMOR_ACCOUNT_ID
                createFriendTrigger.onNext(Unit)
                goToMainActivity()
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbSignIn?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
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
    // end Region


    private fun goToMainActivity() {
        val mainIntent = Intent(context, MainActivity::class.java)
        startActivity(mainIntent)
        (activity as SignActivity).finish()
    }
}