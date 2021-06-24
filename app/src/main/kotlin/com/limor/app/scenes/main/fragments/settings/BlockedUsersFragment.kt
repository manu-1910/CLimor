package com.limor.app.scenes.main.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.adapters.BlockedUsersAdapter
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.viewmodels.CreateBlockedUserViewModel
import com.limor.app.scenes.main.viewmodels.DeleteBlockedUserViewModel
import com.limor.app.scenes.main.viewmodels.GetBlockedUsersViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIUser
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_empty_scenario.*
import kotlinx.android.synthetic.main.fragment_users_blocked.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class BlockedUsersFragment : BaseFragment() {

    private var isLastPage: Boolean = false
    private var isScrolling: Boolean = false
    private var isRequestingNewData: Boolean = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var rootView: View? = null
    var app: App? = null

    private lateinit var blockedUsersAdapter: BlockedUsersAdapter

    private lateinit var viewModelGetBlockedUsers: GetBlockedUsersViewModel
    private lateinit var viewModelCreateBlockedUser: CreateBlockedUserViewModel
    private lateinit var viewModelDeleteBlockedUser: DeleteBlockedUserViewModel
    private val getBlockedUsersDataTrigger = PublishSubject.create<Unit>()
    private val createBlockedUserDataTrigger = PublishSubject.create<Unit>()
    private val deleteBlockedUserDataTrigger = PublishSubject.create<Unit>()


    companion object {
        val TAG: String = BlockedUsersFragment::class.java.simpleName
        fun newInstance() = BlockedUsersFragment()
        private const val OFFSET_INFINITE_SCROLL: Int = 10
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_users_blocked, container, false)
        }
        app = context?.applicationContext as App
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        configureEmptyScenario()

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        initApiCallGetBlockedUsers()
        initApiCallCreateBlockedUser()
        initApiCallDeleteBlockedUser()
        initSwipeAndRefreshLayout()
        initRecyclerView()

        if(viewModelGetBlockedUsers.users.size == 0)
            requestNewData()
    }

    private fun configureEmptyScenario() {
        ivEmptyScenario.visibility = View.GONE
        tvTitleEmptyScenario.text = getString(R.string.title_blocked_users)
        tvDescriptionEmptyScenario.text = getString(R.string.empty_scenario_blocked_users)
        tvActionEmptyScenario.visibility = View.GONE
    }

    private fun initToolbar() {
        tvToolbarTitle.text = getString(R.string.title_blocked_users)

        //Toolbar Left
        btnClose.onClick {
            findNavController().popBackStack()
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        rvBlockedUsers?.layoutManager = layoutManager
        blockedUsersAdapter = BlockedUsersAdapter(
            context!!,
            viewModelGetBlockedUsers.users,
            object : BlockedUsersAdapter.OnBlockedUserClickListener {
                override fun onUserClicked(item: UIUser, position: Int) {
                    val userProfileIntent = Intent(context, UserProfileActivity::class.java)
                    userProfileIntent.putExtra("user", item)
                    startActivity(userProfileIntent)
                }

                override fun onBlockClicked(item: UIUser, position: Int) {
                    if(item.blocked) {
                        onUnblockButtonClicked(item)
                    } else {
                        onBlockButtonClicked(item)
                    }
                }

            })

        rvBlockedUsers?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        rvBlockedUsers.adapter = blockedUsersAdapter
        rvBlockedUsers?.setHasFixedSize(false)
    }

    private fun setViewModelVariables() {
        viewModelGetBlockedUsers.offset = viewModelGetBlockedUsers.users.size
    }

    private fun onUnblockButtonClicked(item: UIUser) {
        alert(getString(R.string.confirmation_unblock_user)) {
            okButton {
                performUnblockUser(item)
            }
            cancelButton {  }
        }.show()
    }

    private fun onBlockButtonClicked(item: UIUser) {
        alert(getString(R.string.confirmation_block_user)) {
            okButton {
                performBlockUser(item)
            }
            cancelButton {  }
        }.show()
    }

    private fun performUnblockUser(item: UIUser) {
        viewModelDeleteBlockedUser.user = item
        item.blocked = false
        deleteBlockedUserDataTrigger.onNext(Unit)
    }

    private fun performBlockUser(item: UIUser) {
        viewModelCreateBlockedUser.user = item
        item.blocked = true
        createBlockedUserDataTrigger.onNext(Unit)
    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelGetBlockedUsers = ViewModelProvider(fragmentActivity, viewModelFactory)
                .get(GetBlockedUsersViewModel::class.java)

            viewModelCreateBlockedUser = ViewModelProvider(fragmentActivity, viewModelFactory)
                .get(CreateBlockedUserViewModel::class.java)

            viewModelDeleteBlockedUser = ViewModelProvider(fragmentActivity, viewModelFactory)
                .get(DeleteBlockedUserViewModel::class.java)
        }
    }

    private fun initSwipeAndRefreshLayout() {
        laySwipeRefresh?.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorPrimaryDark
            )
        )

        laySwipeRefresh?.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.brandPrimary500
            )
        )

        laySwipeRefresh?.onRefresh {
            reload()
        }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        isLastPage = false
        viewModelGetBlockedUsers.offset = 0
        hideEmptyScenario()
        viewModelGetBlockedUsers.users.clear()
        rvBlockedUsers?.recycledViewPool?.clear()
        rvBlockedUsers.adapter?.notifyDataSetChanged()
        requestNewData()
    }


    private fun initApiCallGetBlockedUsers() {
        val output = viewModelGetBlockedUsers.transform(
            GetBlockedUsersViewModel.Input(
                getBlockedUsersDataTrigger
            )
        )

        output.response.observe(this, Observer {
            hideProgressBar()
            isRequestingNewData = false
            if (it.code != 0) {
                toast("error")
            } else {
                if(it.data.blocked_users.size > 0) {
                    val previousSize = viewModelGetBlockedUsers.users.size
                    viewModelGetBlockedUsers.users.addAll(it.data.blocked_users)
                    setViewModelVariables()
                    blockedUsersAdapter.notifyItemRangeInserted(previousSize, it.data.blocked_users.size)
                } else {
                    if(viewModelGetBlockedUsers.users.size == 0)
                        showEmptyScenario()

                    if(it.data.blocked_users.size == 0)
                        isLastPage = true
                }
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            hideProgressBar()
            isRequestingNewData = false
            view?.hideKeyboard()
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })
    }

    private fun requestNewData(showProgress : Boolean = true) {
        if (!isRequestingNewData) {
            if(showProgress)
                showProgressBar()
            isRequestingNewData = true
            getBlockedUsersDataTrigger.onNext(Unit)
        }
    }

    private fun showProgressBar() {
        laySwipeRefresh?.let {
            if (!it.isRefreshing) {
                it.isRefreshing = true
            }
        }
    }

    private fun hideProgressBar() {
        laySwipeRefresh?.let {
            if (it.isRefreshing) {
                it.isRefreshing = false
            }
        }
    }

    private fun hideEmptyScenario() {
        layEmptyScenario.visibility = View.GONE
        rvBlockedUsers.visibility = View.VISIBLE
    }

    private fun showEmptyScenario() {
        layEmptyScenario.visibility = View.VISIBLE
        rvBlockedUsers.visibility = View.GONE
    }


    private fun initApiCallDeleteBlockedUser() {

    }

    private fun initApiCallCreateBlockedUser() {

    }

}