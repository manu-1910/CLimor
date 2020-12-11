package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil.setContentView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.fragments.UserFeedFragment

class UserFollowersFollowingsFragment: BaseFragment(){


    var rootView: View? = null


    companion object {
        val TAG: String = UserFollowersFollowingsFragment::class.java.simpleName
        fun newInstance() = UserFollowersFollowingsFragment()
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_followers_followings, container, false)
//            rvFeed = rootView?.findViewById(R.id.rvFeed)
//            swipeRefreshLayout = rootView?.findViewById(R.id.swipeRefreshLayout)
//            requestNewData()
        }
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(context!!, fragmentManager!!)
        val viewPager: ViewPager = rootView!!.findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = rootView!!.findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = rootView!!.findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}