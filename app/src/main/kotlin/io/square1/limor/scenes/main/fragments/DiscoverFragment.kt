package io.square1.limor.scenes.main.fragments

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.Transition
import android.transition.Transition.TransitionListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
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
import io.square1.limor.App
import io.square1.limor.common.BaseActivity
import io.square1.limor.extensions.forceLayoutChanges
import io.square1.limor.scenes.main.MainActivity
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
import javax.inject.Inject


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

    private var discoverAccountsFragment: DiscoverAccountsFragment? = null
    private var discoverHashTagsFragment: DiscoverHashTagsFragment? = null


    private var isSearching = false

    private var rlSearch: ViewGroup? = null

    companion object {
        val TAG: String = DiscoverFragment::class.java.simpleName
        fun newInstance() = DiscoverFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rlSearch = view.findViewById(R.id.rl_search)
        rlSearch?.forceLayoutChanges()

        bindViewModel()
        initSwipeRefreshLayout()
        initViewPager()
        viewModelDiscover.discoverState.observe(viewLifecycleOwner, discoverStateObserver())

        tv_see_all_featured_casts.onClick { toast("See all featured casts clicked") }
        tv_see_all_hashtags.onClick { toast("See all hashtags clicked") }
        tv_search_cancel.onClick { hideSearchingView() }

        setupEditText()


    }

    private fun showSearchingView() {
        val valueAnimator = ValueAnimator.ofInt(
            0,
            swipeRefreshLayout_discover.measuredHeight
        ) //+ (requireActivity() as MainActivity).getToolbarHeight()
        valueAnimator.duration = 300L
        valueAnimator.addUpdateListener {
            val animatedValue = valueAnimator.animatedValue as Int
            val layoutParams = ll_root_search.layoutParams
            layoutParams.height = animatedValue
            ll_root_search.layoutParams = layoutParams
        }
        valueAnimator.start()
        tv_search_cancel.visibility = View.VISIBLE

        discoverAccountsFragment?.setSearchText(et_search.text.toString())
        discoverHashTagsFragment?.setSearchText(et_search.text.toString())

//        val toolbar = (requireActivity() as MainActivity).getToolBar()
//        toolbar.forceLayoutChanges()
//        val layoutTransition = toolbar.layoutTransition
//        layoutTransition.addTransitionListener(object: LayoutTransition.TransitionListener {
//            override fun startTransition(
//                p0: LayoutTransition?,
//                p1: ViewGroup?,
//                p2: View?,
//                p3: Int
//            ) {
//
//            }
//
//            override fun endTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) {
//
//            }
//        })
//        (requireActivity() as MainActivity).hideToolbar(true)
        isSearching = true
    }

    private fun hideSearchingView() {
        val valueAnimator = ValueAnimator.ofInt(swipeRefreshLayout_discover.measuredHeight, 0)
        valueAnimator.duration = 300L
        valueAnimator.addUpdateListener {
            val animatedValue = valueAnimator.animatedValue as Int
            val layoutParams = ll_root_search.layoutParams
            layoutParams.height = animatedValue
            ll_root_search.layoutParams = layoutParams
        }
        valueAnimator.start()

        tv_search_cancel.visibility = View.GONE
        et_search.setText("")
        (requireActivity() as MainActivity).hideToolbar(false)
        isSearching = false
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
                        discoverAccountsFragment = DiscoverAccountsFragment.newInstance()
                        discoverAccountsFragment as DiscoverAccountsFragment
                    }
                    else -> {
                        discoverHashTagsFragment = DiscoverHashTagsFragment.newInstance()
                        discoverHashTagsFragment as DiscoverHashTagsFragment
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
                    if (isSearching) {
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
        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length > 2) {
                    showSearchingView()

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
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
