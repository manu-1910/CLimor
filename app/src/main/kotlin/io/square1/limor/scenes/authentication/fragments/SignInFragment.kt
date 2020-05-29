package io.square1.limor.scenes.authentication.fragments


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
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.authentication.viewmodels.MergeFacebookAccountViewModel
import io.square1.limor.scenes.authentication.viewmodels.SignFBViewModel
import io.square1.limor.scenes.authentication.viewmodels.SignViewModel
import io.square1.limor.scenes.main.MainActivity
import io.square1.limor.scenes.utils.CommonsKt.Companion.toEditable
import io.square1.limor.uimodels.UISignUpUser
import kotlinx.android.synthetic.main.component_edit_text.view.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.json.JSONException
import timber.log.Timber
import javax.inject.Inject


class SignInFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var sessionManager: SessionManager

    private lateinit var viewModelSignInMail: SignViewModel
    private lateinit var viewModelSignInFB: SignFBViewModel
    private lateinit var viewModelMergeFacebookAccount: MergeFacebookAccountViewModel


    private var emailFromForgotPassword: String = ""
    private var callbackManager: CallbackManager? = null

    private val signUpFBLoginTrigger = PublishSubject.create<Unit>()
    private val mergeFacebookAccountTrigger = PublishSubject.create<Unit>()

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
        listeners()
        app = context?.applicationContext as App
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments?.containsKey("email")!!){
            emailFromForgotPassword = arguments?.get("email") as String
            if(!emailFromForgotPassword.isNullOrEmpty()){
                edtSignInEmail.myEdit.text = emailFromForgotPassword.toEditable()
            }
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
        }
    }


    private fun apiCallSignInWithMail() {
        val output = viewModelSignInMail.transform(SignViewModel.Input(
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
                pbSignIn?.visibility = View.VISIBLE
            else {
                pbSignIn?.visibility = View.GONE
                view?.hideKeyboard()
            }
       })

       output.errorMessage.observe(this, Observer {
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


    private fun apiCallSignInWithFacebook() {
        val output = viewModelSignInFB.transform(
            SignFBViewModel.Input(
                signUpFBLoginTrigger
            )
        )

        output.response.observe(this, Observer {
            pbSignIn?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) {
                /*val mainIntent = Intent(context, MainActivity::class.java)
                startActivity(mainIntent)
                (activity as SignActivity).finish()*/

                //sessionManager.storeToken(it.data.token.access_token)

                mergeAccounts(
                    viewModelSignInFB.fbUidViewModel,
                    viewModelSignInFB.fbAccessTokenViewModel,
                    it.data.token.access_token
                )

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


    private fun apiCallMergeFacebookAccount() {
        val output = viewModelMergeFacebookAccount.transform(
            MergeFacebookAccountViewModel.Input(
                mergeFacebookAccountTrigger
            )
        )

        output.response.observe(this, Observer {
            pbSignIn?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) {
                proceedLogin(it.data.token.access_token)
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

        //TODO only implemented to go to MainActivity to continue the development JJ
        btnSignInFacebook?.onClick {
            view?.hideKeyboard()
            getUserDataFromGraph()
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



    //Region Methods for Facebook SignIn

    private fun getUserDataFromGraph() {

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    val grantedPermissions: Set<String> = loginResult!!.recentlyGrantedPermissions
                    if (grantedPermissions.contains("email") && grantedPermissions.contains("public_profile")) {
                        val request: GraphRequest =
                            GraphRequest.newMeRequest(loginResult.accessToken
                            ) { obj, _ ->
                                val fbUid: String = AccessToken.getCurrentAccessToken().userId
                                val fbToken: String = AccessToken.getCurrentAccessToken().token
                                var firstName: String? = null
                                var lastName: String? = null
                                var email: String? = null
                                var userImageUrl: String? = null
                                try {
                                    firstName = obj.getString("first_name")
                                    lastName = obj.getString("last_name")
                                    email = obj.getString("email")
                                    userImageUrl = "https://graph.facebook.com/$fbUid/picture?type=large"
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                                val user = UISignUpUser(
                                    email.toString(),
                                    "",
                                    "$firstName $lastName"
                                )
                                tryLoginWithFacebook(fbUid, fbToken, user)
                            }
                        val parameters = Bundle()
                        parameters.putString("fields", "first_name, last_name, email")
                        request.parameters = parameters
                        request.executeAsync()
                    } else {
                        alert { getString(R.string.you_have_no_permissions_facebook) }
                    }
                }

                override fun onCancel() {}
                override fun onError(error: FacebookException) {
                    alert { error.localizedMessage }
                }

            })
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
    }


    private fun tryLoginWithFacebook(fbUid: String, fbToken: String, user: UISignUpUser) {

        viewModelSignInFB.fbAccessTokenViewModel = fbToken
        viewModelSignInFB.fbUidViewModel = fbUid
        viewModelSignInFB.userViewModel = user

        signUpFBLoginTrigger.onNext(Unit)
    }


    private fun mergeAccounts(fbUid: String, fbToken: String, temporaryAccessToken: String) {
        sessionManager.storeToken(temporaryAccessToken)

        viewModelMergeFacebookAccount.fbAccessTokenViewModel = fbToken
        viewModelMergeFacebookAccount.fbUidViewModel = fbUid

        mergeFacebookAccountTrigger.onNext(Unit)
    }

    private fun proceedLogin(accessToken: String) {

        sessionManager.storeToken(accessToken);

        //DataManager.getInstance().getUserInfoData(true, null)

        val mainIntent = Intent(context, MainActivity::class.java)
        startActivity(mainIntent)
        (activity as SignActivity).finish()
    }
    // end Region
}