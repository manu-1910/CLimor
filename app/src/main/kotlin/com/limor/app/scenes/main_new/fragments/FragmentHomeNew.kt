package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentHomeNewBinding
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main_new.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.uimodels.CastUIModel
import kotlinx.android.synthetic.main.fragment_home_new.*
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class FragmentHomeNew : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val homeFeedViewModel: HomeFeedViewModel by viewModels { viewModelFactory }
    private val likePodcastViewModel: LikePodcastViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }

    lateinit var binding: FragmentHomeNewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSwipeToRefresh()
        subscribeToViewModel()
        setOnClicks()
    }

    private fun setOnClicks() {
        binding.btnNotification.setOnClickListener {
            toast("Notifications Coming Soon")
        }
    }

    private fun initSwipeToRefresh() {
        binding.swipeToRefresh.setColorSchemeResources(R.color.colorAccent)
        binding.swipeToRefresh.setOnRefreshListener {
            homeFeedViewModel.loadHomeFeed()
        }
    }

    private fun subscribeToViewModel() {
        homeFeedViewModel.homeFeedData.observe(viewLifecycleOwner) { casts ->
            binding.swipeToRefresh.isRefreshing = false
            setDataToRecyclerView(casts)
        }
        recastPodcastViewModel.recatedResponse.observe(viewLifecycleOwner){
            homeFeedViewModel.loadHomeFeed()
        }
    }

    private fun setDataToRecyclerView(list: List<CastUIModel>) {
        val adapter = binding.rvHome.adapter
        if (adapter != null) {
            (adapter as HomeFeedAdapter).submitList(list)
        } else {
            setUpRecyclerView(list)
        }
    }

    private fun setUpRecyclerView(list: List<CastUIModel>) {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvHome.itemAnimator = null

        binding.rvHome.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        binding.rvHome.addItemDecoration(MarginItemDecoration(itemMargin))
        val adapter = HomeFeedAdapter(
            onLikeClick = { castId, like ->
                likePodcastViewModel.likeCast(castId, like)
            },
            onCastClick = { cast ->
                openPlayer(cast)
            },
            onReCastClick = { castId ->
                recastPodcastViewModel.reCast(castId)
            },
            onCommentsClick = {cast ->
                RootCommentsFragment.newInstance(cast).also { fragment ->
                    fragment.show(parentFragmentManager, fragment.requireTag())
                }
            }
        ).apply { submitList(list) }
        rvHome.adapter = adapter
    }

    private fun openPlayer(cast: CastUIModel) {
        (activity as? PlayerViewManager)?.showPlayer(
            PlayerViewManager.PlayerArgs(
                PlayerViewManager.PlayerType.EXTENDED,
                cast
            )
        )
    }
}