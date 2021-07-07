package com.limor.app.scenes.main_new.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFade
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentHomeNewBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main_new.PodcastsActivity
import com.limor.app.scenes.main_new.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.CastUIModel
import kotlinx.android.synthetic.main.fragment_home_new.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject


class FragmentHomeNew : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val homeFeedViewModel: HomeFeedViewModel by viewModels { viewModelFactory }
    private val likePodcastViewModel: LikePodcastViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel : RecastPodcastViewModel by viewModels { viewModelFactory }

    lateinit var binding: FragmentHomeNewBinding
    private lateinit var playerBinder: PlayerBinder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeNewBinding.inflate(inflater, container, false)
        initPlayerBinder()
        subscribeToPlayerUpdates()
        initViews()
        return binding.root
    }

    private fun initViews() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSwipeToRefresh()
        subscribeToViewModel()
    }

    private fun initSwipeToRefresh() {
        binding.swipeToRefresh.setColorSchemeResources(R.color.colorAccent)
        binding.swipeToRefresh.setOnRefreshListener {
            homeFeedViewModel.loadHomeFeed()
        }
    }

    private fun initPlayerBinder() {
        playerBinder = PlayerBinder(this, WeakReference(requireContext()))
    }

    private fun subscribeToPlayerUpdates() {
        playerBinder.currentPlayPositionLiveData.observe(viewLifecycleOwner) {
            binding.includeMiniPlayer.cpiPodcastListeningProgress.progress = it.second
            binding.includeMiniPlayer.tvMiniplayerSubtitle.text =
                StringBuilder((it.first ?: 0 / 1000).toString()).append(" ")
                    .append(getString(R.string.left))
        }

        playerBinder.playerStatusLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            when (it) {
                is PlayerStatus.Cancelled -> changePodcastMiniPlayerVisibility(false)
                is PlayerStatus.Ended -> binding.includeMiniPlayer.btnMiniPlayerPlay.setImageResource(
                    R.drawable.ic_player_play
                )
                is PlayerStatus.Error -> Timber.d("Player Error")
                is PlayerStatus.Other -> Timber.d("Player Other")
                is PlayerStatus.Paused -> binding.includeMiniPlayer.btnMiniPlayerPlay.setImageResource(
                    R.drawable.ic_player_play
                )
                is PlayerStatus.Playing -> binding.includeMiniPlayer.btnMiniPlayerPlay.setImageResource(
                    R.drawable.pause
                )
            }
        }
    }

    private fun subscribeToViewModel() {
        homeFeedViewModel.homeFeedData.observe(viewLifecycleOwner) { casts ->
            binding.swipeToRefresh.isRefreshing = false
            setDataToRecyclerView(casts)
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
                openPodcastActivity(cast)
            },
            onReCastClick = { castId ->
                recastPodcastViewModel.reCast(castId)
            }
        ).apply { submitList(list) }
        rvHome.adapter = adapter
    }

    private fun showMiniPlayerView(podcast: CastUIModel) {
        podcast.imageLinks?.small?.let {
            binding.includeMiniPlayer.ivAvatarMiniPlayer.loadCircleImage(it)
        }
        binding.includeMiniPlayer.tvMiniPlayerTitle.text = podcast.title

        binding.includeMiniPlayer.btnCloseMiniPlayer.setOnClickListener {
            changePodcastMiniPlayerVisibility(false)
            playerBinder.stopAudioService()
        }
        changePodcastMiniPlayerVisibility(true)
        binding.includeMiniPlayer.btnMiniPlayerPlay.setOnClickListener {
            playerBinder.playPause()
        }
        binding.includeMiniPlayer.clMiniPlayer.setOnClickListener { _ ->
            openPodcastActivity(podcast)
        }
    }

    private fun changePodcastMiniPlayerVisibility(visible: Boolean) {
        val materialFade = MaterialFade().apply {
            duration = 100L
        }
        TransitionManager.beginDelayedTransition(binding.root, materialFade)
        binding.includeMiniPlayer.clMiniPlayer.visibility =
            if (visible) View.VISIBLE else View.INVISIBLE
        binding.includeMiniPlayer.clMiniPlayer.isEnabled = visible
    }

    private fun openPodcastActivity(cast: CastUIModel) {
        lifecycleScope.launch {
            val intent = PodcastsActivity.getIntent(requireContext(), cast)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                requireActivity(),
                binding.includeMiniPlayer.clMiniPlayer,
                "podcast_player_transition_label" // The transition name to be matched in Activity B.
            )
            startActivity(intent, options.toBundle())
        }
    }
}