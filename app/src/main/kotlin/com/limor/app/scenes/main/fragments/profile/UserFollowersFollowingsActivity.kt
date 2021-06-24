package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.databinding.ActivityFollowersAndFollowingBinding
import com.limor.app.uimodels.UIUser
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class UserFollowersFollowingsActivity : BaseActivity(), HasSupportFragmentInjector {

    private lateinit var binding: ActivityFollowersAndFollowingBinding
    var rootView: View? = null
    private var uiUser: UIUser? = null
    private var tabToShow: String = ""

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>


    companion object {
        val TAG: String = UserFollowersFollowingsActivity::class.java.simpleName
        fun newInstance() = UserFollowersFollowingsActivity()
    }

    var followersCount = 0;
    var followingsCount = 0;


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_followers_and_following) as ActivityFollowersAndFollowingBinding


        val bundle = intent?.extras
        uiUser = bundle?.get("user") as UIUser
        tabToShow = bundle?.getString("tabToShow").toString()

        configureToolbar()

        configureViewPager()

    }


    private fun configureToolbar(){
        //Toolbar title
       /* if(uiUser!=null){
            if (uiUser!!.username.isNullOrEmpty()){
                binding.top.tvToolbarTitle.text = getString(R.string.username)
            }else{
                binding.top.tvToolbarTitle.text = "user_name"
            }
        }
        binding.top.tvToolbarTitle.text = "user_name"

        //Toolbar Left
        binding.top.btnClose.onClick {
            this.finish()
        }*/

        /*//Search View
        search_view.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty() && newText.length > 3) {
                    //searchLocations(newText)
                    println(newText)
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty() && query.length > 3) {
                    //searchLocations(query)
                    println(query)
                } else {
                    toast(getString(R.string.min_3_chars))
                }
                return false
            }
        })*/
    }


    private fun configureViewPager(){
        val viewPager: ViewPager = findViewById(R.id.view_pager_followers_followings)
        val tabs: TabLayout = findViewById(R.id.tabs)
        val names = arrayOf(
            getString(R.string.followers_count, uiUser?.followers_count),
            getString(R.string.followings_count, uiUser?.following_count)
        )

        val adapter = object : FragmentStatePagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getItem(position: Int): Fragment {
                if(position == 0){
                    return UserFollowersFragment.newInstance(uiUser!!)
                }else{
                    return UserFollowingsFragment.newInstance(uiUser!!)
                }
            }

            override fun getCount() : Int {
                return names.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return names[position]
            }

            // this is necessary. Without this, app will crash when you are in a different fragment
            // and then push back and it goes back to this fragment.
            // the fragmentstatepageradapter saves states between different fragments of the adapter itself
            // but if you go to a different fragment, for example home, and the push back and the navigation
            // goes back to this profile fragment, the fragmentstatepageradapter will try to restore the
            // state of the adapter fragments but they are not alive anymore.
            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
                try {
                    super.restoreState(state, loader)
                } catch (e: Exception) {
//                        Timber.e("Error Restore State of Fragment : %s", e.message)
                }
            }
        }
        viewPager.adapter = adapter

        tabs.setupWithViewPager(viewPager)
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val myFragment = adapter.instantiateItem(viewPager, it.position)
//                            if (myFragment is UserFollowersFragment) {
//                                myFragment.scrollToTop()
//                            }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {

            }
        })

        if (!tabToShow.isNullOrEmpty()){
            if(tabToShow.trim().equals("followers")){
                val tab: TabLayout.Tab? = tabs.getTabAt(0)
                tab?.select()
            }else{
                val tab: TabLayout.Tab? = tabs.getTabAt(1)
                tab?.select()
            }
        }

    }


}
