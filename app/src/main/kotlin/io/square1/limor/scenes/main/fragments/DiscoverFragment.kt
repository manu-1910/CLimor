package io.square1.limor.scenes.main.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.square1.limor.App
import io.square1.limor.common.BaseActivity
import io.square1.limor.extensions.forceLayoutChanges
import io.square1.limor.scenes.main.adapters.DiscoverMainTagsAdapter
import io.square1.limor.scenes.main.adapters.FeaturedItemAdapter
import io.square1.limor.scenes.main.adapters.SuggestedPersonAdapter
import io.square1.limor.scenes.main.adapters.TopCastAdapter
import io.square1.limor.scenes.main.viewmodels.*
import io.square1.limor.service.AudioService
import io.square1.limor.uimodels.UIPodcast
import io.square1.limor.uimodels.UITags
import io.square1.limor.uimodels.UIUser
import kotlinx.android.synthetic.main.fragment_discover.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val BUNDLE_KEY_SEARCH_TEXT = "BUNDLE_KEY_SEARCH_TEXT"

class DiscoverFragment : BaseFragment(),
    DiscoverMainTagsAdapter.OnDiscoverMainTagClicked,
    SuggestedPersonAdapter.OnPersonClicked,
    FeaturedItemAdapter.OnFeaturedClicked,
    TopCastAdapter.OnTopCastClicked {

    var app: App? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelDiscover: DiscoverViewModel

    private var discoverMainTagsAdapter: DiscoverMainTagsAdapter? = null
    private var suggestedPersonAdapter: SuggestedPersonAdapter? = null
    private var featuredAdapter: FeaturedItemAdapter? = null
    private var topCastAdapter: TopCastAdapter? = null

    private var discoverText = ""

    private var rlSearch: ViewGroup? = null

    companion object {
        val TAG: String = DiscoverFragment::class.java.simpleName
        fun newInstance() = DiscoverFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        bindViewModel()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(viewModelDiscover.isSearching){
            revealLayout()
        }

        rlSearch = view.findViewById(R.id.rl_search)
        rlSearch?.forceLayoutChanges()

        initSwipeRefreshLayout()
        initViewPager()
        viewModelDiscover.discoverState.observe(viewLifecycleOwner, discoverStateObserver())

        tv_see_all_featured_casts.onClick { toast("See all featured casts clicked") }
        tv_see_all_hashtags.onClick { toast("See all hashtags clicked") }
        tv_search_cancel.onClick { hideSearchingView() }


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

                            hideSearchingView()

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

                } else {
                    if (viewModelDiscover.isSearching) {
                        hideSearchingView()
                    }
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
        }
    }

    override fun onDiscoverTagClicked(item: UITags, position: Int) {
        toast("hashTag clicked")
    }

    override fun onPersonClicked(item: UIUser, position: Int) {
        toast("person clicked")
    }

    override fun onFeaturedItemClicked(item: UIPodcast, position: Int) {
        toast("featured item clicked")
    }

    override fun onMoreClicked(item: UIPodcast, position: Int) {
        toast("more clicked")
    }

    override fun onPlayClicked(item: UIPodcast, position: Int) {
        startAudioService(item)
    }

    override fun onTopCastItemClicked(item: UIPodcast, position: Int) {
        toast("top cast clicked")
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


}
