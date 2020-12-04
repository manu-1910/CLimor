package com.limor.app.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
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
import com.limor.app.scenes.main.adapters.NotificationsAdapter
import com.limor.app.scenes.main.viewmodels.CreateFriendViewModel
import com.limor.app.scenes.main.viewmodels.DeleteFriendViewModel
import com.limor.app.scenes.main.viewmodels.NotificationsViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UINotificationItem
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_empty_scenario.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.uiThread
import javax.inject.Inject


class NotificationsFragment : BaseFragment() {

    var app: App? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelNotifications: NotificationsViewModel
    private lateinit var viewModelCreateFriend: CreateFriendViewModel
    private lateinit var viewModelDeleteFriend: DeleteFriendViewModel

    private val getNotificationsTrigger = PublishSubject.create<Unit>()
    private val createFriendDataTrigger = PublishSubject.create<Unit>()
    private val deleteFriendDataTrigger = PublishSubject.create<Unit>()

    private var notificationAdapter: NotificationsAdapter? = null

    // views
    private var rvNotifications: RecyclerView? = null

    // infinite scroll variables
    private var isScrolling: Boolean = false
    private var isLastPage: Boolean = false

    private var currentFollowItem: UINotificationItem? = null

    companion object {
        val TAG: String = NotificationsFragment::class.java.simpleName
        fun newInstance() = NotificationsFragment()
        private const val OFFSET_INFINITE_SCROLL = 2
        private const val FEED_LIMIT_REQUEST = 15
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = context?.applicationContext as App

        rvNotifications = view.findViewById(R.id.rv_notifications)

        showEmptyScenario(true)

        bindViewModel()
        initApiCallGetNotifications()
        apiCallCreateUser()
        apiCallDeleteUser()
        configureAdapter()
        initSwipeRefreshLayout()

        if (viewModelNotifications.notificationList.size == 0) {
            getNotificationsTrigger.onNext(Unit)
        } else {
            showEmptyScenario(false)
            showProgress(false)
        }
    }

    private fun initSwipeRefreshLayout() {

        swipeRefreshLayout_notifications.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorPrimaryDark
            )
        )

        swipeRefreshLayout_notifications.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.brandPrimary500
            )
        )

        swipeRefreshLayout_notifications.setOnRefreshListener {
            isLastPage = false
            viewModelNotifications.notificationList.clear()
            viewModelNotifications.notificationMap.clear()
            setNotificationViewModelVariables(0)
            getNotificationsTrigger.onNext(Unit)
        }
    }

    private fun initApiCallGetNotifications() {
        val output = viewModelNotifications.transform(
            NotificationsViewModel.Input(
                getNotificationsTrigger
            )
        )

        output.response.observe(this, Observer {

            val notificationsLength = it.data.notificationItems.size

            if (notificationsLength == 0 && viewModelNotifications.notificationList.size == 0) {
                showEmptyScenario(true)
            } else if (notificationsLength == 0) {
                isLastPage = true
            } else {
                doAsync {

                    viewModelNotifications.addItems(it.data.notificationItems)

                    uiThread {
                        showEmptyScenario(false)
                        rvNotifications?.adapter?.notifyItemRangeInserted(
                            viewModelNotifications.oldLength,
                            viewModelNotifications.newLength
                        )
                    }
                }


            }

            showProgress(false)

        })

        output.errorMessage.observe(this, Observer {
            showEmptyScenario(true)
            showProgress(false)
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })

    }


    private fun showEmptyScenario(show: Boolean) {
        if (show) {
            context?.let {
                ivEmptyScenario.setImageDrawable(
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.notifications_icon_empty_scenario
                    )
                )
            }
            layEmptyScenario.visibility = View.VISIBLE
            tvTitleEmptyScenario.text = getString(R.string.title_notifications)
            tvDescriptionEmptyScenario.text =
                getString(R.string.empty_scenario_notifications_description)
            tvActionEmptyScenario.visibility = View.GONE
        } else {
            layEmptyScenario.visibility = View.GONE
        }
    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelNotifications = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(NotificationsViewModel::class.java)

            viewModelCreateFriend = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateFriendViewModel::class.java)

            viewModelDeleteFriend = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeleteFriendViewModel::class.java)
        }
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            pb_notifications.visibility = View.VISIBLE
        } else {
            swipeRefreshLayout_notifications.isRefreshing = false
            pb_notifications.visibility = View.INVISIBLE
        }
    }

    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvNotifications?.layoutManager = layoutManager
        notificationAdapter = context?.let {
            NotificationsAdapter(
                requireContext(),
                viewModelNotifications.notificationList,
                object : NotificationsAdapter.OnNotificationClicked {
                    override fun onNotificationClicked(item: UINotificationItem, position: Int) {
                        toast("You clicked on a notification")
                    }

                    override fun onFollowClicked(item: UINotificationItem, position: Int) {

                        currentFollowItem = item
                        val userId = item.resources.owner.id

                        if (item.resources.owner.followed) {
                            viewModelDeleteFriend.idFriend = userId
                            deleteFriendDataTrigger.onNext(Unit)
                        } else {
                            viewModelCreateFriend.idNewFriend = userId
                            createFriendDataTrigger.onNext(Unit)
                        }

                        showProgress(true)
                    }
                }
            )
        }
        rvNotifications?.adapter = notificationAdapter

        rvNotifications?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                    if (isScrolling && !isLastPage && visibleItemsCount + pastVisibleItems + OFFSET_INFINITE_SCROLL >= totalItemsCount) {
                        isScrolling = false
                        setNotificationViewModelVariables(viewModelNotifications.notificationList.size - 1)
                        showProgress(true)
                        getNotificationsTrigger.onNext(Unit)
                    }
                }
            }
        })

        rvNotifications?.setHasFixedSize(false)
        val divider = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        context?.getDrawable(R.drawable.divider_item_recyclerview)?.let { divider.setDrawable(it) }
        rvNotifications?.addItemDecoration(divider)

    }

    private fun setNotificationViewModelVariables(newOffset: Int = 0) {
        viewModelNotifications.limit = FEED_LIMIT_REQUEST
        viewModelNotifications.offset = newOffset
    }

    private fun apiCallCreateUser() {
        val output = viewModelCreateFriend.transform(
            CreateFriendViewModel.Input(
                createFriendDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code == 0) {
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
            if (it.code == 0) {
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

            val index = viewModelNotifications.updateFollowedStatus(currentFollowItem!!)

            if (index != -1) {
                rvNotifications?.adapter?.notifyItemChanged(index)
            }


        }

        showProgress(false)

    }


}
