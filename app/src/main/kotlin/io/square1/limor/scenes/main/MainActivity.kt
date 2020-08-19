package io.square1.limor.scenes.main


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.scenes.main.fragments.*
import io.square1.limor.scenes.main.fragments.record.RecordActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.toast
import javax.inject.Inject


class MainActivity : BaseActivity(), HasSupportFragmentInjector{

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    private lateinit var navController: NavController
    var app: App? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = applicationContext as App

        //Initialize Firebase Instance
        FirebaseApp.initializeApp(this)

        //bindViewModel()
        setupNavigationController()
    }




    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment)
        bottom_navigation_view.setupWithNavController(navController)

        bottom_navigation_view.setOnNavigationItemReselectedListener {
            //Used to don't load fragment again
        }

        navController.addOnNavigatedListener { _, _ ->
            when (navController.currentDestination.label) {
                HomeFragment.TAG -> {
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
        when (toolbarTitle) {
            getString(R.string.title_home) -> {
                //viewModel.unreadCountCentres = 0
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

        }
    }


}