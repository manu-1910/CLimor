package com.limor.app.scenes.main.fragments.settings

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
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentUsersBlockedBinding
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.settings.adapters.AdapterBlockedUsers
import com.limor.app.scenes.main.viewmodels.GetBlockedUsersViewModel
import kotlinx.android.synthetic.main.fragment_users_blocked.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.yesButton
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

class BlockedUsersFragment : BaseFragment() {

    private var isLastPage: Boolean = false
    private var isScrolling: Boolean = false
    private var isRequestingNewData: Boolean = false
    private lateinit var arrayList: ArrayList<GetBlockedUsersQuery.GetBlockedUser?>


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val  model: SettingsViewModel by viewModels({activity as SettingsActivity}) { viewModelFactory }

    private lateinit var binding: FragmentUsersBlockedBinding
    var app: App? = null

    private lateinit var blockedUsersAdapter: AdapterBlockedUsers

    private lateinit var viewModelGetBlockedUsers: GetBlockedUsersViewModel



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
        binding = FragmentUsersBlockedBinding.inflate(inflater,container,false)

        app = context?.applicationContext as App
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        configureEmptyScenario()
        arrayList = ArrayList()
        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        initApiCallGetBlockedUsers()
        initApiCallCreateBlockedUser()
        initApiCallDeleteBlockedUser()
        initSwipeAndRefreshLayout()
        initRecyclerView()
        model.blockedUsersData.observe(viewLifecycleOwner, Observer {
            Timber.d("blocked users observe -> $it")
            if(it?.size == 0){
                showEmptyScenario()
            }else{
                //switchCommonVisibility(false)
                isRequestingNewData = false
                val adapter = ( binding.rvBlockedUsers.adapter as AdapterBlockedUsers)
                arrayList.addAll(it!!)
                adapter.notifyDataSetChanged()
                hideEmptyScenario()
            }

            hideProgressBar()
        })




    }

    /*override fun load() {
        reload()
    }

    override val errorLiveData: LiveData<String>
        get() = model.blockedUserErrorLiveData*/

    private fun configureEmptyScenario() {
        binding.layEmptyScenario.ivEmptyScenario.visibility = View.GONE
        binding.layEmptyScenario.tvTitleEmptyScenario.text = getString(R.string.empty_scenario_blocked_users)
        binding.layEmptyScenario.tvTitleEmptyScenario.textSize = 14f
        binding.layEmptyScenario.tvDescriptionEmptyScenario.text = ""
        binding.layEmptyScenario.tvActionEmptyScenario.visibility = View.GONE
    }

    private fun initToolbar() {
        model.setToolbarTitle(resources.getString(R.string.title_blocked_users))
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.rvBlockedUsers.layoutManager = layoutManager
        blockedUsersAdapter = AdapterBlockedUsers(arrayList,
            object : AdapterBlockedUsers.OnFollowerClickListener {
                override fun onUserClicked(
                    item: GetBlockedUsersQuery.GetBlockedUser,
                    position: Int
                ) {
                    val userProfileIntent = Intent(context, UserProfileActivity::class.java)
                    userProfileIntent.putExtra("user", item.username)
                    startActivity(userProfileIntent)
                }

                override fun onBlockClicked(
                    item: GetBlockedUsersQuery.GetBlockedUser,
                    position: Int
                ) {
                    if(item.blocked!!) {
                        onUnblockButtonClicked(item,position)
                    } else {
                        onBlockButtonClicked(item,position)
                    }
                }

                override fun onUserLongClicked(
                    item: GetBlockedUsersQuery.GetBlockedUser,
                    position: Int
                ) {

                }


            })

        binding.rvBlockedUsers?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        binding.rvBlockedUsers?.setHasFixedSize(false)
    }

    private fun setViewModelVariables() {

    }

    private fun onUnblockButtonClicked(item: GetBlockedUsersQuery.GetBlockedUser, position: Int) {
        alert(getString(R.string.confirmation_unblock_user)) {
            yesButton {
                performUnblockUser(item)
                blockedUsersAdapter.updateItem(item, position )
            }
            cancelButton {  }
        }.show()
    }

    private fun onBlockButtonClicked(item: GetBlockedUsersQuery.GetBlockedUser, position: Int) {
        alert(getString(R.string.confirmation_block_user)) {
            yesButton {
                performBlockUser(item)
                blockedUsersAdapter.updateItem(item, position )
            }
            cancelButton {  }
        }.show()
    }

    private fun performUnblockUser(item: GetBlockedUsersQuery.GetBlockedUser) {

    }

    private fun performBlockUser(item: GetBlockedUsersQuery.GetBlockedUser) {

    }

    private fun bindViewModel() {

    }

    private fun initSwipeAndRefreshLayout() {
        binding.laySwipeRefresh?.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorPrimaryDark
            )
        )

        binding.laySwipeRefresh?.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.brandPrimary500
            )
        )

        binding.laySwipeRefresh?.onRefresh {
            reload()
        }
    }


    private fun reload() {
        isLastPage = false
        model.blockUsersOffset = 0
        arrayList.clear()
        hideEmptyScenario()
        showProgressBar()
        model.clearBlockedUsers()
        rvBlockedUsers?.recycledViewPool?.clear()
        rvBlockedUsers.adapter?.notifyDataSetChanged()
        requestNewData()
    }


    private fun initApiCallGetBlockedUsers() {
        if(model.blockedUsersData.value == null || model.blockedUsersData.value?.size == 0){
            model.getBlockedUsers(arrayList.size)
        }else{
            model.clearBlockedUsers()
            model.getBlockedUsers(0)
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
        binding.layEmptyScenario.root.visibility = View.GONE
        binding.rvBlockedUsers.visibility = View.VISIBLE
    }

    private fun showEmptyScenario() {

        binding.layEmptyScenario.root.visibility = View.VISIBLE
        binding.rvBlockedUsers.visibility = View.GONE
    }


    private fun initApiCallDeleteBlockedUser() {

    }

    private fun initApiCallCreateBlockedUser() {

    }

}