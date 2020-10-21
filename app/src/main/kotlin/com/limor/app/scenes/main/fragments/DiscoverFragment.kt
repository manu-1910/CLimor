package com.limor.app.scenes.main.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jakewharton.rxbinding4.widget.textChangeEvents
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.common.BaseFragment
import com.limor.app.common.SessionManager
import com.limor.app.extensions.forceLayoutChanges
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.adapters.DiscoverMainTagsAdapter
import com.limor.app.scenes.main.adapters.FeaturedItemAdapter
import com.limor.app.scenes.main.adapters.SuggestedPersonAdapter
import com.limor.app.scenes.main.adapters.TopCastAdapter
import com.limor.app.scenes.main.fragments.podcast.PodcastDetailsActivity
import com.limor.app.scenes.main.fragments.podcast.PodcastsByTagActivity
import com.limor.app.scenes.main.fragments.profile.ReportActivity
import com.limor.app.scenes.main.fragments.profile.TypeReport
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.viewmodels.*
import com.limor.app.service.AudioService
import com.limor.app.uimodels.UIPodcast
import com.limor.app.uimodels.UITags
import com.limor.app.uimodels.UIUser
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_discover.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val BUNDLE_KEY_SEARCH_TEXT = "BUNDLE_KEY_SEARCH_TEXT"

