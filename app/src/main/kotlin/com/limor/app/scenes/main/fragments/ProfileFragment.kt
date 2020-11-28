package com.limor.app.scenes.main.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.SessionManager
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.fragments.profile.*
import com.limor.app.scenes.main.fragments.settings.SettingsActivity
import com.limor.app.scenes.main.viewmodels.*
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.CommonsKt.Companion.formatSocialMediaQuantity
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class ProfileFragment : BaseFragment() {


    private var rootView: View? = null

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
        private const val REQUEST_SETTINGS: Int = 0
        private const val REQUEST_REPORT_USER: Int = 1
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_profile, container, false)

            app = context?.applicationContext as App

            isMyProfileMode = checkIfIsMyProfile()

            bindViewModel()
            apiCallGetUser()

            if (!isMyProfileMode) {
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


        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbarViews()

        listeners()
        initViewPager()
        configureToolbar()
        configureScreen()
        printUserData()

    }

    private fun apiCallDeleteBlockedUser() {
        val output = viewModelDeleteBlockedUser.transform(
            DeleteBlockedUserViewModel.Input(
                deleteBlockedUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code != 0) {
                toast(getString(R.string.error_unblocking_user))
                viewModelGetUser.user?.blocked = true
                setStyleToBlockButton()
            } else {
                viewModelGetUser.user?.followed = false
                setStyleToFollowButton()
                initViewPager()
                configureScreen()
                getUserDataTrigger.onNext(Unit)
                toast(getString(R.string.success_unblocking_user))
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })
    }

    private fun apiCallCreateBlockedUser() {
        val output = viewModelCreateBlockedUser.transform(
            CreateBlockedUserViewModel.Input(
                createBlockedUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code != 0) {
                toast(getString(R.string.error_blocking_user))
                viewModelGetUser.user?.blocked = false
                setStyleToBlockButton()
            } else {
                initViewPager()
                configureScreen()
                toast(getString(R.string.success_blocking_user))
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })
    }

    private fun initViewPager() {
        if(viewModelGetUser.user?.blocked == true) {
            layViewPager?.visibility = View.GONE
        } else {
            layViewPager?.visibility = View.VISIBLE
            val names = arrayOf("Casts", "Likes")

            val adapter = object : FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                override fun getItem(position: Int): Fragment {
                    return when (position) {
                        0 -> UserPodcastsFragment.newInstance(viewModelGetUser.user?.id!!)
                        1 -> UserLikedPodcastsFragment.newInstance(viewModelGetUser.user?.id!!)
                        else -> UserPodcastsFragment.newInstance(viewModelGetUser.user?.id!!)
                    }
                }

                override fun getCount() : Int {
                    return names.size
                }

                override fun getPageTitle(position: Int): CharSequence {
                    return names[position]
                }

                // this is necessary. Without this, app will crash when you are in a different fragment
                // and then push back and it goes back to this fragment.
                // the fragmentstatepageradapter saves states between different fragments of the adapter itself
                // but if you go to a different fragment, for example home, and the push back and the navigation
                // goes back to this profile fragment, the fragmentstatepageradapter will try to restore the
                // state of the adapter fragments but they are not alive anymore.
                override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
                    try {
                        super.restoreState(state, loader)
                    } catch (e: Exception) {
//                        Timber.e("Error Restore State of Fragment : %s", e.message)
                    }
                }
            }
            viewPager?.adapter = adapter

            tab_layout?.setupWithViewPager(viewPager)
            tab_layout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                    tab?.let {
                        val myFragment = adapter.instantiateItem(viewPager, it.position)
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
            if(viewModelGetUser.user?.blocked == true) {
                layFollows?.visibility = View.GONE
                btnBlock?.visibility = View.VISIBLE
                setStyleToBlockButton()
            } else {
                layFollows?.visibility = View.VISIBLE
                btnBlock?.visibility = View.GONE
            }
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

        btnBlock?.onClick {
            onBlockClicked()
        }

        btnBack?.onClick {
            activity?.finish()
        }

        btnSettings?.onClick {
            val editProfileIntent = Intent(it?.context, SettingsActivity::class.java)
            startActivityForResult(editProfileIntent, REQUEST_SETTINGS)
        }

        btnMore?.onClick {
            showPopupMoreMenu()
        }

        btnFollow?.onClick {
            viewModelGetUser.user?.followed?.let {
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
                R.id.menu_block -> onBlockClicked()
            }
            true
        }
        popup.show()
    }


    private fun onBlockClicked() {
        viewModelGetUser.user?.blocked?.let {
            if (it) {
                alert(getString(R.string.confirmation_unblock_user)) {
                    okButton {
                        performUnblockUser()
                    }
                    cancelButton { }
                }.show()
            } else {
                alert(getString(R.string.confirmation_block_user)) {
                    okButton {
                        performBlockUser()
                    }
                    cancelButton { }
                }.show()
            }
        }
    }

    private fun setStyleToBlockButton() {
        if(viewModelGetUser.user?.blocked == true) {
            CommonsKt.setButtonLimorStylePressed(btnBlock, false, R.string.block, R.string.unblock)
        } else {
            CommonsKt.setButtonLimorStylePressed(btnBlock, true, R.string.block, R.string.unblock)
        }
    }



    private fun setStyleToFollowButton() {
        if(viewModelGetUser.user?.followed == true) {
            CommonsKt.setButtonLimorStylePressed(btnFollow, false, R.string.follow, R.string.unfollow)
        } else {
            CommonsKt.setButtonLimorStylePressed(btnFollow, true, R.string.follow, R.string.unfollow)
        }
    }

    private fun performUnblockUser() {
        viewModelGetUser.user?.let { user ->
            viewModelDeleteBlockedUser.user = user
            user.blocked = false
            setStyleToBlockButton()
            deleteBlockedUserDataTrigger.onNext(Unit)
        }
    }

    private fun performBlockUser() {
        viewModelGetUser.user?.let { user ->
            viewModelCreateBlockedUser.user = user
            user.blocked = true
            setStyleToBlockButton()
            createBlockedUserDataTrigger.onNext(Unit)
        }
    }

    private fun onReportUserClicked() {
        val reportUserIntent = Intent(context, ReportActivity::class.java)
        reportUserIntent.putExtra("type", TypeReport.USER)
        startActivityForResult(reportUserIntent, REQUEST_REPORT_USER)
    }

    override fun onResume() {
        super.onResume()
        viewModelGetUser.user?.let {
            getUserDataTrigger.onNext(Unit)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_REPORT_USER -> {
                    data?.let {
                        val reason = data.getStringExtra("reason")
                        viewModelCreateUserReport.idUser = viewModelGetUser.user!!.id
                        viewModelCreateUserReport.reason = reason
                        createUserReportDataTrigger.onNext(Unit)
                    }
                }
                REQUEST_SETTINGS -> {
                    getUserDataTrigger.onNext(Unit)
                }
            }
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
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
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
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })
    }

    private fun revertUserFollowedState() {
        viewModelGetUser.user?.followed?.let {
            if (it) {
                viewModelGetUser.user!!.followers_count =
                    viewModelGetUser.user!!.followers_count?.dec()
            } else {
                viewModelGetUser.user!!.followers_count =
                    viewModelGetUser.user!!.followers_count?.inc()
            }

            tvNumberFollowers.text =
                formatSocialMediaQuantity(viewModelGetUser.user!!.followers_count!!)
            viewModelGetUser.user?.followed = !it
            setStyleToFollowButton()
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
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
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
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })
    }

    private fun printUserData() {
//        var firstName = ""
//        viewModelGetUser.user?.first_name?.let {
//            firstName = it
//        }
//        var lastName = ""
//        viewModelGetUser.user?.last_name?.let {
//            lastName = it
//        }
//        val fullname = "$firstName $lastName".trim()
        tvToolbarUsername?.text = viewModelGetUser.user?.username
        viewModelGetUser.user?.followers_count?.let {
            tvNumberFollowers?.text = formatSocialMediaQuantity(it)
        }
        viewModelGetUser.user?.following_count?.let {
            tvNumberFollowing?.text = formatSocialMediaQuantity(it)
        }
        context?.let {
            Glide.with(it)
                .load(viewModelGetUser.user?.images?.medium_url)
                .apply(RequestOptions.circleCropTransform())
                .into(ivUser)
        }
        viewModelGetUser.user?.verified?.let {
            if (it)
                ivVerifiedUser?.visibility = View.VISIBLE
        }

        if (!isMyProfileMode) {
            setStyleToFollowButton()
        }


        tvBio?.text = viewModelGetUser.user?.description
    }

}
