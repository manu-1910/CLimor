package io.square1.limor.scenes.main


import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.main.fragments.*
import io.square1.limor.scenes.main.fragments.record.RecordActivity
import io.square1.limor.scenes.main.viewmodels.GetUserViewModel
import io.square1.limor.scenes.notifications.PushNotificationsViewModel
import io.square1.limor.uimodels.UIUserDeviceData
import io.square1.limor.uimodels.UIUserDeviceRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_default.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.toast
import javax.inject.Inject
import android.provider.Settings.Secure


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = applicationContext as App

        //Initialize Shared Preferences to store device firebase token
        sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        //Initialize Firebase Instance
        FirebaseApp.initializeApp(this)

        bindViewModel()

        initApiCallGetUser()

        setupNavigationController()

        apiCallPostUserDevice()

        getFirebaseInstance()

        // this is intended to download the data of the current user logged. It's necessary to have it
        // in some times of the code, so we download it everytime this activivty loads to have it updated
        // with all of his data
        getUserDataTrigger.onNext(Unit)
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
            Toast.makeText(
                this,
                getString(R.string.error_initializing_data),
                Toast.LENGTH_SHORT
            ).show()
        })
    }


    private fun bindViewModel() {
        getUserViewModel = ViewModelProviders
            .of(this, viewModelFactory)
            .get(GetUserViewModel::class.java)

        pushNotificationsViewModel = ViewModelProviders
            .of(this, viewModelFactory)
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
                    val currentFragment = hostFragment?.childFragmentManager?.fragments?.get(0)
                    if (currentFragment != null && currentFragment is UserFeedFragment && currentFragment.isVisible) {
                        currentFragment.scrollToTop()
                    }
                }
            }
        }

        navController.addOnNavigatedListener { _, _ ->
            when (navController.currentDestination.label) {
                HomeFragment.TAG -> {
                    showHomeToolbar(getString(R.string.title_home))
                }
                UserFeedFragment.TAG -> {
                    showHomeToolbar(getString(R.string.title_home))
                }
                DiscoverFragment.TAG -> {
                    showHomeToolbar(getString(R.string.title_discover))
                }
                RecordActivity.TAG -> {
                    showHomeToolbar(getString(R.string.title_record))
                }
                NotificationsFragment.TAG -> {
                    showHomeToolbar(getString(R.string.title_notifications))
                }
                ProfileFragment.TAG -> {
                    showHomeToolbar(getString(R.string.title_profile))
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


    private fun showHomeToolbar(toolbarTitle: String) {
        btnClose?.visibility = View.GONE
        toolbar_main.visibility = View.VISIBLE
        toolbarProfile.visibility = View.GONE
        when (toolbarTitle) {
            getString(R.string.title_home) -> {
                //viewModel.unreadCountCentres = 0
                hideMainToolbar(false)
                toolbarDiscover.visibility = View.GONE
                toolbarProfile.visibility = View.GONE

                tvToolbarTitle?.text = toolbarTitle

                btnToolbarLeft?.visibility = View.GONE
                btnToolbarLeft?.onClick { navController.popBackStack() }

                /*badgeVisibility()
                badgeViewingVisibility()
                badgeLeadVisibility()
                badgeCentreVisibility()*/

                btnToolbarRight?.visibility = View.GONE
                btnToolbarRight?.text = ""
                btnToolbarRight?.onClick { toast("right button clicked") }

                bottom_navigation_view?.visibility = View.VISIBLE
            }
            getString(R.string.title_discover) -> {
                //viewModel.unreadCountLeads = 0

                applyDiscoverToolbarElevation(false)

                toolbarDiscover.visibility = View.VISIBLE
                toolbarProfile.visibility = View.GONE
                hideMainToolbar(true)
                toolbarDiscover.findViewById<TextView>(R.id.title).text =
                    getString(R.string.discover)

                bottom_navigation_view?.visibility = View.VISIBLE

            }
            getString(R.string.title_record) -> {

                toolbarDiscover.visibility = View.GONE
                hideMainToolbar(false)
                //viewModel.unreadCountViewings = 0
                tvToolbarTitle?.text = toolbarTitle

                btnToolbarLeft?.visibility = View.GONE
                btnToolbarLeft?.onClick { navController.popBackStack() }

                /*badgeVisibility()
                badgeViewingVisibility()
                badgeLeadVisibility()
                badgeCentreVisibility()*/

                btnToolbarRight?.visibility = View.GONE
                btnToolbarRight?.text = ""
                btnToolbarRight?.onClick { toast("right button clicked") }

                bottom_navigation_view?.visibility = View.VISIBLE

            }
            getString(R.string.title_notifications) -> {
                toolbarDiscover.visibility = View.VISIBLE
                toolbarProfile.visibility = View.GONE
                hideMainToolbar(true)
                toolbarDiscover.findViewById<TextView>(R.id.title).text =
                    getString(R.string.title_notifications)

                tvToolbarTitle?.text = toolbarTitle

                btnToolbarLeft?.visibility = View.GONE
                btnToolbarLeft?.onClick { navController.popBackStack() }

                /*badgeVisibility()
                badgeViewingVisibility()
                badgeLeadVisibility()
                badgeCentreVisibility()*/

                btnToolbarRight?.visibility = View.GONE
                btnToolbarRight?.text = ""
                btnToolbarRight?.onClick { toast("right button clicked") }

                bottom_navigation_view?.visibility = View.VISIBLE
            }
            getString(R.string.title_profile) -> {
                toolbarDiscover.visibility = View.GONE
                toolbarProfile.visibility = View.VISIBLE
                toolbar_main.visibility = View.INVISIBLE
                btnToolbarLeft?.visibility = View.GONE
                bottom_navigation_view?.visibility = View.VISIBLE
            }

        }
    }

    private fun applyDiscoverToolbarElevation(apply: Boolean){

        try{
            val toolbar = window.decorView.findViewById<View>(android.R.id.content).
            rootView.findViewById(R.id.toolbarDiscover) as androidx.appcompat.widget.Toolbar

            if(apply){
                toolbar.elevation = 10F
            }else{
                toolbar.elevation = 0F
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun hideMainToolbar(hide: Boolean){
        val toolbar = window.decorView.findViewById<View>(android.R.id.content).
        rootView.findViewById(R.id.toolbar_main) as androidx.appcompat.widget.Toolbar
        if(hide){
            toolbar.visibility = View.GONE
        }else{
            toolbar.visibility = View.VISIBLE
        }
    }

    fun getToolbarHeight() : Int{
        val toolbar = window.decorView.findViewById<View>(android.R.id.content).
        rootView.findViewById(R.id.toolbar_main) as androidx.appcompat.widget.Toolbar
        return toolbar.measuredHeight
    }

    fun getToolBar(): androidx.appcompat.widget.Toolbar{
        return window.decorView.findViewById<View>(android.R.id.content).
        rootView.findViewById(R.id.toolbar_main) as androidx.appcompat.widget.Toolbar
    }

    private fun getFirebaseInstance() {
        FirebaseInstanceId.getInstance().instanceId
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
            toast(getString(R.string.error_sending_push_to_server))
        })
    }


}