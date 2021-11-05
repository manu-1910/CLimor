package com.limor.app.scenes.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.components.tabselector.TabSelectorView
import com.limor.app.databinding.UserProfileFragmentBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.main.fragments.profile.adapters.ProfileViewPagerAdapter
import com.limor.app.scenes.main.fragments.settings.OpenSettings
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.profile.DialogUserProfileActions
import com.limor.app.uimodels.AudioCommentUIModel
import com.limor.app.uimodels.UserUIModel
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.startActivityForResult
import java.time.Duration
import javax.inject.Inject

class UserProfileFragment : FragmentWithLoading(), Injectable {


    companion object {
        const val USER_ID_KEY = "user_id"
        const val USER_NAME_KEY = "username"
        const val TAB_POS = "tab_pos"
    }

    private lateinit var user: UserUIModel
    private var isSignedInUser = false;

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
    private var currentUserId: Int? = null
    private val openSettings = registerForActivityResult(OpenSettings()) { settingsHaveChanged ->
        if (settingsHaveChanged) {
            loadUserData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkSignedInUser()
    }

    override fun loadFromUserAction() {
        // no super call because it'd call load which we don't need or use anyway
        loadUserData()
    }

    override fun load() {
        // nope.. we don't make use of this func, because it's called on two conflicting occasions:
        // - automatically on fragment's creation
        // - initiated by user when clicking retry
        //
        // and if we load the user data here then the whole view setup and adapter creation would
        // be repeated
    }

    private fun loadUserData() {
        if (isSignedInUser) {
            model.getUserProfile()
        } else {
            model.getUserById(getIntentUserId())
        }
    }

    private fun checkSignedInUser() {
        lifecycleScope.launchWhenCreated {
            currentUserId = JwtChecker.getUserIdFromJwt(false)
            val id = getIntentUserId()
            isSignedInUser = id == 0 || id == currentUserId
            setup()
        }
    }

    private fun setup() {
        ensureToolbar()

        setupDefaultView()

        setupListeners()

        observeProfileActions()

        val loadedUserId = model.userProfileData.value?.id

        // if not user is retained in the view model then we just normally load the user data
        //
        if (loadedUserId == null) {
            subscribeToModels()
            loadUserData()

        } else {
            // if there already is a user loaded in the model then we need to ensure this isn't
            // another user and reset it so the subscribers don't load its data, however if it's
            // the user we want we just let the subscribers consume it
            //
            val targetUserId = if (isSignedInUser) currentUserId else getIntentUserId()
            if (targetUserId == loadedUserId) {
                subscribeToModels()
            } else {
                model.resetUser()
                subscribeToModels()
                loadUserData()
            }
        }
    }

    private fun observeProfileActions() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("blocked")
            ?.observe(viewLifecycleOwner) { blocked ->
                user = user.copy(isBlocked = blocked)
                setupConditionalViews(user)
            }
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

        setupDefaultTab()

        binding.profileViewpager.isUserInputEnabled = false
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

        binding.btnFollow.setOnClickListener {

            if (user.isFollowed == true) {
                user = user.copy(isFollowed = false)
                model.unFollow(user.id)
            } else {
                user = user.copy(isFollowed = true)
                model.startFollowing(user.id)
            }
            setupConditionalViews(user)
        }

        binding.toolbar.btnUserSettings.setOnClickListener {
            handleOptionsClick()
        }

        binding.toolbar.btnBack.setOnClickListener {
            (activity)?.onBackPressed()
        }
    }

    private fun setupDefaultTab() {
        val tabPos = activity?.intent?.extras?.getInt(TAB_POS)?:0
        binding.tabSelectorView.selectTabAt(tabPos)
    }

    private fun handleOptionsClick() {
        if (isSignedInUser) {
            openSettings.launch(null)

        } else if (::user.isInitialized){
            //Show Other user actions dialog
            val bundle = bundleOf(
                DialogUserProfileActions.USER_KEY to user
            )
            findNavController().navigate(R.id.dialog_user_profile_actions, bundle)
        }

    }

    private fun setupDefaultView() {
        /* if ((activity) is MainActivityNew) {
             binding.toolbar.title.text = getString(R.string.title_profile)
         } else {
             //Toolbar
             activity?.intent?.extras?.getString(USER_NAME_KEY)?.let {
                 binding.toolbar.title.text = it
             } ?: run {
                 binding.toolbar.title.text = ""
             }
             binding.toolbar.btnUserSettings.setImageResource(R.drawable.ic_three_dots_black)


         }*/

        // ensureToolbar()
    }

