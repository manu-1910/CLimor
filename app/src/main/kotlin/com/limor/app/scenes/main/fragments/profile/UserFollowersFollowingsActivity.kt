package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.common.Constants
import com.limor.app.databinding.ActivityFollowersAndFollowingBinding
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import timber.log.Timber
import javax.inject.Inject


class UserFollowersFollowingsActivity : BaseActivity(), HasSupportFragmentInjector {

    private lateinit var binding: ActivityFollowersAndFollowingBinding
    var rootView: View? = null
    var userId: Int? = 0
    var userName: String? = ""

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>


    companion object {
        val TAG: String = UserFollowersFollowingsActivity::class.java.simpleName
        fun newInstance() = UserFollowersFollowingsActivity()
    }

    var followersCount = 0
    var followingsCount = 0


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        binding = ActivityFollowersAndFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Timber.d("USER FOLLOW created FF")

        configureToolbar()

        configureViewPager()

    }


    private fun configureToolbar() {
        intent?.extras?.let{
            userName = it.getString("user_name")
            userId = it.getInt("user_id")
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
                return if (position == 0) {
                    UserFollowersFragmentNew.newInstance("")
                } else {
                    UserFollowingsFragmentNew.newInstance("")
                }
            }

            /*override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
                try {
                    super.restoreState(state, loader)
                } catch (e: Exception) {
                }
            }*/
        }
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
        viewPager.offscreenPageLimit = 2


        binding.toggleGender.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnFollowers -> viewPager.currentItem = 0
                    R.id.btnFollowing -> viewPager.currentItem = 1
                }
            }
        }


        val bundle = intent?.extras
        bundle?.let {
            when (bundle.getString(Constants.TAB_KEY)) {
                Constants.TAB_FOLLOWERS -> {
                    binding.toggleGender.check(R.id.btnFollowers)
                    viewPager.currentItem = 0
                }
                Constants.TAB_FOLLOWINGS -> {
                    binding.toggleGender.check(R.id.btnFollowing)
                    viewPager.currentItem = 1
                }

            }
        } ?: run {
            binding.toggleGender.check(R.id.btnFollowers)
        }


        binding.toolbar.btnClose.setOnClickListener {
            finish()
        }


    }


}
