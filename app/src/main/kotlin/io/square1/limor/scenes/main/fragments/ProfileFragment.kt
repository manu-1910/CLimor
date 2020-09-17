package io.square1.limor.scenes.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.main.fragments.profile.UserProfileActivity
import io.square1.limor.scenes.main.fragments.profile.UserReportActivity
import io.square1.limor.scenes.main.viewmodels.CreateUserReportViewModel
import io.square1.limor.scenes.main.viewmodels.GetPodcastCommentsViewModel
import io.square1.limor.scenes.main.viewmodels.LogoutViewModel
import io.square1.limor.scenes.main.viewmodels.ProfileViewModel
import io.square1.limor.scenes.splash.SplashActivity
import io.square1.limor.scenes.utils.CommonsKt.Companion.formatSocialMediaQuantity
import io.square1.limor.uimodels.UIUser
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.ivVerifiedUser
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class ProfileFragment : BaseFragment() {


    private var uiUser: UIUser? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelCreateUserReport: CreateUserReportViewModel
    private val createUserReportDataTrigger = PublishSubject.create<Unit>()



    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var viewModelProfile: ProfileViewModel
    private lateinit var viewModelLogout: LogoutViewModel
    private val getUserDataTrigger = PublishSubject.create<Unit>()
    private val logoutTrigger = PublishSubject.create<Unit>()

    private var tvToolbarUsername: TextView? = null
    private var btnBack: ImageButton? = null
    private var btnSettings: ImageButton? = null
    private var btnMore: ImageButton? = null

    var app: App? = null

    private var isMyProfileMode : Boolean = false


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

        listeners()
        bindViewModel()
        apiCallGetUser()
        apiCallLogout()

        if(activity is UserProfileActivity) {
            isMyProfileMode = false
            uiUser = (activity as UserProfileActivity).uiUser
            apiCallReportUser()
        } else {
            isMyProfileMode = true
            uiUser = sessionManager.getStoredUser()
            getUserDataTrigger.onNext(Unit)
        }
        printUserData()
        configureToolbar()
        configureScreen()
    }

    private fun configureScreen() {
        if(isMyProfileMode) {
            btnLogout.visibility = View.VISIBLE
            btnSettings?.visibility = View.VISIBLE
            btnMore?.visibility = View.GONE
            layFollows?.visibility = View.GONE
        } else {
            btnLogout.visibility = View.GONE
            btnSettings?.visibility = View.GONE
            btnMore?.visibility = View.VISIBLE
            layFollows?.visibility = View.VISIBLE
            uiUser?.followed?.let {
                if(it)
                    btnFollow.text = getString(R.string.unfollow)
                else
                    btnFollow.text = getString(R.string.follow)
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
        if(isMyProfileMode) {
            btnBack?.visibility = View.GONE
        }
    }


    private fun listeners() {
        btnLogout.onClick {
            logoutTrigger.onNext(Unit)
            logOutFromFacebook()
        }

        btnBack?.onClick {
            activity?.finish()
        }

        btnSettings?.onClick {
            toast("You clicked settings")
        }

        btnMore?.onClick {
            showPopupMoreMenu()
        }

        btnFollow?.onClick {

        }
    }


    private fun showPopupMoreMenu() {
        val popup = PopupMenu(context, btnMore, Gravity.TOP)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup_profile, popup.menu)


        //set menu item click listener here
        popup.setOnMenuItemClickListener {menuItem ->
            when(menuItem.itemId) {
                R.id.menu_report -> onReportUserClicked()
                R.id.menu_block -> onBlockUserClicked()
            }
            true
        }
        popup.show()
    }

    private fun onBlockUserClicked() {
        toast("You clicked on block")
    }

    private fun onReportUserClicked() {
        val reportUserIntent = Intent(context, UserReportActivity::class.java)
        startActivityForResult(reportUserIntent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            val reason = data.getStringExtra("reason")
            viewModelCreateUserReport.idUser = uiUser!!.id
            viewModelCreateUserReport.reason = reason
            createUserReportDataTrigger.onNext(Unit)
        }
    }




    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelProfile = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(ProfileViewModel::class.java)

            viewModelLogout = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(LogoutViewModel::class.java)

            if(!isMyProfileMode) {
                viewModelCreateUserReport = ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(CreateUserReportViewModel::class.java)
            }
        }
    }


    private fun apiCallGetUser() {
        val output = viewModelProfile.transform(
            ProfileViewModel.Input(
                getUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            view?.hideKeyboard()
            uiUser = it.data.user
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
            if(it.code == 0) {
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


    private fun apiCallLogout() {
        val output = viewModelLogout.transform(
            LogoutViewModel.Input(
                logoutTrigger
            )
        )

        output.response.observe(this, Observer {
            //pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()

            //if (it.message == "Success") { //TODO review why message is errormessage and is null
            if (it.code == 0) {

                sessionManager.logOut()

                val mainIntent = Intent(context, SplashActivity::class.java)
                startActivity(mainIntent)
                try {
                    activity?.finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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


    private fun logOutFromFacebook() { //TODO
        // Logout
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE,
                GraphRequest.Callback {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()
                }).executeAsync()
        }
    }


    private fun printUserData() {
        val fullname = uiUser?.first_name + " " + uiUser?.last_name
        tvToolbarUsername?.text = fullname
        uiUser?.followers_count?.let {
            tvNumberFollowers.text = formatSocialMediaQuantity(it)
        }
        uiUser?.following_count?.let {
            tvNumberFollowing.text = formatSocialMediaQuantity(it)
        }
        context?.let {
            Glide.with(it)
                .load(uiUser?.images?.medium_url)
                .into(ivUser)
        }
        uiUser?.verified?.let {
            if (it)
                ivVerifiedUser.visibility = View.VISIBLE
        }


        tvBio.text = uiUser?.description
    }

}
