package com.limor.app.scenes.main.fragments.settings

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

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
import com.limor.app.databinding.*
import com.limor.app.dm.ChatManager
import com.limor.app.extensions.dp
import com.limor.app.extensions.hideKeyboard
import com.limor.app.extensions.px
import com.limor.app.scenes.auth_new.firebase.FirebaseSessionHandler
import com.limor.app.scenes.auth_new.fragments.FragmentLanguages
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.settings.EditProfileFragment.Companion.TIMBER_TAG
import com.limor.app.scenes.main.viewmodels.LogoutViewModel
import com.limor.app.scenes.main.viewmodels.UpdateUserViewModel
import com.limor.app.scenes.main_new.fragments.FragmentDeleteAccountPhoneNumberInput
import com.limor.app.scenes.splash.SplashActivity
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.CommonsKt.Companion.handleOnApiError
import com.limor.app.uimodels.UIUser
import com.limor.app.uimodels.UserUIModel
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.dialog_cancel_draft.view.*
import kotlinx.android.synthetic.main.dialog_save_draft.view.*
import kotlinx.android.synthetic.main.dialog_save_draft.view.saveButton
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import kotlinx.coroutines.*
import org.jetbrains.anko.sdk23.listeners.onCheckedChange
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import javax.inject.Inject


class SettingsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: SettingsViewModel by viewModels({ activity as SettingsActivity }) { viewModelFactory }

    @Inject
    lateinit var chatManager: ChatManager

    private var rootView: View? = null
    private var currentUser: UserUIModel? = null
    private lateinit var binding: FragmentSettingsBinding

    private var coroutineJob: Job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + coroutineJob)

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName
        fun newInstance() = SettingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        rootView = binding.root
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading()

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        configureToolbar()
        apiCallLogout()
        apiCallUpdateUser()

        //Set the version name of the app into textview
        tvAppVersion.text = "v" + BuildConfig.VERSION_NAME


        model.userInfoLiveData.observe(viewLifecycleOwner, Observer { user ->
            currentUser = user
            if (currentUser == null) {
                // TODO show reload UI/button
            } else {
                setupUserRelated()
                listeners()
                hideLoading()
            }
        })

        model.getUserInfo()
    }

    private fun setupUserRelated() {
        val user = currentUser ?: return
        binding.swPushNotifications.isChecked = user.hasNotificationsEnabled()
        binding.swSounds.isChecked = PrefsHandler.areSoundsEnabled(requireContext())
        if(user.isPatron == true){
            binding.lytDeleteAccount.visibility = View.GONE
        }
    }

    private fun hideLoading() {
        binding.loading.visibility = View.GONE
    }

    private fun showLoading() {
        binding.loading.visibility = View.VISIBLE
    }

    private fun showWebPage(urlResId: Int, titleResId: Int) {
        try {
            findNavController().navigate(
                R.id.action_settings_fragment_to_webview_fragment,
                Bundle().apply {
                    putString(getString(R.string.webViewKey), getString(urlResId))
                    putString(WebViewFragment.KEY_TITLE, getString(titleResId))
                }
            )
        } catch (e: IllegalArgumentException) {
            Timber.e(getString(R.string.cant_open))
        }
    }

    private fun listeners() {

        lytEditProfile.onClick {
            findNavController().navigate(R.id.action_settings_fragment_to_edit_profile_fragment)
        }

        lytPrivacyPolicy.onClick {
            showWebPage(R.string.privacy_url, R.string.privacy_policy_title)
        }

        lytTermsAndConditions.onClick {
            showWebPage(R.string.terms_url, R.string.tos_title)
        }

        swPushNotifications?.onClick {
            val currentStatus = swPushNotifications.isChecked
            model.setNotificationsEnabled(currentStatus)
            // callToUpdateUser(userItem)
        }

        binding.swSounds.onCheckedChange { _, isChecked ->
            PrefsHandler.setSoundsEnabled(requireContext(), isChecked)
        }

        lytReportProblem.onClick {
            try {
                val emailIntent = Intent(
                    Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", getString(R.string.support_email), null)
                )
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.request_from))
                startActivity(emailIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lytBlockedUsers.onClick {
            findNavController().navigate(R.id.action_settings_fragment_to_users_blocked_fragment)
        }

        lytLogout.onClick {
            showLogoutWarning()
        }

        lytDeleteAccount.onClick {
            showDeleteAccountWarning()
        }

    }

    private fun logout() {
        lifecycleScope.launch {
            scope.launch {
                App.instance.chatManager.logout()
                model.deleteUserDevice()
                OneSignal.removeExternalUserId()
            }
            try {
                FirebaseSessionHandler.logout(requireContext())
                Toast.makeText(requireContext(), "Done!", Toast.LENGTH_LONG).show()
                PrefsHandler.setCanShowCategorySelection(requireContext(), false)
                PrefsHandler.setPreferencesSelected(requireContext(), false)
                PrefsHandler.setPreferencesScreenOpenedInThisSession(requireContext(), false)
                PrefsHandler.setCanShowGenderSelection(requireContext(), false)
                (activity)?.finishAffinity()
                startActivity(
                    Intent(requireContext(), SplashActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error -> ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getDialogButton(labelResId: Int, mainAction: Boolean = false): Button {
        val resId = if (mainAction) R.layout.dialog_button_main else R.layout.dialog_button_normal
        return (layoutInflater.inflate(resId, null) as Button).apply {
            setText(labelResId)
            layoutParams = LinearLayout.LayoutParams(0, 48.px).apply {
                weight = 1.0f
            }
        }
    }

    private fun showLogoutWarning() {

        val dialogView = DialogGenericAlertBinding.inflate(layoutInflater).apply {
            // TODO get icon?
            //  headerIcon.setImageResource(R.drawable.ic_delete_cast)

            textTitle.setText(R.string.logout)
            textMessage.setText(R.string.logout_warning_message)
        }

        val dialogBuilder = AlertDialog.Builder(context).apply {
            setView(dialogView.root)
            setCancelable(true)
        }

        val dialog: AlertDialog = dialogBuilder.create()

        getDialogButton(R.string.btn_cancel, true).also {
            dialogView.buttons.addView(it)
        }.onClick {
            dialog.dismiss()
        }

        getDialogButton(R.string.btn_yes).also {
            dialogView.buttons.addView(it)
        }.onClick {
            dialog.dismiss()
            logout()
        }

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset)
            show()
        }
    }

    private fun showDeleteAccountWarning(){
        findNavController().navigate(R.id.action_fragment_settings_to_fragment_delete_account)
    }


    private fun configureToolbar() {
        model.setToolbarTitle(resources.getString(R.string.settings))
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

