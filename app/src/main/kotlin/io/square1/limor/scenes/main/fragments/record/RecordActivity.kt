package io.square1.limor.scenes.main.fragments.record


import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.scenes.main.fragments.NotificationsFragment
import javax.inject.Inject


class RecordActivity : BaseActivity(), HasSupportFragmentInjector{

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    private lateinit var navController: NavController
    var app: App? = null



    companion object {
        val TAG: String = RecordActivity::class.java.simpleName
        fun newInstance() = RecordActivity()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        app = applicationContext as App

        //Initialize Shared Preferences to store device firebase token
        //sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        //Initialize Firebase Instance
        //FirebaseApp.initializeApp(this)

        //bindViewModel()
        setupNavigationController()

    }









    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_record)

    }



    //private fun showHomeToolbar(toolbarTitle: String) {
    //    when (toolbarTitle) {
    //        getString(R.string.title_home) -> {
    //            //viewModel.unreadCountCentres = 0
    //            tvToolbarTitle?.text = toolbarTitle
//
    //            btnToolbarLeft?.visibility = View.GONE
    //            btnToolbarLeft?.onClick { navController.popBackStack() }
//
    //            /*badgeVisibility()
    //            badgeViewingVisibility()
    //            badgeLeadVisibility()
    //            badgeCentreVisibility()*/
//
    //            btnToolbarRight?.visibility = View.GONE
    //            btnToolbarRight?.text = ""
    //            btnToolbarRight?.onClick { toast("right button clicked") }
//
    //            bottom_navigation_view?.visibility = View.VISIBLE
    //        }
    //        getString(R.string.title_discover) -> {
    //            //viewModel.unreadCountLeads = 0
    //            tvToolbarTitle?.text = toolbarTitle
//
    //            btnToolbarLeft?.visibility = View.GONE
    //            btnToolbarLeft?.onClick { navController.popBackStack() }
//
    //            /*badgeVisibility()
    //            badgeViewingVisibility()
    //            badgeLeadVisibility()
    //            badgeCentreVisibility()*/
//
    //            btnToolbarRight?.visibility = View.GONE
    //            btnToolbarRight?.text = ""
    //            btnToolbarRight?.onClick { toast("right button clicked") }
//
    //            bottom_navigation_view?.visibility = View.VISIBLE
//
    //        }
    //        getString(R.string.title_record) -> {
    //            //viewModel.unreadCountViewings = 0
    //            tvToolbarTitle?.text = toolbarTitle
//
    //            btnToolbarLeft?.visibility = View.GONE
    //            btnToolbarLeft?.onClick { navController.popBackStack() }
//
    //            /*badgeVisibility()
    //            badgeViewingVisibility()
    //            badgeLeadVisibility()
    //            badgeCentreVisibility()*/
//
    //            btnToolbarRight?.visibility = View.GONE
    //            btnToolbarRight?.text = ""
    //            btnToolbarRight?.onClick { toast("right button clicked") }
//
    //            bottom_navigation_view?.visibility = View.VISIBLE
//
    //        }
    //        getString(R.string.title_notifications) -> {
    //            tvToolbarTitle?.text = toolbarTitle
//
    //            btnToolbarLeft?.visibility = View.GONE
    //            btnToolbarLeft?.onClick { navController.popBackStack() }
//
    //            /*badgeVisibility()
    //            badgeViewingVisibility()
    //            badgeLeadVisibility()
    //            badgeCentreVisibility()*/
//
    //            btnToolbarRight?.visibility = View.GONE
    //            btnToolbarRight?.text = ""
    //            btnToolbarRight?.onClick { toast("right button clicked") }
//
    //            bottom_navigation_view?.visibility = View.VISIBLE
    //        }
    //        getString(R.string.title_profile) -> {
//
    //            tvToolbarTitle?.text = toolbarTitle
//
    //            btnToolbarLeft?.visibility = View.GONE
    //            btnToolbarLeft?.onClick { navController.popBackStack() }
//
    //            /*badgeVisibility()
    //            badgeViewingVisibility()
    //            badgeLeadVisibility()
    //            badgeCentreVisibility()*/
//
    //            btnToolbarRight?.visibility = View.GONE
    //            btnToolbarRight?.text = ""
    //            btnToolbarRight?.onClick { toast("right button clicked") }
//
    //            bottom_navigation_view?.visibility = View.VISIBLE
//
    //        }
//
    //    }
    //}
//

}