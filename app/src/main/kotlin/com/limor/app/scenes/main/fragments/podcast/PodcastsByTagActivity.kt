package com.limor.app.scenes.main.fragments.podcast

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import com.limor.app.R
import com.limor.app.common.BaseActivity
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.sdk23.listeners.onClick
import javax.inject.Inject


class PodcastsByTagActivity : BaseActivity(), HasSupportFragmentInjector {

    //var uiFeedItem : UIFeedItem? = null
    private var hashTag: String? = ""

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    private lateinit var navController: NavController

    companion object {
        val TAG: String = PodcastsByTagActivity::class.java.simpleName
        const val BUNDLE_KEY_HASHTAG = "hashTag"
        fun newInstance() = PodcastsByTagActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcasts_by_tag)

        val bundle = intent?.extras
        hashTag = bundle?.get(BUNDLE_KEY_HASHTAG) as String?

        setupNavigationController()
        configureToolbar()
    }


    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_podcasts_by_tag)
    }

    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = hashTag

        //Toolbar Left
        btnClose.onClick {
            finish()
        }

    }

}