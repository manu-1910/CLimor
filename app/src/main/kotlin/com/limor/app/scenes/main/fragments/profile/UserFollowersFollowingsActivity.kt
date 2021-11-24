package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.common.Constants
import com.limor.app.components.tabselector.TabSelectorView
import com.limor.app.databinding.ActivityFollowersAndFollowingBinding
import com.limor.app.scenes.main.fragments.settings.SettingsViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.user_profile_fragment.*
import javax.inject.Inject


class UserFollowersFollowingsActivity : BaseActivity(), HasSupportFragmentInjector {

    private lateinit var binding: ActivityFollowersAndFollowingBinding
    var rootView: View? = null
    var userId: Int? = null
    var userName: String? = ""
    private lateinit var selectedTab: Tab

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    private val viewModel: SettingsViewModel by viewModels { viewModelFactory }

    companion object {
        const val USER_ID_KEY = "user_id"
        const val USER_NAME_KEY = "user_name"
    }

    private val tabs by lazy {
        mapOf(
            Tab.FOLLOWERS to getString(R.string.followers),
            Tab.FOLLOWING to getString(R.string.following)
        )
    }

    var followersCount = 0
    var followingsCount = 0


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        binding = ActivityFollowersAndFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        configureToolbar()

        configureViewPager()

    }

    private fun initViews() {
        binding.searchBar.apply {
            setOnQueryTextListener(
                onQueryTextChange = {
                    performSearch(it)
                },
                onQueryTextSubmit = {
                    performSearch(it)
                },
                onQueryTextBlank = {
                    viewModel.removeSearchFollowersResults()
                    viewModel.removeSearchFollowingResults()
                }
            )
            // Automatically open keyboard
            requestFocusOnText()
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            viewModel.removeSearchFollowersResults()
            viewModel.removeSearchFollowingResults()
        } else if (query.length > 2) {
            viewModel.clearFollowers()
            viewModel.clearFollowing()
            viewModel.searchFollowers(query, 0)
            viewModel.searchFollowings(query, 0)
        }
    }

    private fun configureToolbar() {
        intent?.extras?.let {
            userName = it.getString(USER_NAME_KEY)
            userId = it.getInt(USER_ID_KEY)
        }
        binding.toolbar.tvToolbarTitle.text = userName
    }


    private fun configureViewPager() {
        val viewPager: ViewPager2 = binding.followViewPager
        val names = arrayOf(
            getString(R.string.followers_count, followersCount),
            getString(R.string.followings_count, followingsCount)
        )

        val adapter = object : FragmentStateAdapter(
            supportFragmentManager, lifecycle
        ) {


            override fun getItemCount(): Int {
                return names.size
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        viewModel.removeSearchFollowingResults()
                        UserFollowersFragmentNew.newInstance(userId)
                    }
                    else -> {
                        viewModel.removeSearchFollowersResults()
                        UserFollowings.newInstance(userId)
                    }
                }
                //return UserFollowings.newInstance(userId)


            }

        }
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
        viewPager.offscreenPageLimit = 2



        binding.tabSelectorView.apply {
            setOnTabSelectedListener { tabName, position ->
                binding.followViewPager.currentItem = position
                performSearch(binding.searchBar.getCurrentSearchQuery())
            }
            setMode(TabSelectorView.Mode.FIXED)
            setTabs(tabs.values.toList())
        }
        binding.tabSelectorView.setOnTabSelectedListener { tabName, position ->
            binding.followViewPager.currentItem = position
            if (position == 0) {
                selectedTab = Tab.FOLLOWERS
            } else {
                selectedTab = Tab.FOLLOWING
            }
        }


        val bundle = intent?.extras
        bundle?.let {
            when (bundle.getString(Constants.TAB_KEY)) {
                Constants.TAB_FOLLOWERS -> {
                    tabSelectorView.selectTabAt(0)
                    viewPager.currentItem = 0
                }
                Constants.TAB_FOLLOWINGS -> {
                    tabSelectorView.selectTabAt(1)
                    viewPager.currentItem = 1
                }

            }
        }


        binding.toolbar.btnClose.setOnClickListener {
            finish()
        }


    }

    enum class Tab {
        FOLLOWERS, FOLLOWING
    }

}
