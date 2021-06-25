package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButtonToggleGroup
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
        overridePendingTransition(0,0)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_followers_and_following) as ActivityFollowersAndFollowingBinding






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
        val viewPager: ViewPager2 = binding.followViewPager
        val names = arrayOf(
            getString(R.string.followers_count, followersCount),
            getString(R.string.followings_count, followingsCount)
        )

        val adapter = object : FragmentStateAdapter(
            supportFragmentManager,lifecycle
        ) {


            override fun getItemCount(): Int {
                return names.size
            }

            override fun createFragment(position: Int): Fragment {
                return if(position == 0){
                    UserFollowersFragmentNew.newInstance("")
                }else{
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


        binding.toggleGender.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnFollowers -> viewPager.currentItem = 0
                    R.id.btnFollowing -> viewPager.currentItem = 1
                }
            }
        }


        val bundle = intent?.extras
        bundle?.let{
            when(bundle.getString("tab")){
                "followers" ->{
                    binding.toggleGender.check(R.id.btnFollowers)
                    viewPager.currentItem = 0
                }
                "following" ->{
                    binding.toggleGender.check(R.id.btnFollowing)
                    viewPager.currentItem = 1
                }

            }
        }?:run{
            binding.toggleGender.check(R.id.btnFollowers)
        }


        binding.toolbar.btnClose.setOnClickListener{
            finish()
        }



    }


}
