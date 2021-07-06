package com.limor.app.scenes.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.R
import com.limor.app.databinding.FragmentProfileBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.main.fragments.profile.UserFollowersFollowingsActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel
import com.limor.app.scenes.main.fragments.profile.adapters.ProfileViewPagerAdapter
import com.limor.app.uimodels.UserUIModel
import javax.inject.Inject


class ProfileFragment : FragmentWithLoading(), Injectable {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: UserProfileViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toggleProfileButtons.check(R.id.btnCasts)


        model.userProfileData.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.profileName.text = it.username
                binding.profileDesc.text = it.description
                binding.profileLink.text = it.website
                binding.profileFollowers.text = "${it.followersCount}"
                binding.profileFollowing.text = "${it.followingCount}"
                Glide.with(requireContext()).load(it.imageLinks?.small)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileDp)
                setupViewPager(it)
            }
        })

        load()



        binding.followers.setOnClickListener {
            startActivity(
                Intent(requireContext(), UserFollowersFollowingsActivity::class.java)
                    .putExtra("tab", "followers")
            )
        }

        binding.following.setOnClickListener {

            startActivity(
                Intent(requireContext(), UserFollowersFollowingsActivity::class.java)
                    .putExtra("tab", "following")
            )

        }

        binding.toggleProfileButtons.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnCasts -> binding.profileViewpager.currentItem = 0
                    else -> binding.profileViewpager.currentItem = 1
                }
            }
        }

    }

    override fun load() {
        model.getUserProfile()
    }

    override val errorLiveData: LiveData<String>
        get() = model.profileErrorLiveData

    private fun setupViewPager(user: UserUIModel) {
        val adapter = ProfileViewPagerAdapter(user.id, childFragmentManager, lifecycle)
        binding.profileViewpager.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        model.getUserProfile()
    }
}