package com.limor.app.scenes.main. fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.App
import com.limor.app.FriendsQuery
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentUsersBlockedBinding
import com.limor.app.scenes.main.fragments.profile.adapters.UserFollowingsAdapter
import com.limor.app.scenes.main.fragments.settings.SettingsViewModel
import com.limor.app.scenes.main.viewmodels.GetBlockedUsersViewModel
import kotlinx.android.synthetic.main.fragment_users_blocked.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.onRefresh
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject



class UserFollowingsFragmentNew(private val uiUser: String) : BaseFragment() {

    private lateinit var arrayList: ArrayList<FriendsQuery.GetFriend?>
    private var isLastPage: Boolean = false
    private var isScrolling: Boolean = false
    private var isRequestingNewData: Boolean = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val  model: SettingsViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentUsersBlockedBinding
    var app: App? = null

    private lateinit var blockedUsersAdapter: UserFollowingsAdapter

    private lateinit var viewModelGetBlockedUsers: GetBlockedUsersViewModel



    companion object {
        val TAG: String = UserFollowersFragmentNew::class.java.simpleName
        fun newInstance( uiUser: String) = UserFollowersFragmentNew(uiUser)
        private const val OFFSET_INFINITE_SCROLL: Int = 10
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsersBlockedBinding.inflate(inflater,container,false)
        arrayList = ArrayList()
        app = context?.applicationContext as App
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


        model.followingsData.observe(viewLifecycleOwner, Observer{
            Timber.d("observe -> $it")
            if(it?.size == 0){
                //showEmptyScenario()
            }else{
                isRequestingNewData = false
                arrayList.addAll(it!!)
                ( binding.rvBlockedUsers.adapter as UserFollowingsAdapter ).notifyDataSetChanged()
                hideEmptyScenario()
            }

            hideProgressBar()
        })

        reload()
    }

    private fun configureEmptyScenario() {
        binding.layEmptyScenario.ivEmptyScenario.visibility = View.GONE
        binding.layEmptyScenario.tvTitleEmptyScenario.text = ""
        binding.layEmptyScenario.tvDescriptionEmptyScenario.text = getString(R.string.empty_scenario_following)
        binding.layEmptyScenario.tvActionEmptyScenario.visibility = View.GONE
    }

    private fun initToolbar() {

    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.rvBlockedUsers.layoutManager = layoutManager
        blockedUsersAdapter = UserFollowingsAdapter(arrayList,
            object : UserFollowingsAdapter.OnFollowerClickListener {
                override fun onUserClicked(
                    item: FriendsQuery.GetFriend,
                    position: Int
                ) {
                    val userProfileIntent = Intent(context, UserProfileActivity::class.java)
                    userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.username)
                    userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.id)
                    startActivity(userProfileIntent)
                }

                override fun onUserLongClicked(item: FriendsQuery.GetFriend, position: Int) {

                }

                override fun onFollowClicked(
                    item: FriendsQuery.GetFriend,
                    position: Int
                ) {
                    Timber.d("Following ->%s", item.toString())
                    if(item.followed!!) {
                        onUnblockButtonClicked(item,position)
                    } else {

                        onBlockButtonClicked(item)
                        blockedUsersAdapter.updateItem(item,position)
                    }
                }




            })

        binding.rvBlockedUsers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        })
        binding.rvBlockedUsers.adapter = blockedUsersAdapter
        binding.rvBlockedUsers.setHasFixedSize(false)
    }

    private fun setViewModelVariables() {

    }

    private fun onUnblockButtonClicked(item: FriendsQuery.GetFriend, position:Int) {
        alert(getString(R.string.confirmation_unfollow_user)) {
            okButton {
                performBlockUser(item)
            }
            cancelButton {  }
        }.show()
    }

    private fun onBlockButtonClicked(item: FriendsQuery.GetFriend) {
        performUnblockUser(item)
    }

    private fun performUnblockUser(item: FriendsQuery.GetFriend) {

    }

    private fun performBlockUser(item: FriendsQuery.GetFriend) {

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

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        isLastPage = false
        model.blockUsersOffset = 0
        hideEmptyScenario()
        model.clearFollowing()
        arrayList.clear()
        rvBlockedUsers?.recycledViewPool?.clear()
        rvBlockedUsers.adapter?.notifyDataSetChanged()
        requestNewData()
    }


    private fun initApiCallGetBlockedUsers() {
        if(model.followingsData.value == null || model.followingsData.value?.size == 0){
            model.getFollowings(arrayList.size)
        }else{
            model.clearFollowing()
            model.getFollowings(0)
        }
    }

    private fun requestNewData(showProgress : Boolean = true) {
        if (!isRequestingNewData) {
            if(showProgress)
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