package com.limor.app.scenes.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.SessionManager
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.fragments.profile.adapters.UserFollowingsAdapter
import com.limor.app.scenes.main.viewmodels.CreateFriendViewModel
import com.limor.app.scenes.main.viewmodels.DeleteFriendViewModel
import com.limor.app.scenes.main.viewmodels.GetUserFollowingsViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIUser
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_user_followings.*
import javax.inject.Inject


class UserFollowingsFragment(private val uiUser: UIUser) : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sessionManager: SessionManager

    var app: App? = null

    private lateinit var viewModelFollowings: GetUserFollowingsViewModel
    private lateinit var viewModelCreateFriend: CreateFriendViewModel
    private lateinit var viewModelDeleteFriend: DeleteFriendViewModel

    private val getFollowingsTrigger = PublishSubject.create<Unit>()
    private val createFriendDataTrigger = PublishSubject.create<Unit>()
    private val deleteFriendDataTrigger = PublishSubject.create<Unit>()

    private var followingsAdapter: UserFollowingsAdapter? = null
    protected var rootView: View? = null

    // views
    private var rvFollowings: RecyclerView? = null
    private var tvNoFollowings: TextView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    // infinite scroll variables
    private var isScrolling: Boolean = false
    private var isLastPage: Boolean = false


    companion object {
        val TAG: String = UserFollowingsFragment::class.java.simpleName
        fun newInstance(user: UIUser) = UserFollowingsFragment(user)
        private const val OFFSET_INFINITE_SCROLL = 2
        private const val FEED_LIMIT_REQUEST = 15
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_user_followings, container, false)

            rvFollowings = rootView?.findViewById(R.id.rv_followings)
            tvNoFollowings = rootView?.findViewById(R.id.tv_no_followings)
            swipeRefreshLayout = rootView?.findViewById(R.id.swipeRefreshLayout_followings)

            bindViewModel()
            initApiCallGetFollowings()
            apiCallCreateFriend()
            apiCallDeleteFriend()
            configureAdapter()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = context?.applicationContext as App

        initSwipeRefreshLayout()

        if (followingsAdapter?.list?.size == 0) {
            getFollowingsTrigger.onNext(Unit)
        } else {
            showProgress(false)
        }

    }


    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout?.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorPrimaryDark
            )
        )

        swipeRefreshLayout?.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.brandPrimary500
            )
        )

        swipeRefreshLayout?.setOnRefreshListener {
            isLastPage = false
            followingsAdapter?.list?.clear()
            swipeRefreshLayout?.isRefreshing = true

            viewModelFollowings.offset = 0
            getFollowingsTrigger.onNext(Unit)
        }

        /*
        swipeRefreshLayout?.setOnRefreshListener {
    isLastPage = false
    listPublications.clear()
    swipeRefreshLayout?.isRefreshing = true
    //listener?.onRefreshPublications()
    //Reset saved filter models page to num 1
    val filters = preferences.getFiltersModel()
    filters.keyWords = ""
    filters.page = 1
    preferences.setFiltersModel(filters) //Save filtersmodel again in preferences with the page num updated


    closePresenter.getPublicationsClose(locationResult, filters)
}

         */
    }

    private fun initApiCallGetFollowings() {
        val output = viewModelFollowings.transform(
            GetUserFollowingsViewModel.Input(
                getFollowingsTrigger, uiUser.id
            )
        )



    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelFollowings = ViewModelProvider(fragmentActivity, viewModelFactory)
                .get(GetUserFollowingsViewModel::class.java)

            viewModelCreateFriend = ViewModelProvider(fragmentActivity, viewModelFactory)
                .get(CreateFriendViewModel::class.java)

            viewModelDeleteFriend = ViewModelProvider(fragmentActivity, viewModelFactory)
                .get(DeleteFriendViewModel::class.java)
       }
    }


    private fun showProgress(show: Boolean) {
        if (show) {
            pb_followings.visibility = View.VISIBLE
        } else {
            swipeRefreshLayout?.isRefreshing = false
            pb_followings.visibility = View.INVISIBLE
        }
    }


    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvFollowings?.layoutManager = layoutManager


    }


    private fun setNotificationViewModelVariables(newOffset: Int = 0) {
        viewModelFollowings.limit = FEED_LIMIT_REQUEST
        viewModelFollowings.offset = newOffset
    }


    private fun apiCallCreateFriend() {
        val output = viewModelCreateFriend.transform(
            CreateFriendViewModel.Input(
                createFriendDataTrigger
            )
        )

        /*output.response.observe(this, Observer {
            if (it.code != 0) {
                revertUserFollowedState()
            }else{
                showProgress(false)
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
        })*/
    }

    private fun apiCallDeleteFriend() {
        val output = viewModelDeleteFriend.transform(
            DeleteFriendViewModel.Input(
                deleteFriendDataTrigger
            )
        )

       /* output.response.observe(this, Observer {
            if (it.code != 0) {
                revertUserFollowedState()
            }else{
                showProgress(false)
            }
        })*/

        /*output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()
            revertUserFollowedState()
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })*/
    }


    private fun revertUserFollowedState() {
//        viewModelGetUser.user?.followed?.let {
//            if (it) {
//                viewModelGetUser.user!!.followers_count =
//                    viewModelGetUser.user!!.followers_count?.dec()
//            } else {
//                viewModelGetUser.user!!.followers_count =
//                    viewModelGetUser.user!!.followers_count?.inc()
//            }
//
//            tvNumberFollowers.text =
//                CommonsKt.formatSocialMediaQuantity(viewModelGetUser.user!!.followers_count!!)
//            viewModelGetUser.user?.followed = !it
//            setStyleToFollowButton()
//        }

        showProgress(false)
    }


    private fun goToUserProfile(item: UIUser){

        if (item.id == sessionManager.getStoredUser()?.id) {
            if(activity is MainActivity)
                findNavController().navigate(R.id.navigation_profile)
            else if(activity is UserProfileActivity) {
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.putExtra("destination", "profile")
                startActivity(intent)
                // TODO -> Jose: you are in anothers person profile in UserProfileActivity
                //      and you clicked in you own user, so now you have to show your own profile
                //      but if you open a new UserProfileActivity with your user, you won't be able
                //      to navigate to the actions of the empty views, because you are not in the
                //      main activity. For example, if you don't have any like, you won't be able
                //      to navigate to discover fragment from UserProfileActivity
                //      Maybe the right choice could be to navigate to mainActivity
                //      and to show your profile there ¿?
                //      Waiting for Martin's answer
            }
        } else {
            val userProfileIntent = Intent(context, UserProfileActivity::class.java)
            userProfileIntent.putExtra("user", item)
            startActivity(userProfileIntent)
        }

    }


}
