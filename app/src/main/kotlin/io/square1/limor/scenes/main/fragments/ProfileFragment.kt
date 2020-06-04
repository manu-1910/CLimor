package io.square1.limor.scenes.main.fragments

import android.content.Intent
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.common.SessionManager
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.main.viewmodels.LogoutViewModel
import io.square1.limor.scenes.main.viewmodels.ProfileViewModel
import io.square1.limor.scenes.splash.SplashActivity
import io.square1.limor.scenes.utils.CommonsKt.Companion.toEditable
import io.square1.limor.uimodels.UIUser
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import javax.inject.Inject


class ProfileFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var viewModelProfile: ProfileViewModel
    private lateinit var viewModelLogout: LogoutViewModel
    private val getUserDataTrigger = PublishSubject.create<Unit>()
    private val logoutTrigger = PublishSubject.create<Unit>()

    var app: App? = null


    companion object {
        val TAG: String = ProfileFragment::class.java.simpleName
        fun newInstance() = ProfileFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = context?.applicationContext as App

        listeners()
        bindViewModel()
        apiCallGetUser()
        apiCallLogout()

        getUserDataTrigger.onNext(Unit)
    }


    private fun listeners() {
        btnLogout.onClick {
            logoutTrigger.onNext(Unit)
            logOutFromFacebook()
        }
    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelProfile = ViewModelProviders
                 .of(fragmentActivity, viewModelFactory)
                 .get(ProfileViewModel::class.java)

            viewModelLogout = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(LogoutViewModel::class.java)
         }
    }


    private fun apiCallGetUser() {
        val output = viewModelProfile.transform(
            ProfileViewModel.Input(
                getUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()
            printUserData(it.data.user)
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
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



    private fun apiCallLogout() {
        val output = viewModelLogout.transform(
            LogoutViewModel.Input(
                logoutTrigger
            )
        )

        output.response.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()

            //if (it.message == "Success") { //TODO review why message is errormessage and is null
            if (it.code == 0) {

                sessionManager.logOut()

                val mainIntent = Intent(context, SplashActivity::class.java)
                startActivity(mainIntent)
                try {
                    activity?.finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
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


    private fun logOutFromFacebook(){ //TODO
        // Logout
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, GraphRequest.Callback {
                AccessToken.setCurrentAccessToken(null)
                LoginManager.getInstance().logOut()
            }).executeAsync()
        }
    }


    private fun printUserData(user: UIUser) {
        etFullName.text = (user.first_name + " " + user.last_name).toEditable()
        etUsername.text = user.username?.toEditable()
        etEmail.text = user.email?.toEditable()
        etPhoneNumber.text = user.phone_number?.toEditable()
    }

}
