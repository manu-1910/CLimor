package io.square1.limor.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.adapters.DiscoverUsersAdapter
import io.square1.limor.scenes.main.viewmodels.CreateFriendViewModel
import io.square1.limor.scenes.main.viewmodels.DeleteFriendViewModel
import io.square1.limor.scenes.main.viewmodels.DiscoverAccountsViewModel
import io.square1.limor.uimodels.UIUser
import kotlinx.android.synthetic.main.fragment_discover_accounts.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class DiscoverAccountsFragment : BaseFragment(), DiscoverTabFragment {

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

        fun newInstance(text: String) = DiscoverAccountsFragment().apply {
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

        rvUsers = view.findViewById(R.id.rv_users)
        bindViewModel()
        apiCallCreateUser()
        apiCallDeleteUser()
        initApiCallGetNotifications()
        configureAdapter()
    }


    override fun setSearchText(text: String){
        //if(text != viewModelDiscoverAccounts.searchText){
        //if(viewModelDiscoverAccounts.results.size == 0){
            showProgress(true)
            viewModelDiscoverAccounts.searchText = text
            getUsersTrigger.onNext(Unit)
        //} else{

//            if(text != viewModelDiscoverAccounts.searchText){
//
//            }else{
//                //Just reconnect
//
//            }
//        }

        //}
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
                tv_no_results.visibility = View.VISIBLE
            }else{
                tv_no_results.visibility = View.INVISIBLE
            }
            showProgress(false)

        })

        output.errorMessage.observe(this, Observer {
            tv_no_results?.visibility = View.VISIBLE
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
                        toast("You clicked on a user")
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

            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }
                alert(message.toString()) {
                    okButton { }
                }.show()
            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }

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

            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }
                alert(message.toString()) {
                    okButton { }
                }.show()
            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }

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