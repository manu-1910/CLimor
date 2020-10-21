package com.limor.app.scenes.main.fragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.limor.app.App
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.SessionManager
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.viewmodels.LogoutViewModel
import com.limor.app.scenes.main.viewmodels.UpdateUserViewModel
import com.limor.app.scenes.splash.SplashActivity
import com.limor.app.scenes.utils.CommonsKt.Companion.handleOnApiError
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import javax.inject.Inject


class SettingsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var viewModelLogout: LogoutViewModel
    private lateinit var updateUserViewModel: UpdateUserViewModel
    private val logoutTrigger = PublishSubject.create<Unit>()
    private val updateUserTrigger = PublishSubject.create<Unit>()
    private var rootView: View? = null
    var app: App? = null


    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName
        fun newInstance() = SettingsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        }
        app = context?.applicationContext as App
        return rootView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        listeners()
        configureToolbar()
        apiCallLogout()
        apiCallUpdateUser()

        //Set the version name of the app into textview
        tvAppVersion.text = "v" + BuildConfig.VERSION_NAME

        swPushNotifications.isChecked = sessionManager.getStoredUser()!!.notifications_enabled
    }


    private fun listeners(){

        lytEditProfile.onClick {
            findNavController().navigate(R.id.action_settings_fragment_to_edit_profile_fragment)
        }


        lytChangePassword.onClick {
            findNavController().navigate(R.id.action_settings_fragment_to_change_password_fragment)
        }


        lytPrivacyPolicy.onClick {
            try {
                //mainViewModel.isFromPrivacyFlag = true
                val bundle = Bundle()
                bundle.putString(getString(R.string.webViewKey), getString(R.string.privacy_url))
                findNavController().navigate(
                    R.id.action_settings_fragment_to_webview_fragment,
                    bundle
                )
            } catch (e: IllegalArgumentException) {
                // User tried tapping!
                Timber.e(getString(R.string.cant_open))
            }
        }


        lytTermsAndConditions.onClick {
            try {
                //mainViewModel.isFromPrivacyFlag = true
                val bundle = Bundle()
                bundle.putString(getString(R.string.webViewKey), getString(R.string.terms_url))
                findNavController().navigate(
                    R.id.action_settings_fragment_to_webview_fragment,
                    bundle
                )
            } catch (e: IllegalArgumentException) {
                // User tried tapping!
                Timber.e(getString(R.string.cant_open))
            }
        }


        swPushNotifications?.setOnCheckedChangeListener { _, isChecked ->

            val userItem = sessionManager.getStoredUser()
            if (userItem != null) {
                userItem.notifications_enabled = isChecked
                sessionManager.storeUser(userItem)
            }

            callToUpdateUser()
        }


        lytReportProblem.onClick {
            try {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.support_email), null))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.request_from) + " " + sessionManager.getStoredUser()?.username)
                startActivity(emailIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        lytBlockedUsers.onClick {
            findNavController().navigate(R.id.action_settings_fragment_to_users_blocked_fragment)
        }


        lytLogout.onClick {
            logoutTrigger.onNext(Unit)
            logOutFromFacebook()
        }

    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_settings)

        //Toolbar Left
        btnClose.onClick {
            activity?.finish()
        }
    }


    private fun bindViewModel() {
        activity?.let {
            viewModelLogout = ViewModelProviders
                .of(it, viewModelFactory)
                .get(LogoutViewModel::class.java)

            updateUserViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(UpdateUserViewModel::class.java)
        }
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
            handleOnApiError(App.instance, context!!, this, it)
        })
    }


    private fun logOutFromFacebook() {
        // Logout
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE,
                GraphRequest.Callback {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()
                }).executeAsync()
        }
    }


    private fun callToUpdateUser(){

        updateUserViewModel.first_name = sessionManager.getStoredUser()!!.first_name.toString()
        updateUserViewModel.last_name = sessionManager.getStoredUser()!!.last_name.toString()
        updateUserViewModel.username = sessionManager.getStoredUser()!!.username.toString()
        updateUserViewModel.website = sessionManager.getStoredUser()!!.website.toString()
        updateUserViewModel.description = sessionManager.getStoredUser()!!.description.toString()
        updateUserViewModel.email = sessionManager.getStoredUser()!!.email.toString()
        updateUserViewModel.phone_number = sessionManager.getStoredUser()!!.phone_number.toString()
        updateUserViewModel.date_of_birth = sessionManager.getStoredUser()!!.date_of_birth!!
        updateUserViewModel.gender = sessionManager.getStoredUser()!!.gender.toString()
        updateUserViewModel.image = sessionManager.getStoredUser()!!.images.small_url
        updateUserViewModel.notifications_enabled = sessionManager.getStoredUser()!!.notifications_enabled

        updateUserTrigger.onNext(Unit)
    }


    private fun apiCallUpdateUser() {
        val output = updateUserViewModel.transform(
            UpdateUserViewModel.Input(
                updateUserTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code == 0) {
                sessionManager.storeUser(it.data.user)
                println("Profile updated successfully")
                swPushNotifications.isChecked = it.data.user.notifications_enabled
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            handleOnApiError(App.instance, context!!, this, it)
        })
    }

}

