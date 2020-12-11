package com.limor.app.scenes.authentication.fragments


import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
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
import com.limor.app.scenes.authentication.viewmodels.SignViewModel
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.viewmodels.CreateFriendViewModel
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.component_edit_text.view.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.json.JSONException
import timber.log.Timber
import javax.inject.Inject


class SignInFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var viewModelSignInMail: SignViewModel
    private lateinit var viewModelSignInFB: SignFBViewModel
    private lateinit var viewModelMergeFacebookAccount: MergeFacebookAccountViewModel
    private var viewModelSignUpFB: SignUpFBViewModel? = null
    private lateinit var viewModelCreateFriend : CreateFriendViewModel

    private val loginFBTrigger = PublishSubject.create<Unit>()
    private val mergeFacebookAccountTrigger = PublishSubject.create<Unit>()
    private val createFriendTrigger = PublishSubject.create<Unit>()

    private var emailFromForgotPassword: String = ""
    private var callbackManager: CallbackManager? = null
    var app: App? = null

    companion object {
        fun newInstance(bundle: Bundle? = null): SignInFragment {
            val fragment = SignInFragment()
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
        apiCallSignInWithMail()
        apiCallSignInWithFacebook()
        apiCallMergeFacebookAccount()
        initApiCallAutofollowLimor()
        listeners()
        app = context?.applicationContext as App
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments?.containsKey("email")!!){
            emailFromForgotPassword = arguments?.get("email") as String
            if(!emailFromForgotPassword.isNullOrEmpty()){
                edtSignInEmail.setText(emailFromForgotPassword)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if(activity is SignActivity) {
            (activity as SignActivity).hideToolbar()
        }
    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelSignInMail =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(SignViewModel::class.java)

            viewModelSignInFB =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(SignFBViewModel::class.java)

            viewModelMergeFacebookAccount =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(MergeFacebookAccountViewModel::class.java)

            viewModelSignUpFB =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(SignUpFBViewModel::class.java)

            viewModelCreateFriend =
                ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(CreateFriendViewModel::class.java)
        }
    }


    private fun apiCallSignInWithMail() {
        val output = viewModelSignInMail.transform(
            SignViewModel.Input(
            edtSignInEmail.myEdit.textChanges().map { it.toString() },
            edtSignInPassword.myEdit.textChanges().map { it.toString() },
            //Only launch onClick function if the field pass the validations
            btnSignIn.clicks().filter {
                validatedEmail(edtSignInEmail.myEdit.text.toString()) && validatedPassword(edtSignInPassword.myEdit.text.toString())
            }
        ))

        output.response.observe(this, Observer {
            if (it) {
                val mainIntent = Intent(context, MainActivity::class.java)
                startActivity(mainIntent)
                (activity as SignActivity).finish()
            }
        })

       output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
            if (it)
                showProgress(true)
            else {
                showProgress(false)
                view?.hideKeyboard()
            }
       })

       output.errorMessage.observe(this, Observer {
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


    private fun listeners() {
        btnSignInForgotPassword?.onClick {
            view?.hideKeyboard()
            try {
                viewModelSignInMail.emailSavedViewModel = edtSignInEmail.myEdit.text.toString()
                findNavController().navigate(R.id.action_signInFragment_to_forgotPasswordFragment)
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

        btnSignInFacebook?.onClick {
            view?.hideKeyboard()
            //Launch facebook SDK to get data from facebook user
            getUserDataFromGraph()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    //FIELDS VALIDATIONS
    private fun validatedEmail(email: String): Boolean {
        return if (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtSignInEmail.setError(null)
            true
        } else {
            edtSignInEmail.setError(getString(R.string.error_not_valid_email))
            false
        }
    }
    private fun validatedPassword(password: String): Boolean {
        return if (password.isNotBlank() && password.count() >= resources.getInteger(R.integer.PASSWORD_MIN_LENGTH)) {
            edtSignInEmail.setError(null)
            true
        } else {
            edtSignInPassword?.setError(getString(R.string.error_not_valid_password))
            false
        }
    }
    //end Region


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

                                //Setup viewmodel fields
                                viewModelSignInFB.fbUidViewModel = fbUid
                                viewModelSignInFB.fbAccessTokenViewModel = fbToken
                                viewModelSignInFB.firstnameViewModel = firstName
                                viewModelSignInFB.lastnameViewModel = lastName
                                viewModelSignInFB.emailViewModel = email
                                viewModelSignInFB.userimageViewModel = userImageUrl

                                tryLoginWithFacebook()

                            } else {
                                Timber.e("FBLOGIN_FAILD $data")
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
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
        val output = viewModelSignInFB.transform(
            SignFBViewModel.Input(
                loginFBTrigger
            )
        )

        output.response.observe(this, Observer {
            showProgress(false)
            view?.hideKeyboard()

            var token : String
            try{
                token = it.data.token.access_token
                viewModelSignInFB.tokenInApp = token
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
                                viewModelSignInFB.fbUidViewModel,
                                viewModelSignInFB.fbAccessTokenViewModel,
                                token
                            )
                        }
                    }.show()
                }
            }

        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            showProgress(false)
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {
                if (it.code == Constants.ERROR_CODE_FACEBOOK_USER_DOES_NOT_EXISTS) {

                    val bundle = Bundle()
                    bundle.putString("fbUid", viewModelSignInFB.fbUidViewModel)
                    bundle.putString("fbToken", viewModelSignInFB.fbAccessTokenViewModel)
                    bundle.putString("firstName", viewModelSignInFB.firstnameViewModel)
                    bundle.putString("lastName", viewModelSignInFB.lastnameViewModel)
                    bundle.putString("email", viewModelSignInFB.emailViewModel)
                    bundle.putString("userImageUrl", viewModelSignInFB.userimageViewModel)
                    findNavController().navigate(R.id.action_signInFragment_to_facebookAuthFragment, bundle)

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
            showProgress(false)
            view?.hideKeyboard()
            if (it.message == "Success") {
                goToMainActivity()
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            showProgress(false)
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

    private fun showProgress(show : Boolean) {
        if(show) {
            pbSignIn?.visibility = View.VISIBLE
            btnSignIn.text = ""
        } else {
            pbSignIn?.visibility = View.GONE
            btnSignIn.text = getString(R.string.sig_in)
        }
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
    //end Region


    private fun goToMainActivity() {
        val mainIntent = Intent(context, MainActivity::class.java)
        startActivity(mainIntent)
        (activity as SignActivity).finish()
    }
}