package io.square1.limor.scenes.main.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.main.adapters.BlockedUsersAdapter
import io.square1.limor.scenes.main.fragments.profile.UserProfileActivity
import io.square1.limor.scenes.main.viewmodels.CreateBlockedUserViewModel
import io.square1.limor.scenes.main.viewmodels.DeleteBlockedUserViewModel
import io.square1.limor.scenes.main.viewmodels.GetBlockedUsersViewModel
import io.square1.limor.uimodels.UIUser
import kotlinx.android.synthetic.main.fragment_users_blocked.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class BlockedUsersFragment : BaseFragment() {

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

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        initApiCallGetBlockedUsers()
        initApiCallCreateBlockedUser()
        initApiCallDeleteBlockedUser()
        initSwipeAndRefreshLayout()
        initRecyclerView()
        requestNewData()
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
                        onUnblockButtonClicked(item, position)
                    } else {
                        onBlockButtonClicked(item, position)
                    }
                }

            })
        rvBlockedUsers.adapter = blockedUsersAdapter
    }

    private fun onUnblockButtonClicked(item: UIUser, position: Int) {
        alert(getString(R.string.confirmation_unblock_user)) {
            okButton {
                performUnblockUser(item, position)
            }
            cancelButton {  }
        }.show()
    }

    private fun onBlockButtonClicked(item: UIUser, position: Int) {
        alert(getString(R.string.confirmation_block_user)) {
            okButton {
                performBlockUser(item, position)
            }
            cancelButton {  }
        }.show()
    }

    private fun performUnblockUser(item: UIUser, position: Int) {
        viewModelDeleteBlockedUser.user = item
        item.blocked = false
        deleteBlockedUserDataTrigger.onNext(Unit)
    }

    private fun performBlockUser(item: UIUser, position: Int) {
        viewModelCreateBlockedUser.user = item
        item.blocked = true
        createBlockedUserDataTrigger.onNext(Unit)
    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelGetBlockedUsers = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetBlockedUsersViewModel::class.java)

            viewModelCreateBlockedUser = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateBlockedUserViewModel::class.java)

            viewModelDeleteBlockedUser = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
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

    private fun reload() {
        viewModelGetBlockedUsers.users.clear()
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
                viewModelGetBlockedUsers.users.addAll(it.data.blocked_users)
                blockedUsersAdapter.notifyDataSetChanged()

                if(viewModelGetBlockedUsers.users.size == 0)
                    showEmptyScenario()
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            hideProgressBar()
            isRequestingNewData = false
            view?.hideKeyboard()
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
        })
    }

    private fun requestNewData() {
        if (!isRequestingNewData) {
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
        val output = viewModelDeleteBlockedUser.transform(
            DeleteBlockedUserViewModel.Input(
                deleteBlockedUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if(it.code != 0) {
                toast(getString(R.string.error_unblocking_user))
                viewModelDeleteBlockedUser.user?.blocked = true
            }
            blockedUsersAdapter.notifyDataSetChanged()
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()
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
        })
    }

    private fun initApiCallCreateBlockedUser() {
        val output = viewModelCreateBlockedUser.transform(
            CreateBlockedUserViewModel.Input(
                createBlockedUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if(it.code != 0) {
                toast(getString(R.string.error_blocking_user))
                viewModelCreateBlockedUser.user?.blocked = false
            }
            blockedUsersAdapter.notifyDataSetChanged()
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()
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
        })
    }

}