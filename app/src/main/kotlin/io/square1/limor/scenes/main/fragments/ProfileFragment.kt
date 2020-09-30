package io.square1.limor.scenes.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.main.fragments.profile.*
import io.square1.limor.scenes.main.fragments.settings.SettingsActivity
import io.square1.limor.scenes.main.viewmodels.*
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.scenes.utils.CommonsKt.Companion.formatSocialMediaQuantity
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class ProfileFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelCreateUserReport: CreateUserReportViewModel
    private lateinit var viewModelCreateFriend: CreateFriendViewModel
    private lateinit var viewModelDeleteFriend: DeleteFriendViewModel
    private lateinit var viewModelCreateBlockedUser: CreateBlockedUserViewModel
    private lateinit var viewModelDeleteBlockedUser: DeleteBlockedUserViewModel
    private val createUserReportDataTrigger = PublishSubject.create<Unit>()
    private val createFriendDataTrigger = PublishSubject.create<Unit>()
    private val deleteFriendDataTrigger = PublishSubject.create<Unit>()
    private val createBlockedUserDataTrigger = PublishSubject.create<Unit>()
    private val deleteBlockedUserDataTrigger = PublishSubject.create<Unit>()


    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var viewModelGetUser: GetUserViewModel
    private val getUserDataTrigger = PublishSubject.create<Unit>()

    private var tvToolbarUsername: TextView? = null
    private var btnBack: ImageButton? = null
    private var btnSettings: ImageButton? = null
    private var btnMore: ImageButton? = null

    var app: App? = null

    private var isMyProfileMode: Boolean = false


    companion object {
        val TAG: String = ProfileFragment::class.java.simpleName
        fun newInstance() = ProfileFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = context?.applicationContext as App
        initToolbarViews()
        initViewPager()

        isMyProfileMode = checkIfIsMyProfile()

        listeners()
        bindViewModel()
        apiCallGetUser()

        if(!isMyProfileMode) {
            viewModelGetUser.user = (activity as UserProfileActivity).uiUser
            apiCallReportUser()
            apiCallCreateFriend()
            apiCallDeleteFriend()
            apiCallCreateBlockedUser()
            apiCallDeleteBlockedUser()
        } else {
            viewModelGetUser.user = sessionManager.getStoredUser()
        }

        viewModelGetUser.id = viewModelGetUser.user?.id
        getUserDataTrigger.onNext(Unit)

        configureToolbar()
        configureScreen()
        printUserData()
        CommonsKt.reduceSwipeSensitivity(viewPager)
    }

    private fun apiCallDeleteBlockedUser() {
        val output = viewModelDeleteBlockedUser.transform(
            DeleteBlockedUserViewModel.Input(
                deleteBlockedUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if(it.code != 0) {
                toast(getString(R.string.error_unblocking_user))
                viewModelGetUser.user?.blocked = true
            } else {
                toast(getString(R.string.success_unblocking_user))
            }
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

    private fun apiCallCreateBlockedUser() {
        val output = viewModelCreateBlockedUser.transform(
            CreateBlockedUserViewModel.Input(
                createBlockedUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if(it.code != 0) {
                toast(getString(R.string.error_blocking_user))
                viewModelGetUser.user?.blocked = false
            } else {
                toast(getString(R.string.success_blocking_user))
            }
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

    private fun initViewPager() {
        val names = arrayOf("Casts", "Likes", "Not implemented")
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return names.size
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> UserPodcastsFragment.newInstance(viewModelGetUser.user?.id!!)
                    1 -> UserLikedPodcastsFragment.newInstance(viewModelGetUser.user?.id!!)
                    else -> UserPodcastsFragment.newInstance(viewModelGetUser.user?.id!!)
                }
            }

        }
        TabLayoutMediator(tab_layout, viewPager) { tab, position ->
            tab.text = names[position]
        }.attach()

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val myFragment = childFragmentManager.findFragmentByTag(
                        "f" + this@ProfileFragment.viewPager.adapter?.getItemId(it.position)
                    )
                    if (myFragment is FeedItemsListFragment) {
                        myFragment.scrollToTop()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {

            }
        })
    }

    private fun checkIfIsMyProfile(): Boolean {
        return if (activity !is UserProfileActivity) {
            true
        } else {
            val loggedUser = sessionManager.getStoredUser()
            val user = (activity as UserProfileActivity).uiUser
            loggedUser?.id == user?.id
        }
    }

    private fun configureScreen() {
        if (isMyProfileMode) {
            btnSettings?.visibility = View.VISIBLE
            btnMore?.visibility = View.GONE
            layFollows?.visibility = View.GONE
        } else {
            btnSettings?.visibility = View.GONE
            btnMore?.visibility = View.VISIBLE
            layFollows?.visibility = View.VISIBLE
        }
    }

    private fun initToolbarViews() {
        tvToolbarUsername = activity?.findViewById(R.id.tvToolbarUsername)
        btnBack = activity?.findViewById(R.id.btnBack)
        btnSettings = activity?.findViewById(R.id.btnSettings)
        btnMore = activity?.findViewById(R.id.btnMore)
    }

    private fun configureToolbar() {
        if (isMyProfileMode) {
            btnBack?.visibility = View.GONE
        }
    }


    private fun listeners() {

        btnBack?.onClick {
            activity?.finish()
        }

        btnSettings?.onClick {
            toast("You clicked settings")

            val editProfileIntent = Intent(it?.context, SettingsActivity::class.java)
            startActivity(editProfileIntent)



        }

        btnMore?.onClick {
            showPopupMoreMenu()
        }

        btnFollow.onClick {
            viewModelGetUser.user?.followed?.let{
                revertUserFollowedState()

                // if the user is followed now, we must unfollow him
                if (it) {
                    viewModelDeleteFriend.idFriend = viewModelGetUser.user!!.id
                    deleteFriendDataTrigger.onNext(Unit)

                    // if the user is unfollowed now, we must follow him
                } else {
                    viewModelCreateFriend.idNewFriend = viewModelGetUser.user!!.id
                    createFriendDataTrigger.onNext(Unit)
                }
            }
        }
    }


    private fun showPopupMoreMenu() {
        val popup = PopupMenu(context, btnMore, Gravity.TOP)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup_profile, popup.menu)

        val menuBlock = popup.menu.findItem(R.id.menu_block)
        viewModelGetUser.user?.blocked?.let {
            if (it) {
                menuBlock.title = getString(R.string.unblock)
            } else {
                menuBlock.title = getString(R.string.block)
            }
        }

        //set menu item click listener here
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_report -> onReportUserClicked()
                R.id.menu_block -> onBlockUserMenuClicked()
            }
            true
        }
        popup.show()
    }



    private fun onBlockUserMenuClicked() {
        viewModelGetUser.user?.blocked?.let {
            if(it) {
                alert(getString(R.string.confirmation_unblock_user)) {
                    okButton {
                        performUnblockUser()
                    }
                    cancelButton {  }
                }.show()
            } else {
                alert(getString(R.string.confirmation_block_user)) {
                    okButton {
                        performBlockUser()
                    }
                    cancelButton {  }
                }.show()
            }
        }

    }

    private fun performUnblockUser() {
        viewModelGetUser.user?.let {user ->
            viewModelDeleteBlockedUser.idUser = user.id
            user.blocked = false
            deleteBlockedUserDataTrigger.onNext(Unit)
        }
    }

    private fun performBlockUser() {
        viewModelGetUser.user?.let {user ->
            viewModelCreateBlockedUser.idUser = user.id
            user.blocked = true
            createBlockedUserDataTrigger.onNext(Unit)
        }
    }

    private fun onReportUserClicked() {
        val reportUserIntent = Intent(context, ReportActivity::class.java)
        reportUserIntent.putExtra("type", TypeReport.USER)
        startActivityForResult(reportUserIntent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            val reason = data.getStringExtra("reason")
            viewModelCreateUserReport.idUser = viewModelGetUser.user!!.id
            viewModelCreateUserReport.reason = reason
            createUserReportDataTrigger.onNext(Unit)
        }
    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelGetUser = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetUserViewModel::class.java)

            if (!isMyProfileMode) {
                viewModelCreateUserReport = ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(CreateUserReportViewModel::class.java)

                viewModelCreateFriend = ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(CreateFriendViewModel::class.java)

                viewModelDeleteFriend = ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(DeleteFriendViewModel::class.java)

                viewModelCreateBlockedUser = ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(CreateBlockedUserViewModel::class.java)

                viewModelDeleteBlockedUser = ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(DeleteBlockedUserViewModel::class.java)
            }
        }
    }

    private fun apiCallCreateFriend() {
        val output = viewModelCreateFriend.transform(
            CreateFriendViewModel.Input(
                createFriendDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code != 0) {
                revertUserFollowedState()
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()
            revertUserFollowedState()
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


    private fun apiCallDeleteFriend() {
        val output = viewModelDeleteFriend.transform(
            DeleteFriendViewModel.Input(
                deleteFriendDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code != 0) {
                revertUserFollowedState()
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()
            revertUserFollowedState()
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

    private fun revertUserFollowedState() {
        viewModelGetUser.user?.followed?.let {
            if(it) {
                btnFollow.text = getString(R.string.follow)
                viewModelGetUser.user!!.followers_count = viewModelGetUser.user!!.followers_count?.dec()
            } else {
                btnFollow.text = getString(R.string.unfollow)
                viewModelGetUser.user!!.followers_count = viewModelGetUser.user!!.followers_count?.inc()
            }

            tvNumberFollowers.text = formatSocialMediaQuantity(viewModelGetUser.user!!.followers_count!!)
            viewModelGetUser.user?.followed = !it

        }
    }


    private fun apiCallGetUser() {
        val output = viewModelGetUser.transform(
            GetUserViewModel.Input(
                getUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            view?.hideKeyboard()
            viewModelGetUser.user = it.data.user
            printUserData()
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
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



    private fun apiCallReportUser() {
        val output = viewModelCreateUserReport.transform(
            CreateUserReportViewModel.Input(
                createUserReportDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code == 0) {
                toast(getString(R.string.user_reported_ok))
            } else {
                toast(getString(R.string.user_reported_error))
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
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

    private fun printUserData() {
        val fullname = viewModelGetUser.user?.first_name + " " + viewModelGetUser.user?.last_name
        tvToolbarUsername?.text = fullname
        viewModelGetUser.user?.followers_count?.let {
            tvNumberFollowers.text = formatSocialMediaQuantity(it)
        }
        viewModelGetUser.user?.following_count?.let {
            tvNumberFollowing.text = formatSocialMediaQuantity(it)
        }
        context?.let {
            Glide.with(it)
                .load(viewModelGetUser.user?.images?.medium_url)
                .apply(RequestOptions.circleCropTransform())
                .into(ivUser)
        }
        viewModelGetUser.user?.verified?.let {
            if (it)
                ivVerifiedUser.visibility = View.VISIBLE
        }

        if(!isMyProfileMode) {
            viewModelGetUser.user?.followed?.let {
                if(it)
                    btnFollow.text = getString(R.string.unfollow)
                else
                    btnFollow.text = getString(R.string.follow)
            }
        }


        tvBio.text = viewModelGetUser.user?.description
    }

}
