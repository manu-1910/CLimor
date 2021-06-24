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

import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.limor.app.App
import com.limor.app.BuildConfig
import com.limor.app.R
import io.reactivex.subjects.PublishSubject
import com.limor.app.common.BaseFragment
import com.limor.app.common.SessionManager
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.fragments.settings.EditProfileFragment.Companion.TIMBER_TAG
import com.limor.app.scenes.main.viewmodels.LogoutViewModel
import com.limor.app.scenes.main.viewmodels.UpdateUserViewModel
import com.limor.app.scenes.splash.SplashActivity
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.CommonsKt.Companion.handleOnApiError
import com.limor.app.uimodels.UIUser
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

//        swPushNotifications.isChecked = sessionManager.getStoredUser()!!.notifications_enabled
    }


    private fun listeners(){

        lytEditProfile.onClick {
            findNavController().navigate(R.id.action_settings_fragment_to_edit_profile_fragment)
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


        swPushNotifications?.onClick {
            val currentStatus = swPushNotifications.isChecked
           // callToUpdateUser(userItem)
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
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(),SplashActivity::class.java))
        }

    }


    private fun configureToolbar() {

    }


    private fun bindViewModel() {

    }


    private fun apiCallLogout() {

    }


    private fun logOutFromFacebook() {
        // Logout

    }


    private fun callToUpdateUser(userItem: UIUser) {

    }


    private fun apiCallUpdateUser() {

    }

}

