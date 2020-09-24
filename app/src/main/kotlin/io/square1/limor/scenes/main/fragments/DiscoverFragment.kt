package io.square1.limor.scenes.main.fragments

import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.App
import io.square1.limor.common.BaseActivity
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
import timber.log.Timber
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

        bindViewModel()
        initSwipeRefreshLayout()
        viewModelDiscover.discoverState.observe(viewLifecycleOwner, discoverStateObserver())

        tv_see_all_featured_casts.onClick { toast("See all featured casts clicked") }
        tv_see_all_hashtags.onClick { toast("See all hashtags clicked") }

        setupEditText()

    }

    override fun onStart() {
        viewModelDiscover.start()
        super.onStart()
    }

    private fun setupEditText() {
        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s.toString().length > 2){
                    Timber.e("baz")
                    val rlParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                    ll_root_search.layoutParams = rlParams
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
