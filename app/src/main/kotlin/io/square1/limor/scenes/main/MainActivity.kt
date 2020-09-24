package io.square1.limor.scenes.main


import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.main.fragments.*
import io.square1.limor.scenes.main.fragments.record.RecordActivity
import io.square1.limor.scenes.main.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notification_item.*
import kotlinx.android.synthetic.main.toolbar_default.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.toast
import java.lang.Exception
import javax.inject.Inject


class MainActivity : BaseActivity(), HasSupportFragmentInjector{

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var sessionManager : SessionManager

    private lateinit var profileViewModel : ProfileViewModel
    private val getProfileTrigger = PublishSubject.create<Unit>()
    private lateinit var navController: NavController
    var app: App? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = applicationContext as App

        //Initialize Firebase Instance
        FirebaseApp.initializeApp(this)

        bindViewModel()

        initApiCallGetProfile()

        setupNavigationController()

        // this is intended to download the data of the current user logged. It's necessary to have it
        // in some times of the code, so we download it everytime this activivty loads to have it updated
        // with all of his data
        getProfileTrigger.onNext(Unit)
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    private fun initApiCallGetProfile() {
        val output = profileViewModel.transform(
            ProfileViewModel.Input(
                getProfileTrigger
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
        profileViewModel = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileViewModel::class.java)
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
                    val hostFragment = supportFragmentManager.findFragmentById(R.id.navigation_host_fragment)
                    val currentFragment = hostFragment?.childFragmentManager?.fragments?.get(0)
                    if (currentFragment != null && currentFragment is UserFeedFragment  && currentFragment.isVisible) {
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
        toolbarProfile.visibility = View.GONE
        when (toolbarTitle) {
            getString(R.string.title_home) -> {
                //viewModel.unreadCountCentres = 0

                applyToolbarElevation(true)

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

                applyToolbarElevation(false)

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
            getString(R.string.title_record) -> {
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
                tvToolbarTitle?.text = toolbarTitle

                applyToolbarElevation(true)

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
                toolbarProfile.visibility = View.VISIBLE
                bottom_navigation_view?.visibility = View.VISIBLE
            }

        }
    }

    private fun applyToolbarElevation(apply: Boolean){

        try{
            val toolbar = window.decorView.findViewById<View>(android.R.id.content).
            rootView.findViewById(R.id.toolbar) as androidx.appcompat.widget.Toolbar

            if(apply){
                toolbar.elevation = 10F
            }else{
                toolbar.elevation = 0F
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }


}