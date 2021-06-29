package com.limor.app.scenes.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.GetUserProfileByIdQuery
import com.limor.app.GetUserProfileQuery
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.UserProfileFragmentBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.fragments.profile.adapters.ProfileViewPagerAdapter
import com.limor.app.scenes.main.fragments.settings.SettingsActivity
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import kotlinx.android.synthetic.main.fragment_new_auth_loading_include.*
import javax.inject.Inject

class UserProfileFragment : FragmentWithLoading(), Injectable {


    companion object{
        const val USER_ID_KEY = "user_id"
        const val USER_NAME_KEY = "username"
    }

    private lateinit var user: GetUserProfileByIdQuery.GetUserById

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

        if((activity) is MainActivityNew){
            binding.toolbar.root.visibility = View.VISIBLE
        }else{
            binding.toolbar.root.visibility = View.GONE
        }

        binding.toggleProfileButtons.check(R.id.btnCasts)
        load()
        binding.followers.setOnClickListener {
            startActivity(Intent(requireContext(), UserFollowersFollowingsActivity::class.java)
                .putExtra(Constants.TAB_KEY, Constants.TAB_FOLLOWERS)
                .putExtra("user_name", user.username)
                .putExtra("user_id", user.id)
            )
        }
        binding.following.setOnClickListener {

            startActivity(Intent(requireContext(), UserFollowersFollowingsActivity::class.java)
                .putExtra(Constants.TAB_KEY,Constants.TAB_FOLLOWINGS)
                .putExtra("user_name", user.username)
                .putExtra("user_id", user.id)
            )

        }
        binding.toolbar.btnUserSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.toggleProfileButtons.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                 when (checkedId) {
                    R.id.btnCasts -> binding.profileViewpager.currentItem = 0
                    else -> binding.profileViewpager.currentItem = 1
                }
            }
        }
        binding.toolbar.btnBack.setOnClickListener {
            it.findNavController().popBackStack()
        }

    }

    override fun subscribeToViewModel() {
        super.subscribeToViewModel()

        model.userProfileIdData.observe(viewLifecycleOwner, Observer {
            it?.let {
                user = it
                switchCommonVisibility(false)
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
                setupViewForUser(it)
                setupViewPager(it)
            }
        })

        model.profileErrorLiveData.observe(viewLifecycleOwner,{
            binding.profileMainContainer.visibility = View.GONE
        })

    }

    private fun setupViewForUser(user: GetUserProfileByIdQuery.GetUserById) {

        binding.profileMainContainer.visibility = View.VISIBLE
        if(user.id != PrefsHandler.getCurrentUserId(requireContext())){
            //Views Handle specific to Selected user
            binding.otherUserNormalLayout.visibility = View.VISIBLE

        }else{
            //Views Handled specific to current user
        }


    }

    override fun load() {

        activity?.intent?.extras?.getInt(USER_ID_KEY)?.let{
            model.getUserById(28)
        }?:run{
            model.getUserById(PrefsHandler.getCurrentUserId(requireContext()))
        }

    }

    override val errorLiveData: LiveData<String>
        get() = model.profileErrorLiveData

    private fun setupViewPager(it: GetUserProfileByIdQuery.GetUserById) {
        val adapter = ProfileViewPagerAdapter(childFragmentManager, lifecycle)
        binding.profileViewpager.adapter = adapter


    }

    override fun onResume() {
        super.onResume()
        load()
    }
}