package com.limor.app.scenes.main.fragments.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.adapters.PodcastsGridAdapter
import com.limor.app.scenes.main.viewmodels.GetPodcastsByCategoryViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UICategory
import com.limor.app.uimodels.UIPodcast
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.fragment_podcasts_by_category.*
import kotlinx.android.synthetic.main.fragment_users_blocked.*
import kotlinx.android.synthetic.main.fragment_users_blocked.layEmptyScenario
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
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

    private lateinit var viewModelGetPodcastsByCategory: GetPodcastsByCategoryViewModel

    private val getPodcastsDataTrigger = PublishSubject.create<Unit>()

    private lateinit var category: UICategory
    private var podcasts: ArrayList<UIPodcast> = ArrayList()
    private lateinit var podcastsGridAdapter: PodcastsGridAdapter


    companion object {
        val TAG: String = DiscoverPodcastsByCategoryFragment::class.java.simpleName
        fun newInstance() = DiscoverPodcastsByCategoryFragment()
        private const val OFFSET_INFINITE_SCROLL: Int = 10
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
            initApiCallGetPodcasts()
            initRecyclerView()
            initSwipeAndRefreshLayout()
            getPodcastsDataTrigger.onNext(Unit)
        } ?: run {
            alert(getString(R.string.error_getting_category)) {
                okButton {
                    activity?.finish()
                }
            }
        }
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
        }
    }


    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        rvPodcasts?.layoutManager = layoutManager
        podcastsGridAdapter = PodcastsGridAdapter(
            context!!,
            podcasts,
            object : PodcastsGridAdapter.OnPodcastClickListener {
                override fun onItemClicked(item: UIPodcast, position: Int) {

                }

                override fun onPlayClicked(item: UIPodcast, position: Int) {

                }

                override fun onUserClicked(item: UIPodcast, position: Int) {

                }

                override fun onMoreClicked(item: UIPodcast, position: Int, view: View) {

                }

            })

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
    }

    private fun requestNewData(showProgress: Boolean) {
        if (showProgress)
            showProgress(true)
        getPodcastsDataTrigger.onNext(Unit)
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
        isLastPage = false
        viewModelGetPodcastsByCategory.offset = 0
        showEmptyScenario(true)
        podcasts.clear()
        rvBlockedUsers?.recycledViewPool?.clear()
        rvBlockedUsers.adapter?.notifyDataSetChanged()
        requestNewData(true)
    }

    private fun showProgress(show: Boolean) {
        swipeRefreshLayout.isRefreshing = show
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
}