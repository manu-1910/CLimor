package com.limor.app.scenes.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.GetUserProfileByIdQuery
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.components.tabselector.TabSelectorView
import com.limor.app.databinding.UserProfileFragmentBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.adapters.ProfileViewPagerAdapter
import com.limor.app.scenes.main.fragments.settings.SettingsActivity
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.uimodels.UserUIModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserProfileFragment : FragmentWithLoading(), Injectable {


    companion object {
        const val USER_ID_KEY = "user_id"
        const val USER_NAME_KEY = "username"
    }

    private lateinit var user: UserUIModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: UserProfileViewModel by viewModels { viewModelFactory }

    private lateinit var binding: UserProfileFragmentBinding
    private val tabs by lazy {
        mapOf(
            Tab.CASTS to getString(R.string.casts),
            Tab.PATRON to getString(R.string.limor_patron)
        )
    }
    private var selectedTab: Tab = Tab.CASTS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDefaultView()

        setupListeners()


        setupViewPager()
    }

    private fun setupListeners() {

        binding.tabSelectorView.apply {
            setOnTabSelectedListener { tabName, position ->
                selectedTab = tabs.keys.elementAt(position)
                binding.profileViewpager.currentItem = position
            }
            setMode(TabSelectorView.Mode.FIXED)
            setTabs(tabs.values.toList())
        }

        binding.profileViewpager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabSelectorView.selectTabAt(position)
            }
        })

        binding.followers.setOnClickListener {
            startActivity(
                Intent(requireContext(), UserFollowersFollowingsActivity::class.java)
                    .putExtra(Constants.TAB_KEY, Constants.TAB_FOLLOWERS)
                    .putExtra("user_name", user.username)
                    .putExtra("user_id", user.id)
            )
        }
        binding.following.setOnClickListener {

            startActivity(
                Intent(requireContext(), UserFollowersFollowingsActivity::class.java)
                    .putExtra(Constants.TAB_KEY, Constants.TAB_FOLLOWINGS)
                    .putExtra("user_name", user.username)
                    .putExtra("user_id", user.id)
            )

        }

        binding.toolbar.btnUserSettings.setOnClickListener {
            handleOptionsClick()
        }

        binding.toolbar.btnBack.setOnClickListener {
            it.findNavController().popBackStack()
        }
    }

    private fun handleOptionsClick() {
        if ((activity) is MainActivityNew) {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        } else {
            //Show Other user actions dialog
            findNavController().navigate(R.id.action_profile_fragment_to_dialog_user_actions2)
        }

    }

    private fun setupDefaultView() {
        if ((activity) is MainActivityNew) {
            binding.toolbar.title.text = getString(R.string.title_profile)
        } else {
            binding.toolbar.title.text = ""
            binding.toolbar.btnUserSettings.setImageResource(R.drawable.ic_three_dots_black)

        }
    }

    override fun subscribeToViewModel() {
        super.subscribeToViewModel()

        model.userProfileData.observe(viewLifecycleOwner, {
            it?.let {
                setDataToProfileViews(it)
                setupConditionalViews(it)
            }

        })

        model.profileErrorLiveData.observe(viewLifecycleOwner, {
            binding.profileMainContainer.visibility = View.GONE
            switchCommonVisibility(hasError = true)
        })

    }

    private fun setupConditionalViews(user: UserUIModel) {
        if ((activity) is MainActivityNew) {
            binding.otherUserNormalLayout.visibility = View.VISIBLE
        }
        lifecycleScope.launch {
            Timber.d("LUID CHAECK "+ user.id+ " "+JwtChecker.getUserIdFromJwt())
        }
        binding.profileMainContainer.visibility = View.VISIBLE
        switchCommonVisibility(false)
    }

    private fun setDataToProfileViews(it: UserUIModel) {
        user = it
        binding.profileName.text = it.username
        binding.profileDesc.text = it.description
        binding.profileLink.text = it.website
        binding.profileFollowers.text = "${it.followersCount}"
        binding.profileFollowing.text = "${it.followingCount}"
        Glide.with(requireContext()).load(it.imageLinks.small)
            .placeholder(R.mipmap.ic_launcher_round)
            .error(R.mipmap.ic_launcher_round)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.profileDp)

    }

    override fun load() {

        when (activity) {
            is MainActivityNew -> {
                model.getUserProfile()
               // model.getUserById(PrefsHandler.getCurrentUserId(requireContext()))
            }
            is UserProfileActivity -> {
                activity?.intent?.extras?.getInt(USER_ID_KEY)?.let {
                    model.getUserById(it)
                }
            }
        }





    }

    override val errorLiveData: LiveData<String>
        get() = model.profileErrorLiveData

    private fun setupViewPager() {
        val adapter = ProfileViewPagerAdapter(childFragmentManager, lifecycle)
        binding.profileViewpager.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    enum class Tab {
        CASTS, PATRON
    }
}