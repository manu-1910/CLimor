package com.limor.app.scenes.main.fragments.discover

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.AbsListView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.common.BaseFragment
import com.limor.app.common.SessionManager
import com.limor.app.components.GridSpacingItemDecoration
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.adapters.PodcastsGridAdapter
import com.limor.app.scenes.main.fragments.podcast.PodcastDetailsActivity
import com.limor.app.scenes.main.fragments.profile.ReportActivity
import com.limor.app.scenes.main.fragments.profile.TypeReport
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.viewmodels.*
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.service.AudioService
import com.limor.app.uimodels.UICategory
import com.limor.app.uimodels.UIPodcast
import com.limor.app.uimodels.UIUser
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_empty_scenario.*
import kotlinx.android.synthetic.main.fragment_podcasts_by_category.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class DiscoverPodcastsByCategoryFragment : BaseFragment() {

    private var isLastPage = false
    private var isRequestingNewData = false
    private var isScrolling = false
    private var rootView: View? = null
    var app: App? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var viewModelGetPodcastsByCategory: GetPodcastsByCategoryViewModel

    private val getPodcastsDataTrigger = PublishSubject.create<Unit>()

    private lateinit var category: UICategory
    private var podcasts: ArrayList<UIPodcast> = ArrayList()
    private lateinit var podcastsGridAdapter: PodcastsGridAdapter

    private lateinit var viewModelCreatePodcastReport: CreatePodcastReportViewModel
    private lateinit var viewModelCreateUserReport: CreateUserReportViewModel
    private lateinit var viewModelCreateBlockedUser: CreateBlockedUserViewModel
    private lateinit var viewModelDeletePodcast: DeletePodcastViewModel
    private val createPodcastReportDataTrigger = PublishSubject.create<Unit>()
    private val createUserReportDataTrigger = PublishSubject.create<Unit>()
    private val createBlockedUserDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastDataTrigger = PublishSubject.create<Unit>()

    private var lastPodcastDeletedPosition = 0


    companion object {
        val TAG: String = DiscoverPodcastsByCategoryFragment::class.java.simpleName
        fun newInstance() = DiscoverPodcastsByCategoryFragment()
        private const val OFFSET_INFINITE_SCROLL: Int = 10
        private const val REQUEST_REPORT_PODCAST = 1
        private const val REQUEST_REPORT_USER = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_podcasts_by_category, container, false)

            app = context?.applicationContext as App
        }
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        activity?.intent?.extras?.let {
            category = it.getSerializable("category") as UICategory
            bindViewModel()
            configureToolbar()
            configureEmptyScenario()
            initApiCallGetPodcasts()
            initApiCallCreatePodcastReport()
            initApiCallCreateUserReport()
            initApiCallCreateBlockedUser()
            initApiCallDeletePodcast()
            initRecyclerView()
            initSwipeAndRefreshLayout()
            showEmptyScenario(true)
            showProgress(true)
            getPodcastsDataTrigger.onNext(Unit)
        } ?: run {
            alert(getString(R.string.error_getting_category)) {
                okButton {
                    activity?.finish()
                }
            }
        }
    }

    private fun configureEmptyScenario() {
        tvActionEmptyScenario.visibility = View.GONE
        ivEmptyScenario.visibility = View.GONE
        tvTitleEmptyScenario.text = category.name
        tvDescriptionEmptyScenario.text = getString(R.string.no_content_with_this_category)
    }

    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = category.name

        //Toolbar Left
        btnClose.onClick {
            activity?.finish()
        }
    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelGetPodcastsByCategory = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetPodcastsByCategoryViewModel::class.java)

            viewModelCreatePodcastReport = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastReportViewModel::class.java)

            viewModelCreateUserReport = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateUserReportViewModel::class.java)

            viewModelCreateBlockedUser = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateBlockedUserViewModel::class.java)

            viewModelDeletePodcast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastViewModel::class.java)
        }
    }


    private fun initRecyclerView() {
        val layoutManager = GridLayoutManager(context, 2)

        rvPodcasts?.layoutManager = layoutManager
        podcastsGridAdapter = PodcastsGridAdapter(
            context!!,
            podcasts,
            object : PodcastsGridAdapter.OnPodcastClickListener {
                override fun onItemClicked(item: UIPodcast, position: Int) {
                    val podcastDetailsIntent =
                        Intent(context, PodcastDetailsActivity::class.java)
                    podcastDetailsIntent.putExtra("podcast", item)
                    podcastDetailsIntent.putExtra("position", position)
                    startActivity(podcastDetailsIntent)
                }

                override fun onPlayClicked(item: UIPodcast, position: Int) {
                    AudioService.newIntent(requireContext(), item, 1L)
                        .also { intent ->
                            requireContext().startService(intent)
                            val activity = requireActivity() as BaseActivity
                            activity.showMiniPlayer()
                        }
                }

                override fun onUserClicked(item: UIPodcast, position: Int) {
                    if (item.user.id == sessionManager.getStoredUser()?.id) {
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        intent.putExtra("destination", "profile")
                        startActivity(intent)
                    } else {
                        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
                        userProfileIntent.putExtra("user", item.user)
                        startActivity(userProfileIntent)
                    }
                }

                override fun onMoreClicked(item: UIPodcast, position: Int, view: View) {
                    showPopupMenu(view, item, position)
                }

            })

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return podcastsGridAdapter.getSpanByPosition(position)
            }
        }
        rvPodcasts?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        rvPodcasts.adapter = podcastsGridAdapter
        rvPodcasts?.setHasFixedSize(false)

        val dip = 16f
        val r: Resources = resources
        val pxMedium = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        )
        rvPodcasts?.addItemDecoration(GridSpacingItemDecoration(pxMedium.toInt()))
    }


    private fun initApiCallCreateBlockedUser() {
        val output = viewModelCreateBlockedUser.transform(
            CreateBlockedUserViewModel.Input(
                createBlockedUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code != 0) {
                toast(getString(R.string.error_blocking_user))
                viewModelCreateBlockedUser.user?.blocked = false
            } else {
                reload()
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

    private fun initApiCallCreatePodcastReport() {
        val output = viewModelCreatePodcastReport.transform(
            CreatePodcastReportViewModel.Input(
                createPodcastReportDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                Toast.makeText(
                    context,
                    getString(R.string.podcast_already_reported),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.podcast_reported_ok),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        output.errorMessage.observe(this, Observer {
            Toast.makeText(
                context,
                getString(R.string.error_report),
                Toast.LENGTH_SHORT
            ).show()
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })
    }

    private fun showPopupMenu(
        view: View?,
        item: UIPodcast,
        position: Int
    ) {
        val popup = PopupMenu(context, view, Gravity.TOP)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup_podcast, popup.menu)

        val loggedUser = sessionManager.getStoredUser()
        if (item.user.id != loggedUser?.id) {
            val menuToHide = popup.menu.findItem(R.id.menu_delete_cast)
            menuToHide.isVisible = false
        } else {
            val menuBlock = popup.menu.findItem(R.id.menu_block_user)
            menuBlock.isVisible = false
            val menuReportUser = popup.menu.findItem(R.id.menu_report_user)
            menuReportUser.isVisible = false
            val menuReportCast = popup.menu.findItem(R.id.menu_report_cast)
            menuReportCast.isVisible = false
        }

        //set menu item click listener here
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_share -> onShareClicked(item)
                R.id.menu_report_cast -> onPodcastReportClicked(item)
                R.id.menu_report_user -> onUserReportClicked(item)
                R.id.menu_delete_cast -> onDeletePodcastClicked(item, position)
                R.id.menu_block_user -> onBlockUserClicked(item)
            }
            true
        }
        popup.show()
    }

    private fun onBlockUserClicked(item: UIPodcast) {
        alert(getString(R.string.confirmation_block_user)) {
            okButton {
                performBlockUser(item.user)
            }
            cancelButton { }
        }.show()
    }


    private fun performBlockUser(user: UIUser?) {
        user?.let {
            viewModelCreateBlockedUser.user = it
            it.blocked = true
            createBlockedUserDataTrigger.onNext(Unit)
        }
    }

    private fun onDeletePodcastClicked(item: UIPodcast, position: Int) {
        alert(getString(R.string.confirmation_delete_podcast)) {
            okButton {
                lastPodcastDeletedPosition = position
                viewModelDeletePodcast.podcast = item
                deletePodcastDataTrigger.onNext(Unit)
            }
            cancelButton { }
        }.show()
    }

    private fun onUserReportClicked(item: UIPodcast) {
        item.user.let {
            viewModelCreateUserReport.idUser = it.id
            val reportIntent = Intent(context, ReportActivity::class.java)
            reportIntent.putExtra("type", TypeReport.USER)
            startActivityForResult(reportIntent, REQUEST_REPORT_USER)
        }
    }

    private fun onPodcastReportClicked(item: UIPodcast) {
        item.id.let {
            viewModelCreatePodcastReport.idPodcastToReport = it
            val reportIntent = Intent(context, ReportActivity::class.java)
            reportIntent.putExtra("type", TypeReport.CAST)
            startActivityForResult(reportIntent, REQUEST_REPORT_PODCAST)
        }
    }

    private fun onShareClicked(item: UIPodcast) {
        item.sharing_url?.let { url ->
            val text = getString(R.string.check_out_this_cast)
            val finalText = "$text $url"
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, finalText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        } ?: run {
            toast(getString(R.string.error_retrieving_sharing_url))
        }
    }

    private fun requestNewData(showProgress: Boolean) {
        if (showProgress)
            showProgress(true)
        getPodcastsDataTrigger.onNext(Unit)
    }

    private fun initSwipeAndRefreshLayout() {
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

        swipeRefreshLayout?.onRefresh {
            reload()
        }
    }


    private fun reload() {
        isLastPage = false
        viewModelGetPodcastsByCategory.offset = 0
        podcasts.clear()
        rvPodcasts?.recycledViewPool?.clear()
        rvPodcasts.adapter?.notifyDataSetChanged()
        requestNewData(true)
    }

    private fun showProgress(show: Boolean) {
//        swipeRefreshLayout.isRefreshing = show
        progressBar?.visibility = if(show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun showEmptyScenario(show: Boolean) {
        layEmptyScenario.visibility = when (show) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun setViewModelVariables() {
        viewModelGetPodcastsByCategory.offset = podcasts.size
    }


    private fun initApiCallCreateUserReport() {
        val output = viewModelCreateUserReport.transform(
            CreateUserReportViewModel.Input(
                createUserReportDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                Toast.makeText(
                    context,
                    getString(R.string.user_reported_error),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.user_reported_ok),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        output.errorMessage.observe(this, Observer {
            Toast.makeText(
                context,
                getString(R.string.error_report),
                Toast.LENGTH_SHORT
            ).show()
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })
    }


    private fun initApiCallGetPodcasts() {
        val output = viewModelGetPodcastsByCategory.transform(
            GetPodcastsByCategoryViewModel.Input(
                getPodcastsDataTrigger,
                category.id
            )
        )

        output.response.observe(this, Observer {
            showProgress(false)
            if (it.code != 0) {
                toast(getString(R.string.error_getting_podcasts)).show()
            } else {
                if (it.data.podcasts.size > 0) {
                    showEmptyScenario(false)
                    val previousSize = podcasts.size
                    podcasts.addAll(it.data.podcasts)
                    setViewModelVariables()
                    podcastsGridAdapter.notifyItemRangeInserted(previousSize, it.data.podcasts.size)
                } else {
                    if (podcasts.size == 0)
                        showEmptyScenario(true)

                    isLastPage = true
                }
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            showProgress(false)
            view?.hideKeyboard()
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })
    }



    private fun initApiCallDeletePodcast() {
        val output = viewModelDeletePodcast.transform(
            DeletePodcastViewModel.Input(
                deletePodcastDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                toast(getString(R.string.delete_podcast_error))
            } else {
                podcasts.removeAt(lastPodcastDeletedPosition)
                rvPodcasts?.adapter?.notifyItemRemoved(lastPodcastDeletedPosition)
                if(podcasts.size == 0)
                    showEmptyScenario(true)
            }
        })

        output.errorMessage.observe(this, Observer {
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
            toast(getString(R.string.delete_podcast_error))
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_REPORT_PODCAST -> {
                    data?.let {
                        val reason = data.getStringExtra("reason")
                        viewModelCreatePodcastReport.reason = reason
                        createPodcastReportDataTrigger.onNext(Unit)
                    }
                }
                REQUEST_REPORT_USER -> {
                    data?.let {
                        val reason = data.getStringExtra("reason")
                        reason?.let { viewModelCreateUserReport.reason = it }
                        createUserReportDataTrigger.onNext(Unit)
                    }
                }
                /*REQUEST_PODCAST_DETAILS -> {
                    data?.let {
//                        val podcast = data.getSerializableExtra("podcast") as UIPodcast
                        val position = data.getIntExtra("position", 0)
                        lastPodcastByIdRequestedPosition = position
                        feedItemsList[position].podcast?.id?.let {
                            viewModelGetPodcastById.idPodcast = it
                        }
                        showProgressBar()
                        getPodcastByIdDataTrigger.onNext(Unit)
//                        val changedItem = feedItemsList[position]
//                        changedItem.podcast = podcast
//                        feedAdapter?.notifyItemChanged(position)
                    }
                }*/
            }
        }
    }
}