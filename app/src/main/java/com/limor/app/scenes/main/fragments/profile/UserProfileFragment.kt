package com.limor.app.scenes.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.GetUserProfileQuery
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.UserProfileFragmentBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.main.fragments.profile.adapters.ProfileViewPagerAdapter
import com.limor.app.scenes.main.fragments.settings.SettingsActivity
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import javax.inject.Inject

class UserProfileFragment : Fragment(), Injectable {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: HomeFeedViewModel by viewModels { viewModelFactory }

    private lateinit var binding: UserProfileFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toggleGender.check(R.id.btnCasts)



        model.userProfileData.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.profileName.text = it.username
                binding.profileDesc.text = it.description
                binding.profileLink.text = it.website
                binding.profileFollowers.text = "${it.followers_count}"
                binding.profileFollowing.text = "${it.following_count}"
                Glide.with(requireContext()).load(it.images?.small_url)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileDp)
                setupViewPager(it)
            }
        })
        model.getUserProfile()



        binding.followers.setOnClickListener {
            startActivity(Intent(requireContext(), UserFollowersFollowingsActivity::class.java)
                .putExtra(Constants.TAB_KEY, Constants.TAB_FOLLOWERS)
            )
        }

        binding.following.setOnClickListener {

            startActivity(Intent(requireContext(), UserFollowersFollowingsActivity::class.java)
                .putExtra(Constants.TAB_KEY,Constants.TAB_FOLLOWINGS)
            )

        }

        binding.btnUserSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))

        }

        binding.toggleGender.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                 when (checkedId) {
                    R.id.btnCasts -> 0
                    else -> 1
                }
            }
        }

    }

    private fun setupViewPager(it: GetUserProfileQuery.GetUser) {
        val adapter = ProfileViewPagerAdapter(childFragmentManager, lifecycle)

        binding.profileViewpager.adapter = adapter


    }

    override fun onResume() {
        super.onResume()
        model.getUserProfile()
    }
}