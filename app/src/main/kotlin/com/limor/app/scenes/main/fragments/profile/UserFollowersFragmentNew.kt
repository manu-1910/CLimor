package com.limor.app.scenes.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.App
import com.limor.app.FollowersQuery
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentUsersBlockedBinding
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.adapters.UserFollowersAdapter
import com.limor.app.scenes.main.fragments.settings.SettingsViewModel
import com.limor.app.scenes.main.viewmodels.GetBlockedUsersViewModel
import com.limor.app.uimodels.UserUIModel
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_users_blocked.*
import kotlinx.android.synthetic.main.item_grid_casts_list.view.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.onRefresh
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject


class UserFollowersFragmentNew(private val uiUserId: Int?) : BaseFragment() {

    private lateinit var arrayList: ArrayList<UserUIModel?>
    private lateinit var searchResults: ArrayList<UserUIModel?>
    private var isLastPage: Boolean = false
    private var isScrolling: Boolean = false
    private var isRequestingNewData: Boolean = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: SettingsViewModel by activityViewModels { viewModelFactory }

    private lateinit var binding: FragmentUsersBlockedBinding

    private lateinit var blockedUsersAdapter: UserFollowersAdapter
    private lateinit var followersAdapter: UserFollowersAdapter

    private var offset = 0

    companion object {
        val TAG: String = UserFollowersFragmentNew::class.java.simpleName
        fun newInstance(uiUser: Int?) = UserFollowersFragmentNew(uiUser)
        private const val OFFSET_INFINITE_SCROLL: Int = 10
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersBlockedBinding.inflate(inflater, container, false)
        arrayList = ArrayList()
        searchResults = ArrayList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        configureEmptyScenario()

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        initRecyclerView()
        initApiCallCreateBlockedUser()
        initApiCallDeleteBlockedUser()
        initSwipeAndRefreshLayout()
        subscribeViewModel()

        reload()
    }

    private fun configureEmptyScenario() {
        binding.layEmptyScenario.ivEmptyScenario.visibility = View.GONE
        binding.layEmptyScenario.tvTitleEmptyScenario.text = ""
        binding.layEmptyScenario.tvDescriptionEmptyScenario.text =
            getString(R.string.empty_scenario_followers)
        binding.layEmptyScenario.tvActionEmptyScenario.visibility = View.GONE
    }

