package com.limor.app.scenes.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings.Secure
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.installations.FirebaseInstallations
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.common.SessionManager
import com.limor.app.scenes.main.viewmodels.GetUserViewModel
import com.limor.app.scenes.notifications.PushNotificationsViewModel
import com.limor.app.scenes.notifications.UtilsRegistrationIntentService
import com.limor.app.uimodels.UIUserDeviceData
import com.limor.app.uimodels.UIUserDeviceRequest
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_default.*
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import javax.inject.Inject

class MainActivity : BaseActivity(), HasSupportFragmentInjector{

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var sessionManager : SessionManager

    private lateinit var getUserViewModel : GetUserViewModel
    private val getUserDataTrigger = PublishSubject.create<Unit>()
    private lateinit var navController: NavController
    var app: App? = null

    private val PREFS_NAME = "limorv2pref"
    private val PUSH_NEW_KEY = "pushnewtoken"
    private val postUserDeviceTrigger = PublishSubject.create<Unit>()
    private lateinit var pushNotificationsViewModel: PushNotificationsViewModel
    lateinit var sharedPref: SharedPreferences

    companion object {
        const val REQUEST_AUDIO_PLAYER = 45
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = applicationContext as App

        //Initialize Shared Preferences to store device firebase token
        sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        bindViewModel()

        initApiCallGetUser()

        setupNavigationController()

        apiCallPostUserDevice()

        getFirebaseInstance()

        setupPushNotifications()

        // this is intended to download the data of the current user logged. It's necessary to have it
        // in some times of the code, so we download it everytime this activivty loads to have it updated
        // with all of his data
        getUserDataTrigger.onNext(Unit)

        val extras = intent.extras
        if(extras != null && !extras.isEmpty) {
            val destination = extras.getString("destination")
            destination?.let {
                when(destination) {
                    "profile" -> navController.navigate(R.id.navigation_profile)
                    "notifications" -> navController.navigate(R.id.navigation_notifications)
                }
            }
        }
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    private fun initApiCallGetUser() {
        val output = getUserViewModel.transform(
            GetUserViewModel.Input(
                getUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            sessionManager.storeUser(it.data.user)
        })

        output.errorMessage.observe(this, Observer {
//            Toast.makeText(
//                this,
//                getString(R.string.error_initializing_data),
//                Toast.LENGTH_SHORT
//            ).show()
        })
    }


    private fun bindViewModel() {
        getUserViewModel = ViewModelProvider(this, viewModelFactory)
            .get(GetUserViewModel::class.java)

        pushNotificationsViewModel = ViewModelProvider(this, viewModelFactory)
            .get(PushNotificationsViewModel::class.java)
    }


    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment)
        bottom_navigation_view.setupWithNavController(navController)


        //Used to don't load fragment twice
        bottom_navigation_view.setOnNavigationItemReselectedListener {

            // this is to know when the user clicks again in an already loaded fragment
            when (it.itemId) {

                // in this case, we'll scroll to the top again
                R.id.navigation_home -> {
                    val hostFragment =
                        supportFragmentManager.findFragmentById(R.id.navigation_host_fragment)
                }
            }
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_discover -> {
                    navController.navigate(R.id.navigation_discover)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_record -> {
                    stopAudioService()
                    navController.navigate(R.id.navigation_record)
                    return@setOnNavigationItemSelectedListener false
                }
                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.navigation_profile)
                    return@setOnNavigationItemSelectedListener true
                }
                else ->
                    return@setOnNavigationItemSelectedListener true
            }
        }
    }

    private fun getFirebaseInstance() {
        FirebaseInstallations.getInstance().getToken(true)
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    //Timber.e("getInstanceId failed %s", task.exception)
                    return@OnCompleteListener
                } else {
                    // Get new Instance ID token
                    val newToken = sharedPref.getString(PUSH_NEW_KEY, "")
                    if (!newToken.isNullOrEmpty()) {
                        //Send new token to API
                        sessionManager.storePushToken(newToken)

                        val androidId = Secure.getString(this.contentResolver, Secure.ANDROID_ID);

                        //val uuid = sessionManager.getStoredUser()?.id
                        val device = UIUserDeviceData(
                            androidId.toString(),
                            "android",
                            newToken
                        )
                        val request = UIUserDeviceRequest(device)

                        pushNotificationsViewModel.uiUserDeviceRequest = request
                        postUserDeviceTrigger.onNext(Unit)
                    }
                }
            })
    }

    private fun apiCallPostUserDevice() {
        val output = pushNotificationsViewModel.transform(
            PushNotificationsViewModel.Input(
                postUserDeviceTrigger
            )
        )

        output.response.observe(this, Observer {
            /*if (it.status == getString(R.string.ok)) {
                /*val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.putString(PUSH_NEW_KEY, "")
                editor.apply()*/
            } else {
               // toast(R.string.some_error)
            }*/
            if (it.message.trim() == "Success") {
                println("Update push notification token Success")
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
//            toast(getString(R.string.error_sending_push_to_server))
        })
    }


    private fun setupPushNotifications() {
        val intent = Intent(this, UtilsRegistrationIntentService::class.java)
        startService(intent)
    }
}
