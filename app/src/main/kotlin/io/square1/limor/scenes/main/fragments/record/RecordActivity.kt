package io.square1.limor.scenes.main.fragments.record


import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import javax.inject.Inject


class RecordActivity : BaseActivity(), HasSupportFragmentInjector{

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector

    private lateinit var navController: NavController

    companion object {
        val TAG: String = RecordActivity::class.java.simpleName
        fun newInstance() = RecordActivity()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        //Initialize Shared Preferences to store device firebase token
        //sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        //bindViewModel()
        setupNavigationController()

    }



    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_record)
    }


}