package com.limor.app.scenes.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.adapters.DiscoverHashTagsAdapter
import com.limor.app.scenes.main.fragments.podcast.PodcastsByTagActivity
import com.limor.app.scenes.main.viewmodels.DiscoverHashTagsViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UITags
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_discover_accounts.*
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class DiscoverHashTagsFragment : BaseFragment(), DiscoverHashTagsAdapter.OnHashTagSearchClicked,
    DiscoverTabFragment {

    var app: App? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelDiscoverHashTags: DiscoverHashTagsViewModel
    private val getHashTagsTrigger = PublishSubject.create<Unit>()

    private var rvHashTags: RecyclerView? = null
    private var hashtagsAdapter: DiscoverHashTagsAdapter? = null

    companion object {
        val TAG: String = DiscoverHashTagsFragment::class.java.simpleName

        fun newInstance(text: String) = DiscoverHashTagsFragment().apply {
            arguments = Bundle(1).apply {
                putString(BUNDLE_KEY_SEARCH_TEXT, text)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        bindViewModel()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover_hashtags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rvHashTags = view.findViewById(R.id.rv_hashtags)

        initApiCallSearchHashTags()
        configureAdapter()
        arguments?.getString(BUNDLE_KEY_SEARCH_TEXT, "")?.let { setSearchText(it) }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun initApiCallSearchHashTags() {
        val output = viewModelDiscoverHashTags.transform(
            DiscoverHashTagsViewModel.Input(
                getHashTagsTrigger
            )
        )

        output.response.observe(this, Observer {

            rvHashTags?.adapter?.notifyDataSetChanged()
            if (it.data.tags.size == 0) {
                layEmptyScenario.visibility = View.VISIBLE
            } else {
                layEmptyScenario.visibility = View.GONE
            }
            showProgress(false)

        })

        output.errorMessage.observe(this, Observer {
            layEmptyScenario.visibility = View.VISIBLE
            viewModelDiscoverHashTags.results.clear()
            rvHashTags?.adapter?.notifyDataSetChanged()
            showProgress(false)
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })

    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelDiscoverHashTags = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DiscoverHashTagsViewModel::class.java)
        }
    }

    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvHashTags?.layoutManager = layoutManager
        hashtagsAdapter = context?.let {
            DiscoverHashTagsAdapter(
                requireContext(),
                viewModelDiscoverHashTags.results,
                object : DiscoverHashTagsAdapter.OnHashTagSearchClicked {
                    override fun onHashTagClicked(item: UITags, position: Int) {
                        val podcastByTagIntent = Intent(context, PodcastsByTagActivity::class.java)
                        val text = "#" + item.text
                        podcastByTagIntent.putExtra(
                            PodcastsByTagActivity.BUNDLE_KEY_HASHTAG,
                            text
                        )
                        startActivity(podcastByTagIntent)
                    }


                }
            )
        }
        rvHashTags?.adapter = hashtagsAdapter



        rvHashTags?.setHasFixedSize(false)
        val divider = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        context?.getDrawable(R.drawable.divider_item_recyclerview)?.let { divider.setDrawable(it) }
        rvHashTags?.addItemDecoration(divider)

        if (viewModelDiscoverHashTags.results.size != 0) {
            showProgress(false)
        }

    }

    override fun setSearchText(text: String) {
        showProgress(true)
        viewModelDiscoverHashTags.searchText = text
        getHashTagsTrigger.onNext(Unit)
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            pb_loading.visibility = View.VISIBLE
        } else {
            pb_loading.visibility = View.INVISIBLE
        }
    }

    override fun onHashTagClicked(item: UITags, position: Int) {
        toast("Hashtag clicked")
    }

}