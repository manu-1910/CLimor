package com.limor.app.scenes.main.fragments.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.limor.app.FollowersQuery
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.GetUserProfileQuery
import com.limor.app.R
import com.limor.app.apollo.Apollo
import com.limor.app.databinding.ActivityUserProfileFragmentBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.main.fragments.profile.adapters.ProfileViewPagerAdapter
import com.limor.app.scenes.main.fragments.settings.SettingsActivity
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.scenes.profile.FollowersAndFollowingActivity
import kotlinx.android.synthetic.main.fragment_new_auth_gender.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UserProfileFragment : Fragment(), Injectable {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: HomeFeedViewModel by viewModels { viewModelFactory }

    private lateinit var binding: ActivityUserProfileFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityUserProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toggleGender.check(R.id.btnCasts)


        lifecycleScope.launch(Dispatchers.Main) {
            val user = model.getUserProfile()
            user?.let {
                binding.profileName.text = it.username
                binding.profileDesc.text = it.description
                binding.profileFollowers.text = "${it.followers_count}"
                binding.profileFollowing.text = "${it.following_count}"

                setupViewPager(it)

            }
        }


        binding.followers.setOnClickListener {
            startActivity(Intent(requireContext(), FollowersAndFollowingActivity::class.java))
        }

        binding.following.setOnClickListener {

            startActivity(Intent(requireContext(), FollowersAndFollowingActivity::class.java))

        }

        binding.btnUserSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))

        }

        binding.toggleGender.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                val gender = when (checkedId) {
                    R.id.btnCasts -> 0
                    R.id.btnPatron -> 1
                    else -> 0
                }
                //move viewpager
            }
        }

    }

    private fun setupViewPager(it: GetUserProfileQuery.GetUser) {
        val adapter = ProfileViewPagerAdapter(childFragmentManager, lifecycle)

        binding.profileViewpager.adapter = adapter


    }


}