class DiscoverFragment : BaseFragment(),
    DiscoverMainTagsAdapter.OnDiscoverMainTagClicked,
    SuggestedPersonAdapter.OnPersonClicked,
    FeaturedItemAdapter.OnFeaturedClicked,
    TopCastAdapter.OnTopCastClicked {

    private var lastPodcastDeletedPosition: Int = 0
    var app: App? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionManager : SessionManager

    private lateinit var viewModelDiscover: DiscoverViewModel

    private lateinit var viewModelCreatePodcastReport: CreatePodcastReportViewModel
    private val createPodcastReportDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreateUserReport: CreateUserReportViewModel
    private val createUserReportDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelDeletePodcast: DeletePodcastViewModel
    private val deletePodcastDataTrigger = PublishSubject.create<Unit>()

    private var discoverMainTagsAdapter: DiscoverMainTagsAdapter? = null
    private var suggestedPersonAdapter: SuggestedPersonAdapter? = null
    private var featuredAdapter: FeaturedItemAdapter? = null
    private var topCastAdapter: TopCastAdapter? = null

    private var discoverText = ""
    private var skipTextChange = false

    private var rlSearch: ViewGroup? = null

    companion object {
        val TAG: String = DiscoverFragment::class.java.simpleName
        fun newInstance() = DiscoverFragment()
        private const val REQUEST_REPORT_PODCAST: Int = 1
        private const val REQUEST_REPORT_USER: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        bindViewModel()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initApiCallCreateUserReport()
        initApiCallCreatePodcastReport()
        initApiCallDeletePodcast()

        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModelDiscover.isSearching) {
            revealLayout()
        }

        rlSearch = view.findViewById(R.id.rl_search)
        rlSearch?.forceLayoutChanges()

        initSwipeRefreshLayout()
        initViewPager()
        viewModelDiscover.discoverState.observe(viewLifecycleOwner, discoverStateObserver())

        tv_see_all_featured_casts.onClick { toast("See all featured casts clicked") }
        tv_see_all_hashtags.onClick { toast("See all hashtags clicked") }
        tv_search_cancel.onClick {
            hideSearchingView()
            et_search.setText("")
            discoverText = ""
            et_search.hideKeyboard()
        }


    }

    override fun onResume() {
        super.onResume()
        setupEditText()
    }

    override fun onSaveInstanceState(outState: Bundle) {

        if (!viewModelDiscover.isSearching) {
            viewModelDiscover.isSearchConfigChange = true
        }

        super.onSaveInstanceState(outState)
    }

    private fun showSearchingView() {

        if (viewModelDiscover.isSearchConfigChange) {
            viewModelDiscover.isSearchConfigChange = false
            return
        }

        revealLayout()

        val currentFragment = childFragmentManager.findFragmentByTag("f" + viewPager.currentItem)
        if (currentFragment != null && currentFragment is DiscoverTabFragment) {
            currentFragment.setSearchText(et_search.text.toString())
        }

        viewModelDiscover.isSearching = true

    }

    private fun revealLayout() {
        if (viewModelDiscover.isSearching) {
            //Just show with no animation
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            ll_root_search.layoutParams = params
        } else {
            //Animate the layout
            val valueAnimator = ValueAnimator.ofInt(
                0,
                swipeRefreshLayout_discover.measuredHeight
            )
            valueAnimator.duration = 300L
            valueAnimator.addUpdateListener {
                try {
                    val animatedValue = valueAnimator.animatedValue as Int
                    val layoutParams = ll_root_search.layoutParams
                    layoutParams.height = animatedValue
                    ll_root_search.layoutParams = layoutParams
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            valueAnimator.start()
        }

        tv_search_cancel.visibility = View.VISIBLE
        tab_layout.bringToFront()
        addCloseIconToSearch(true)
    }

    private fun addCloseIconToSearch(add: Boolean) {
        if (add) {
            et_search.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.discover_search,
                0,
                R.drawable.et_close,
                0
            )

            et_search.setOnTouchListener(object : View.OnTouchListener {

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                    val drawableRight = 2

                    if (event?.action == MotionEvent.ACTION_UP) {
                        if (et_search.compoundDrawables[drawableRight] != null && event.rawX >= (et_search.right - et_search.compoundDrawables[drawableRight].bounds.width())) {

                            skipTextChange = true
                            et_search.setText("")

                            return true
                        }
                    }
                    return false
                }

            })

        } else {
            et_search.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.discover_search,
                0,
                0,
                0
            )
        }
    }

    private fun hideSearchingView() {

        val valueAnimator = ValueAnimator.ofInt(swipeRefreshLayout_discover.measuredHeight, 0)
        valueAnimator.duration = 300L
        valueAnimator.addUpdateListener {
            try {
                val animatedValue = valueAnimator.animatedValue as Int
                val layoutParams = ll_root_search.layoutParams
                layoutParams.height = animatedValue
                ll_root_search.layoutParams = layoutParams
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        valueAnimator.start()

        tv_search_cancel.visibility = View.GONE
        //(requireActivity() as MainActivity).hideToolbar(false)
        addCloseIconToSearch(false)
        viewModelDiscover.isSearching = false

        val currentFragment = childFragmentManager.findFragmentByTag("f" + viewPager.currentItem)
        if (currentFragment != null && currentFragment is DiscoverAccountsFragment) {
            currentFragment.clearAdapter()
        }


    }

    private fun initViewPager() {
        val names = arrayOf("Accounts", "Hashtags")
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return names.size
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        DiscoverAccountsFragment.newInstance(discoverText)

                    }
                    else -> {
                        DiscoverHashTagsFragment.newInstance(discoverText)

                    }
                }
            }

        }


        TabLayoutMediator(tab_layout, viewPager) { tab, position ->
            tab.text = names[position]
        }.attach()

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager.currentItem = tab.position

                    val currentFragment =
                        childFragmentManager.findFragmentByTag("f" + viewPager.currentItem)
                    if (currentFragment != null && currentFragment is DiscoverTabFragment) {
                        currentFragment.setSearchText(et_search.text.toString())
                    }
                }
            }
        })
    }

    override fun onStart() {
        viewModelDiscover.start()

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModelDiscover.isSearching) {
                        hideSearchingView()
                    } else {
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }

                }
            }
            )

        super.onStart()
    }

    private fun setupEditText() {

        // Debounce this input so that the API is not called too often
        et_search.textChangeEvents().debounce(300, TimeUnit.MILLISECONDS)
            .map { charSequence ->
                charSequence.text
            }
            //.skip(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { searchText ->

                if (!skipTextChange) {
                    if (searchText.isNotEmpty() && searchText.length > 1) {
                        discoverText = et_search.text.toString()
                        if (!viewModelDiscover.isSearching) {
                            showSearchingView()
                        } else {

                            val currentFragment =
                                childFragmentManager.findFragmentByTag("f" + viewPager.currentItem)
                            if (currentFragment != null && currentFragment is DiscoverTabFragment) {
                                currentFragment.setSearchText(discoverText)
                            }

                        }

                    }
//                    else {
//                        if (viewModelDiscover.isSearching) {
//                            hideSearchingView()
//                        }
//                   }
                } else {
                    skipTextChange = false
                }

            }

    }


    private fun discoverStateObserver(): Observer<DiscoverState> = Observer { state ->
        when (state) {
            is DiscoverAllData -> {

                tv_error.visibility = View.INVISIBLE
                sv_main.visibility = View.VISIBLE

                // set the adapters
                setHashTagsAdapter(state.trendingTags)
                setSuggestedPeopleAdapter(state.suggestedUsers)
                setFeaturedAdapter(state.featuredPodcasts)
                setTopCastAdapter(state.popularPodcasts)

            }

            is DiscoverError -> {
                sv_main.visibility = View.INVISIBLE
                tv_error.visibility = View.VISIBLE
            }

        }

        pb_loading.visibility = View.GONE
        swipeRefreshLayout_discover.isRefreshing = false

    }

    private fun setHashTagsAdapter(tags: ArrayList<UITags>) {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        rv_hashtags?.layoutManager = layoutManager
        discoverMainTagsAdapter = context?.let {
            DiscoverMainTagsAdapter(
                requireContext(),
                tags,
                this
            )
        }
        rv_hashtags.adapter = discoverMainTagsAdapter
    }

    private fun setSuggestedPeopleAdapter(users: ArrayList<UIUser>) {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        rv_suggested_people?.layoutManager = layoutManager
        suggestedPersonAdapter = context?.let {
            SuggestedPersonAdapter(
                requireContext(),
                users,
                this
            )
        }
        rv_suggested_people.adapter = suggestedPersonAdapter
    }

    private fun setTopCastAdapter(podcasts: ArrayList<UIPodcast>) {
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.orientation = RecyclerView.VERTICAL
        rv_top_casts?.layoutManager = layoutManager
        topCastAdapter = context?.let {
            TopCastAdapter(
                requireContext(),
                podcasts,
                this
            )
        }
        rv_top_casts.adapter = topCastAdapter
    }

    private fun setFeaturedAdapter(podcasts: ArrayList<UIPodcast>) {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        rv_featured_casts?.layoutManager = layoutManager
        featuredAdapter = context?.let {
            FeaturedItemAdapter(
                requireContext(),
                podcasts,
                this
            )
        }
        rv_featured_casts.adapter = featuredAdapter
    }

    private fun initSwipeRefreshLayout() {

        swipeRefreshLayout_discover.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorPrimaryDark
            )
        )

        swipeRefreshLayout_discover.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.brandPrimary500
            )
        )

        swipeRefreshLayout_discover.setOnRefreshListener {
            viewModelDiscover.reload()
        }
    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelDiscover = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DiscoverViewModel::class.java)

            viewModelCreatePodcastReport = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastReportViewModel::class.java)

            viewModelCreateUserReport = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateUserReportViewModel::class.java)

            viewModelDeletePodcast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastViewModel::class.java)
        }
    }

    override fun onDiscoverTagClicked(item: UITags, position: Int) {
        val podcastByTagIntent = Intent(context, PodcastsByTagActivity::class.java)
        val text = "#" + item.text
        podcastByTagIntent.putExtra(
            PodcastsByTagActivity.BUNDLE_KEY_HASHTAG,
            text
        )
        startActivity(podcastByTagIntent)
    }

    override fun onPersonClicked(item: UIUser, position: Int) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra("user", item)
        startActivity(userProfileIntent)
    }

    override fun onFeaturedItemClicked(item: UIPodcast, position: Int) {
        val podcastDetailsIntent =
            Intent(context, PodcastDetailsActivity::class.java)
        podcastDetailsIntent.putExtra("podcast", item)
        startActivity(podcastDetailsIntent)
    }

    override fun onMoreClicked(item: UIPodcast, position: Int, view: View) {
        showPopupMenu(view, item, position)
    }

    override fun onPlayClicked(item: UIPodcast, position: Int) {
        startAudioService(item)
    }

    override fun onTopCastItemClicked(item: UIPodcast, position: Int) {
        val podcastDetailsIntent =
            Intent(context, PodcastDetailsActivity::class.java)
        podcastDetailsIntent.putExtra("podcast", item)
        startActivity(podcastDetailsIntent)
    }

    override fun onTopCastPlayClicked(item: UIPodcast, position: Int) {
        startAudioService(item)
    }

    private fun startAudioService(item: UIPodcast) {
        item.audio.audio_url.let {

            AudioService.newIntent(requireContext(), item, 1L)
                .also { intent ->
                    requireContext().startService(intent)
                    val activity = requireActivity() as BaseActivity
                    activity.showMiniPlayer()
                }

        }
    }

    private fun showPopupMenu(
        view: View?,
        item: UIPodcast,
        position: Int
    ) {
        val popup = PopupMenu(context, view, Gravity.END)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup_podcast, popup.menu)

        val loggedUser = sessionManager.getStoredUser()
        if(item.user.id != loggedUser?.id) {
            val menuToHide = popup.menu.findItem(R.id.menu_delete_cast)
            menuToHide.isVisible = false
        }

        //set menu item click listener here
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_share -> onShareClicked(item)
                R.id.menu_report_cast -> onPodcastReportClicked(item)
                R.id.menu_report_user -> onUserReportClicked(item)
                R.id.menu_delete_cast -> onDeletePodcastClicked(item, position)
                R.id.menu_block_user -> toast("You clicked on block user")
            }
            true
        }
        popup.show()
    }

    private fun onDeletePodcastClicked(
        item: UIPodcast,
        position: Int
    ) {
        alert(getString(R.string.confirmation_delete_podcast)) {
            okButton {
                lastPodcastDeletedPosition = position
                viewModelDeletePodcast.podcast = item
                deletePodcastDataTrigger.onNext(Unit)
            }
            cancelButton {  }
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

    private fun onPodcastReportClicked(item: UIPodcast) {
        item.id.let {
            viewModelCreatePodcastReport.idPodcastToReport = it
            val reportIntent = Intent(context, ReportActivity::class.java)
            reportIntent.putExtra("type", TypeReport.CAST)
            startActivityForResult(reportIntent, REQUEST_REPORT_PODCAST)
        }
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
                toast(getString(R.string.delete_podcast_ok))
                viewModelDiscover.deleteFeaturedItem(lastPodcastDeletedPosition)
                rv_featured_casts?.adapter?.notifyItemRemoved(lastPodcastDeletedPosition)
            }
        })

        output.errorMessage.observe(this, Observer {
            toast(getString(R.string.delete_podcast_error))
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
        })
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
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val reason = data?.getStringExtra("reason")
            when (requestCode) {
                REQUEST_REPORT_PODCAST -> {
                    data?.let {
                        viewModelCreatePodcastReport.reason = reason
                        createPodcastReportDataTrigger.onNext(Unit)
                    }
                }
                REQUEST_REPORT_USER -> {
                    data?.let {
                        reason?.let { viewModelCreateUserReport.reason = it }
                        createUserReportDataTrigger.onNext(Unit)
                    }
                }
            }
        }
    }


}