    private fun ensureToolbar() {
        if (!::binding.isInitialized) {
            return
        }

        val username = activity?.intent?.extras?.getString(USER_NAME_KEY)

        if (isSignedInUser) {
            binding.toolbar.btnUserSettings.setImageResource(R.drawable.ic_setting)

        } else if (!username.isNullOrEmpty()) {
            binding.toolbar.title.text = "@$username"
            binding.toolbar.btnUserSettings.setImageResource(R.drawable.ic_three_dots_black)

        }
    }

    fun subscribeToModels() {
        model.userProfileData.observe(viewLifecycleOwner, {
            it?.let {
                setDataToProfileViews(it)
                setupConditionalViews(it)
                setupViewPager(it)
            }
        })

        model.profileErrorLiveData.observe(viewLifecycleOwner, {
            binding.profileMainContainer.visibility = View.GONE
        })
    }

    private fun setupConditionalViews(user: UserUIModel) {

        ensureToolbar()
        binding.toolbar.title.text = "@${user.username}"
        binding.btnFollow.visibility = if (isSignedInUser) View.GONE else View.VISIBLE
        binding.profileMainContainer.visibility = View.VISIBLE

        if (isSignedInUser) {
            return
        }

        setUpOtherPerson()
    }

    private fun setUpOtherPerson() {
        if (user.isBlocked == true) {

            //Blocked State
            binding.otherUserNormalLayout.visibility = View.GONE
            binding.profileViewpager.visibility = View.GONE
            binding.tabSelectorView.visibility = View.GONE
            //ToastMaker.showToast(requireContext(), "Blocked UI")

        } else {

            if (user.isFollowed == true) {
                //Followed state

                binding.btnFollow.setBackgroundResource(R.drawable.bg_round_bluish_ripple)
                binding.btnFollow.text = getString(R.string.unfollow)
                //ToastMaker.showToast(requireContext(), "Unfollow UI")

            } else {
                //New User State
                binding.btnFollow.setBackgroundResource(R.drawable.bg_round_yellow_ripple)
                binding.btnFollow.text = getString(R.string.follow)
                //ToastMaker.showToast(requireContext(), "Follow UI")
            }

            binding.otherUserNormalLayout.visibility = View.VISIBLE
            binding.profileViewpager.visibility = View.VISIBLE
            binding.tabSelectorView.visibility = View.VISIBLE

        }
    }

    private fun setDataToProfileViews(it: UserUIModel) {
        user = it
        binding.profileName.text = it.getFullName()
        if(it.isVerified == true){
            binding.ivVerifiedAvatar.visibility = View.VISIBLE
        } else{
            binding.ivVerifiedAvatar.visibility = View.GONE
        }
        binding.profileDesc.text = it.description
        binding.profileLink.text = it.website
        binding.profileFollowers.text = "${it.followersCount}"
        binding.profileFollowing.text = "${it.followingCount}"

        val url = it.voiceBioURL

        if (url.isNullOrEmpty()) {
            binding.audioPlayer.visibility = View.GONE
        } else {
            val durationMillis = ((it.durationSeconds ?: 0.0) * 1000.0).toLong()
            binding.audioPlayer.initialize(
                AudioCommentUIModel(
                    url = url,
                    duration = Duration.ofMillis(durationMillis)
                )
            )
            binding.audioPlayer.visibility = View.VISIBLE
        }

        val avatarUrl = it.getAvatarUrl();
        Glide.with(requireContext())
            .load(avatarUrl)
            .signature(ObjectKey(avatarUrl ?: ""))
            .placeholder(R.drawable.ic_podcast_listening)
            .error(R.drawable.ic_podcast_listening)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.profileDp)

        switchCommonVisibility()
    }

    private fun getIntentUserId(): Int {
        return activity?.intent?.extras?.getInt(USER_ID_KEY, 0) ?: 0
    }

    override val errorLiveData: LiveData<String>
        get() = model.profileErrorLiveData

    private fun setupViewPager(user: UserUIModel) {
        val adapter = ProfileViewPagerAdapter(user, childFragmentManager, lifecycle)
        binding.profileViewpager.adapter = adapter
    }

    enum class Tab {
        CASTS, PATRON
    }
}