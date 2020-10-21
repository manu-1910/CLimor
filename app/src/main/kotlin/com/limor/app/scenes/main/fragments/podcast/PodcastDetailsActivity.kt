package com.limor.app.scenes.main.fragments.podcast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.uimodels.UIPodcast
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class PodcastDetailsActivity : BaseActivity(), HasSupportFragmentInjector {

    private var feedPosition: Int? = 0
    var startCommenting: Boolean? = false
    var commentWithParent : CommentWithParent? = null
    var uiPodcast : UIPodcast? = null


    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController

    companion object {
        val TAG: String = PodcastDetailsActivity::class.java.simpleName
        fun newInstance() = PodcastDetailsActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast_details)

        val bundle = intent?.extras
        uiPodcast = bundle?.get("podcast") as UIPodcast?
        feedPosition = bundle?.get("position") as Int?
        commentWithParent = bundle?.get("model") as CommentWithParent?
        startCommenting = bundle?.get("commenting") as Boolean?

        setupNavigationController()
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        resultIntent.putExtra("podcast", uiPodcast)
        resultIntent.putExtra("position", feedPosition)
        setResult(Activity.RESULT_OK, resultIntent)
        super.onBackPressed()
    }


    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_podcast_details)
    }

}