package com.limor.app.scenes.authentication.fragments


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.limor.app.App
import com.limor.app.BuildConfig
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
import com.limor.app.uimodels.UISignUpUser
import com.limor.app.uimodels.UIUsernameResponse
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.component_edit_text.view.*
import kotlinx.android.synthetic.main.fragment_facebook_auth.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.uiThread
import timber.log.Timber
import javax.inject.Inject


class FacebookAuthFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var viewModelSignInMail: SignViewModel
    private lateinit var viewModelSignInFB: SignFBViewModel
    private lateinit var viewModelMergeFacebookAccount: MergeFacebookAccountViewModel
    private lateinit var viewModelSignUpFB: SignUpFBViewModel
    private lateinit var viewModelCreateFriend : CreateFriendViewModel

    private val signUpFBLoginTrigger = PublishSubject.create<Unit>()
    private val mergeFacebookAccountTrigger = PublishSubject.create<Unit>()
    private val signUpFBTrigger = PublishSubject.create<Unit>()
    private val createFriendTrigger = PublishSubject.create<Unit>()

    private var fbUid = ""
    private var fbToken = ""
    private var firstName = ""
    private var lastName = ""
    private var email = ""
    private var userImageUrl = ""
    private var usernameIsUnique = false
    var app: App? = null
    var btnClose : ImageButton? = null


    companion object {
        fun newInstance(bundle: Bundle? = null): FacebookAuthFragment {
            val fragment = FacebookAuthFragment()
            bundle?.let { fragment.arguments = it }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_facebook_auth, container, false)


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindViewModel()

        apiCallSignInWithFacebook()
        apiCallMergeFacebookAccount()
        apiCallSignUpWithFacebook()
        initApiCallAutofollowLimor()
        listeners()
        app = context?.applicationContext as App
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fbUid = arguments?.getString("fbUid").toString()
        fbToken = arguments?.getString("fbToken").toString()
        firstName = arguments?.getString("firstName").toString()
        lastName = arguments?.getString("lastName").toString()
        email = arguments?.getString("email").toString()
        userImageUrl = arguments?.getString("userImageUrl").toString()

        btnClose = activity?.findViewById(R.id.btnClose)
    }

    override fun onResume() {
        super.onResume()
        if(activity is SignActivity) {
            (activity as SignActivity).showToolbar()
        }
    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelSignInMail =
                ViewModelProvider(fragmentActivity, viewModelFactory)
                    .get(SignViewModel::class.java)

            viewModelSignInFB =
                ViewModelProvider(fragmentActivity, viewModelFactory)
                    .get(SignFBViewModel::class.java)

            viewModelMergeFacebookAccount =
                ViewModelProvider(fragmentActivity, viewModelFactory)
                    .get(MergeFacebookAccountViewModel::class.java)

            viewModelSignUpFB =
                ViewModelProvider(fragmentActivity, viewModelFactory)
                    .get(SignUpFBViewModel::class.java)

            viewModelCreateFriend =
                ViewModelProvider(fragmentActivity, viewModelFactory)
                    .get(CreateFriendViewModel::class.java)
        }
    }


    private fun listeners() {
        btnJoinLimor?.onClick {
            view?.hideKeyboard()
            try {
                if(usernameIsUnique){
                    tryLoginWithFacebook(edtUsername.myEdit.text.toString())
                }else{
                    alert(getString(R.string.you_must_type_avaible_username)) {
                        okButton { }
                    }.show()
                }
            } catch (e: IllegalArgumentException) {
                Timber.e(getString(R.string.cant_open))
            }
        }

        edtUsername.myEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                //println("el campo es:$s")
                if (s.isNotEmpty() && s.length > 3){
                    usernameExists(s.toString())
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        btnClose?.onClick {
            findNavController().navigateUp()
        }
    }




    //Region Methods for Facebook SignIn/SignUp
    private fun tryLoginWithFacebook(username: String) {

        val user = UISignUpUser(email, "", username)

        viewModelSignInFB.fbAccessTokenViewModel = fbToken
        viewModelSignInFB.fbUidViewModel = fbUid
        viewModelSignInFB.userViewModel = user
        viewModelSignInFB.emailViewModel = email

        signUpFBLoginTrigger.onNext(Unit)
    }
    private fun apiCallSignInWithFacebook() {
        val output = viewModelSignInFB.transform(
            SignFBViewModel.Input(
                signUpFBLoginTrigger
            )
        )

        output.response.observe(viewLifecycleOwner, Observer {
            pbSignIn?.visibility = View.GONE
            view?.hideKeyboard()

            var token: String = ""
            try {
                token = it.data.token.access_token
                viewModelSignInFB.tokenInApp = token
            } catch (e: Exception) {
                token = ""
                e.printStackTrace()
            }

            if (it.message == "Success") {
                goToMainActivity()
            } else {
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

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbSignIn?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {
                if (it.code == Constants.ERROR_CODE_FACEBOOK_USER_DOES_NOT_EXISTS) {
                    tryRegisterWithFacebook(
                        viewModelSignInFB.fbUidViewModel,
                        viewModelSignInFB.fbAccessTokenViewModel,
                        viewModelSignInFB.userViewModel
                    )
                } else {
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


    private fun tryRegisterWithFacebook(fbUid: String, fbToken: String, user: UISignUpUser) {
        viewModelSignUpFB.fbAccessTokenViewModel = fbToken
        viewModelSignUpFB.fbUidViewModel = fbUid
        viewModelSignUpFB.emailViewModel = user.email
        viewModelSignUpFB.passwordViewModel = user.password
        viewModelSignUpFB.userNameViewModel = user.username

        signUpFBTrigger.onNext(Unit)
    }
    private fun apiCallSignUpWithFacebook() {
        val output = viewModelSignUpFB.transform(
            SignUpFBViewModel.Input(
                signUpFBTrigger
            )
        )

        output.response.observe(viewLifecycleOwner, Observer {
            pbSignIn?.visibility = View.GONE
            view?.hideKeyboard()

            var token: String = ""
            try {
                token = it.data.access_token.token.access_token
            } catch (e: Exception) {
                token = ""
                e.printStackTrace()
            }


            if (it.message == "Success") {
                viewModelCreateFriend.idNewFriend = Constants.LIMOR_ACCOUNT_ID
                createFriendTrigger.onNext(Unit)
                goToMainActivity()
            } else {
                if (it.code == Constants.ERROR_CODE_FACEBOOK_USER_EXISTS) {
                    mergeAccounts(
                        viewModelSignUpFB.fbUidViewModel,
                        viewModelSignUpFB.fbAccessTokenViewModel,
                        token
                    )
                }
            }

        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
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

        output.response.observe(viewLifecycleOwner, Observer {
            pbSignIn?.visibility = View.GONE
            view?.hideKeyboard()
            if (it.message == "Success") {
                goToMainActivity()
            }
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
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


    private fun initApiCallAutofollowLimor() {
        val output = viewModelCreateFriend.transform(
            CreateFriendViewModel.Input(
                createFriendTrigger
            )
        )

        output.response.observe(viewLifecycleOwner, Observer {
            view?.hideKeyboard()
            goToMainActivity()
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()

            // TODO: maybe we should do some checks
            goToMainActivity()
        })
    }
    // end Region


    private fun goToMainActivity() {
        val mainIntent = Intent(context, MainActivity::class.java)
        startActivity(mainIntent)
        (activity as SignActivity).finish()
    }



    //Check if username exists previously on the system
    private fun usernameExists(username: String){

        doAsync {

            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(context)
            val url= BuildConfig.BASE_URL + "api/v1/users/username?username=$username"

            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    //var strResponse = "Response is: $response"
                    val response: UIUsernameResponse = Gson().fromJson(response, UIUsernameResponse::class.java)
                    println(response)
                    uiThread {
                        if(response.data.available){
                            usernameIsUnique = true
                            showCheck()
                        }else{
                            usernameIsUnique = false
                            hideCheck()
                        }
                    }
                    //{"code":0,"message":"Success","data":{"available":false}}
                },
                Response.ErrorListener {
                    println(it.localizedMessage.toString())
                    uiThread {
                        usernameIsUnique = false
                        hideCheck()
                    }
                }
            )

            // Add the request to the RequestQueue.
            queue.add(stringRequest)
        }



    }

    private fun showCheck(){
    val drawable = ResourcesCompat.getDrawable(resources, R.drawable.selected, null)
    drawable!!.setBounds(0, 0, 50, 50)

    DrawableCompat.setTint(drawable, ContextCompat.getColor(requireContext(), R.color.green500))
    edtUsername.myEdit.setCompoundDrawables(null, null, drawable, null)

    edtUsername?.myEditLyt?.isErrorEnabled = false
    edtUsername?.myEditLyt?.error = null
    }

    private fun hideCheck(){
    edtUsername.myEdit.setCompoundDrawables(null, null, null, null)

    edtUsername?.myEditLyt?.isErrorEnabled = true
    edtUsername?.myEditLyt?.error = getString(R.string.username_not_available)
    edtUsername?.requestFocus()
    edtUsername?.myEdit?.background = resources.getDrawable(R.drawable.edittext, null)
    }


}