    private fun initToolbar() {

    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.rvBlockedUsers.layoutManager = layoutManager
        blockedUsersAdapter = UserFollowersAdapter(arrayList,
            object : UserFollowersAdapter.OnFollowerClickListener {
                override fun onUserClicked(
                    item: UserUIModel,
                    position: Int
                ) {
                    val userProfileIntent = Intent(context, UserProfileActivity::class.java)
                    userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.username)
                    userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.id)
                    startActivity(userProfileIntent)
                }

                override fun onUserLongClicked(item: UserUIModel, position: Int) {

                }

                override fun onFollowClicked(
                    item: UserUIModel,
                    position: Int
                ) {
                    if (item.isFollowed!!) {
                        requestUnfollow(item, position)
                    } else {

                        followUser(item)
                        blockedUsersAdapter.updateItem(item, position)
                    }

                }


            })

        /* binding.rvBlockedUsers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
             override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                 super.onScrollStateChanged(recyclerView, newState)
                 if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                     isScrolling = true
             }

             override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                 super.onScrolled(recyclerView, dx, dy)

                 // if we scroll down...
                 if (dy > 0) {

                     // those are the items that we have already passed in the list, the items we already saw
                     val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                     // this are the items that are currently showing on screen
                     val visibleItemsCount = layoutManager.childCount

                     // this are the total amount of items
                     val totalItemsCount = layoutManager.itemCount

                     // if the past items + the current visible items + offset is greater than the total amount of items, we have to retrieve more data
                     if (!isRequestingNewData && isScrolling && !isLastPage && visibleItemsCount + pastVisibleItems + OFFSET_INFINITE_SCROLL >= totalItemsCount) {
                         isScrolling = false
                         setViewModelVariables()
                         requestNewData(false)
                     }
                 }
             }
         })*/
        binding.rvBlockedUsers.adapter = blockedUsersAdapter
        binding.rvBlockedUsers.setHasFixedSize(false)

        val followersLayoutManager = LinearLayoutManager(context)
        binding.rvSearchResults.layoutManager = followersLayoutManager
        followersAdapter = UserFollowersAdapter(searchResults,
            object : UserFollowersAdapter.OnFollowerClickListener {
                override fun onUserClicked(
                    item: UserUIModel,
                    position: Int
                ) {
                    val userProfileIntent = Intent(context, UserProfileActivity::class.java)
                    userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.username)
                    userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.id)
                    startActivity(userProfileIntent)
                }

                override fun onUserLongClicked(item: UserUIModel, position: Int) {

                }

                override fun onFollowClicked(
                    item: UserUIModel,
                    position: Int
                ) {
                    if (item.isFollowed!!) {
                        requestUnfollow(item, position)
                    } else {

                        followUser(item)
                        blockedUsersAdapter.updateItem(item, position)
                    }

                }


            })
        binding.rvSearchResults.adapter = followersAdapter
        binding.rvSearchResults.setHasFixedSize(false)
    }

    private fun setViewModelVariables() {

    }

    private fun subscribeViewModel() {
        model.followersData.observe(viewLifecycleOwner, Observer {
            Timber.d("observe -> $it")
            if (it?.size == 0) {
                // showEmptyScenario()
            } else {
                isRequestingNewData = false
                val adapter = (binding.rvBlockedUsers.adapter as UserFollowersAdapter)
                arrayList.addAll(it!!)
                adapter.notifyDataSetChanged()
                hideEmptyScenario()
            }

            hideProgressBar()
        })
        model.searchFollowersData.observe(viewLifecycleOwner, {
            if (it == null) {
                binding.tvNoFollowers.visibility = View.GONE
                binding.rvSearchResults.visibility = View.GONE
                binding.laySwipeRefresh.visibility = View.VISIBLE
            } else if (it.isEmpty()) {
                binding.tvNoFollowers.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
                binding.laySwipeRefresh.visibility = View.GONE
            } else {
                Log.d("follower_count", model.searchFollowersOffset.toString())
                if (model.searchFollowersOffset == 0)
                    searchResults.clear()
                searchResults.addAll(it)
                followersAdapter.notifyDataSetChanged()
                if (binding.rvSearchResults.visibility == View.GONE)
                    binding.rvSearchResults.visibility = View.VISIBLE
                binding.tvNoFollowers.visibility = View.GONE
                binding.laySwipeRefresh.visibility = View.GONE
            }
        })
    }

    private fun requestUnfollow(item: UserUIModel, position: Int) {
        alert(getString(R.string.confirmation_unfollow_user)) {
            okButton {
                unFollowUser(item)
                blockedUsersAdapter.updateItem(item, position)
            }
            cancelButton { }
        }.show()
    }

    private fun followUser(item: UserUIModel) {
        model.followUser(item.id!!)
    }

    private fun unFollowUser(item: UserUIModel) {
        model.unFollowUser(item.id!!)

    }

    private fun bindViewModel() {

    }

    private fun initSwipeAndRefreshLayout() {
        binding.laySwipeRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        binding.laySwipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.brandPrimary500
            )
        )

        binding.laySwipeRefresh.onRefresh {
            reload()
        }
    }


    private fun reload() {
        isLastPage = false
        model.blockUsersOffset = 0
        hideEmptyScenario()
        // model.clearFollowers()
        arrayList.clear()
        requestNewData()
    }


    private fun initApiCallGetBlockedUsers() {

        if (model.followersData.value == null || model.followersData.value?.size != 0) {
            model.getFollowers(uiUserId, arrayList.size)
        } else {
            model.clearFollowers()
            model.getFollowers(uiUserId, 0)
        }

    }

    private fun requestNewData(showProgress: Boolean = true) {
        if (!isRequestingNewData) {
            if (showProgress)
                showProgressBar()
            isRequestingNewData = true
            initApiCallGetBlockedUsers()
        }
    }

    private fun showProgressBar() {
        binding.laySwipeRefresh.let {
            if (!it.isRefreshing) {
                it.isRefreshing = true
            }
        }
    }

    private fun hideProgressBar() {
        binding.laySwipeRefresh.let {
            if (it.isRefreshing) {
                it.isRefreshing = false
            }
        }
    }

    private fun hideEmptyScenario() {
        //binding.layEmptyScenario.visibility = View.GONE
        binding.rvBlockedUsers.visibility = View.VISIBLE
    }

    private fun showEmptyScenario() {
        //binding.layEmptyScenario.visibility = View.VISIBLE
        binding.rvBlockedUsers.visibility = View.GONE
    }


    private fun initApiCallDeleteBlockedUser() {

    }

    private fun initApiCallCreateBlockedUser() {

    }

}