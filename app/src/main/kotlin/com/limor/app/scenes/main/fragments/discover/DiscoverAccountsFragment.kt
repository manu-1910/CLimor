package com.limor.app.scenes.main.fragments.discover

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.adapters.DiscoverUsersAdapter
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.viewmodels.CreateFriendViewModel
import com.limor.app.scenes.main.viewmodels.DeleteFriendViewModel
import com.limor.app.scenes.main.viewmodels.DiscoverAccountsViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIUser
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_discover_accounts.*
import kotlinx.android.synthetic.main.fragment_empty_scenario.*
import javax.inject.Inject

class DiscoverAccountsFragment : BaseFragment(),
    DiscoverTabFragment {

    var app: App? = null


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelDiscoverAccounts: DiscoverAccountsViewModel
    private lateinit var viewModelCreateFriend: CreateFriendViewModel
    private lateinit var viewModelDeleteFriend: DeleteFriendViewModel

    private val createFriendDataTrigger = PublishSubject.create<Unit>()
    private val deleteFriendDataTrigger = PublishSubject.create<Unit>()
    private val getUsersTrigger = PublishSubject.create<Unit>()

    //private var searchText: String = ""
    private var rvUsers: RecyclerView? = null
    private var usersAdapter: DiscoverUsersAdapter? = null
    private var currentFollowItem: UIUser? = null

    companion object {
        val TAG: String = DiscoverAccountsFragment::class.java.simpleName

        fun newInstance(text: String) = DiscoverAccountsFragment()
            .apply {
            arguments = Bundle(1).apply {
                putString(BUNDLE_KEY_SEARCH_TEXT, text)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover_accounts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = context?.applicationContext as App
        configureEmptyScenario()
        rvUsers = view.findViewById(R.id.rv_users)
        bindViewModel()
        apiCallCreateUser()
        apiCallDeleteUser()
        initApiCallGetNotifications()
        configureAdapter()
    }



    private fun configureEmptyScenario() {
        tvActionEmptyScenario.visibility = View.GONE
        context?.let {
            ivEmptyScenario.setImageDrawable(
                ContextCompat.getDrawable(
                    it, R.drawable.search_icon_empty_scenario
                )
            )
        }
        tvTitleEmptyScenario.text = getString(R.string.search_empty_scenario_title)
        tvDescriptionEmptyScenario.text = getString(R.string.search_empty_scenario_description)
    }


    override fun setSearchText(text: String){
            showProgress(true)
            viewModelDiscoverAccounts.searchText = text
            getUsersTrigger.onNext(Unit)
    }

    fun clearAdapter(){
        viewModelDiscoverAccounts.results.clear()
        rvUsers?.adapter?.notifyDataSetChanged()
    }

    private fun initApiCallGetNotifications() {
        val output = viewModelDiscoverAccounts.transform(
            DiscoverAccountsViewModel.Input(
                getUsersTrigger
            )
        )

        output.response.observe(this, Observer {

            rvUsers?.adapter?.notifyDataSetChanged()
            if(it.data.users.size == 0){
                layEmptyScenario.visibility = View.VISIBLE
            }else{
                layEmptyScenario.visibility = View.GONE
            }
            showProgress(false)

        })

        output.errorMessage.observe(this, Observer {
            layEmptyScenario.visibility = View.VISIBLE
            viewModelDiscoverAccounts.results.clear()
            rvUsers?.adapter?.notifyDataSetChanged()
            showProgress(false)
        })

    }

    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvUsers?.layoutManager = layoutManager
        usersAdapter = context?.let {
            DiscoverUsersAdapter(
                requireContext(),
                viewModelDiscoverAccounts.results,
                object : DiscoverUsersAdapter.OnUserSearchClicked {
                    override fun onUserClicked(item: UIUser, position: Int) {
                        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
                        userProfileIntent.putExtra("user", item)
                        startActivity(userProfileIntent)
                    }

                    override fun onFollowClicked(item: UIUser, position: Int) {

                        currentFollowItem = item
                        val userId = item.id

                        if(item.followed){
                            viewModelDeleteFriend.idFriend = userId
                            deleteFriendDataTrigger.onNext(Unit)
                        }else{
                            viewModelCreateFriend.idNewFriend = userId
                            createFriendDataTrigger.onNext(Unit)
                        }

                        showProgress(true)
                    }
                }
            )
        }
        rvUsers?.adapter = usersAdapter

//        rvUsers?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
//                    isScrolling = true
//            }
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                // if we scroll down...
//                if (dy > 0) {
//
//                    // those are the items that we have already passed in the list, the items we already saw
//                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
//
//                    // this are the items that are currently showing on screen
//                    val visibleItemsCount = layoutManager.childCount
//
//                    // this are the total amount of items
//                    val totalItemsCount = layoutManager.itemCount
//
//                    // if the past items + the current visible items + offset is greater than the total amount of items, we have to retrieve more data
//                    if (isScrolling && !isLastPage && visibleItemsCount + pastVisibleItems + NotificationsFragment.OFFSET_INFINITE_SCROLL >= totalItemsCount) {
//                        isScrolling = false
//                        setNotificationViewModelVariables(viewModelNotifications.notificationList.size - 1)
//                        showProgress(true)
//                        getNotificationsTrigger.onNext(Unit)
//                    }
//                }
//            }
//        })

        rvUsers?.setHasFixedSize(false)
        val divider = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        context?.getDrawable(R.drawable.divider_item_recyclerview)?.let { divider.setDrawable(it) }
        rvUsers?.addItemDecoration(divider)

        if(viewModelDiscoverAccounts.results.size != 0){
            showProgress(false)
        }

    }

    private fun showProgress(show: Boolean) {
        if (show) {
            pb_loading.visibility = View.VISIBLE
        } else {
            pb_loading.visibility = View.INVISIBLE
        }
    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelDiscoverAccounts = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DiscoverAccountsViewModel::class.java)

            viewModelCreateFriend = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateFriendViewModel::class.java)

            viewModelDeleteFriend = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeleteFriendViewModel::class.java)
        }
    }

    private fun apiCallCreateUser() {
        val output = viewModelCreateFriend.transform(
            CreateFriendViewModel.Input(
                createFriendDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if(it.code == 0) {
                revertUserFollowedState()
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {

            CommonsKt.handleOnApiError(app!!, context!!, this, it)

            showProgress(false)

        })
    }


    private fun apiCallDeleteUser() {
        val output = viewModelDeleteFriend.transform(
            DeleteFriendViewModel.Input(
                deleteFriendDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if(it.code == 0) {
                revertUserFollowedState()
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {

            CommonsKt.handleOnApiError(app!!, context!!, this, it)

            showProgress(false)
        })
    }


    private fun revertUserFollowedState() {

        currentFollowItem?.let {

            val index = viewModelDiscoverAccounts.updateFollowedStatus(currentFollowItem!!)

            if(index != -1){
                rvUsers?.adapter?.notifyItemChanged(index)
            }


        }

        showProgress(false)

    }

}