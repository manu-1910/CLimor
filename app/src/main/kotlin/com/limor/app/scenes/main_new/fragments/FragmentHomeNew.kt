package com.limor.app.scenes.main_new.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.SkuDetails
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.audio.wav.waverecorder.calculateAmplitude
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentHomeNewBinding
import com.limor.app.dm.ui.ShareDialog
import com.limor.app.extensions.requireTag
import com.limor.app.extensions.visibleIf
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.main_new.view.editpreview.EditPreviewDialog
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.scenes.main_new.view_model.PodcastInteractionViewModel
import com.limor.app.scenes.patron.manage.fragment.ChangePriceActivity
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayBillingHandler
import com.limor.app.service.PurchaseTarget
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToAudioTrack
import com.limor.app.util.SoundType
import com.limor.app.util.Sounds
import kotlinx.android.synthetic.main.fragment_home_new.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FragmentHomeNew : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val homeFeedViewModel: HomeFeedViewModel by viewModels { viewModelFactory }
    private val likePodcastViewModel: LikePodcastViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }
    private val sharePodcastViewModel: SharePodcastViewModel by viewModels { viewModelFactory }
    private val podcastInteractionViewModel: PodcastInteractionViewModel by activityViewModels { viewModelFactory }

    @Inject
    lateinit var playBillingHandler: PlayBillingHandler

    lateinit var binding: FragmentHomeNewBinding

    private var homeFeedAdapter: HomeFeedAdapter? = null

    private var sharedPodcastId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeFeedViewModel.getFeaturedPodcastsGroups()
    }

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
        createAdapter()
        loadSuggestedPodcastGroups()
        setUpRecyclerView()
        subscribeToViewModel()
        setOnClicks()
    }

    private fun launchPurchaseCast(cast: CastUIModel, skuDetails: SkuDetails?) {
        val sku = skuDetails ?: return
        val purchaseTarget = PurchaseTarget(sku, cast)
        playBillingHandler.launchBillingFlowFor(purchaseTarget, requireActivity()) { success ->
            if (success) {
                lifecycleScope.launch {
                    reloadCurrentCasts()
                }
            }
        }
    }

    private fun setOnClicks() {
        binding.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.navigation_notifications)
        }
    }

    private fun loadSuggestedPodcastGroups() {
        lifecycleScope.launch {
            homeFeedViewModel.podcastGroups.observe(viewLifecycleOwner, {
                loadFeeds()
            })
        }
    }

    private fun loadFeeds() {
        lifecycleScope.launch {
            homeFeedViewModel.getHomeFeed().collectLatest { data ->
                binding.swipeToRefresh.isRefreshing = false
                homeFeedAdapter?.submitData(data)
            }
        }
        homeFeedAdapter?.addLoadStateListener { it ->
            if (it.source.append.endOfPaginationReached) {
                toggleNoFeedLayout()
            }
        }
    }

    private fun toggleNoFeedLayout() {
        Log.d("CAPTION_CAPTION", homeFeedAdapter?.snapshot()?.items?.size.toString())
        binding.noFeedLayout.visibleIf(homeFeedAdapter?.snapshot()?.isEmpty() ?: true)
    }

    private fun initSwipeToRefresh() {
        binding.swipeToRefresh.setColorSchemeResources(R.color.colorAccent)
        binding.swipeToRefresh.setOnRefreshListener {
            reloadCurrentCasts()
        }
    }

    private fun subscribeToViewModel() {
        likePodcastViewModel.reload.observe(viewLifecycleOwner) {
            // reloadCurrentCasts()
        }
        recastPodcastViewModel.recastedResponse.observe(viewLifecycleOwner) {
            // reloadCurrentCasts()
        }
        recastPodcastViewModel.deleteRecastResponse.observe(viewLifecycleOwner) {
            reloadCurrentCasts()
        }
        sharePodcastViewModel.sharedResponse.observe(viewLifecycleOwner) {
            println("Will reload...")
            // reloadCurrentCasts()
        }
        podcastInteractionViewModel.reload.observe(viewLifecycleOwner) {
            if (it == true) {
                reloadCurrentCasts()
            }
        }
    }

    private fun reloadCurrentCasts() {
        homeFeedViewModel.invalidate()
    }

    private fun createAdapter() {
        homeFeedAdapter = HomeFeedAdapter(
            onLikeClick = { castId, like ->
                if (like) {
                    Sounds.playSound(requireContext(), SoundType.HEART)
                }
                likePodcastViewModel.likeCast(castId, like)
            },
            onCastClick = { cast, sku ->
                onCastClick(cast, sku)
            },
            onReCastClick = { castId, isRecasted ->
                if (isRecasted) {
                    Sounds.playSound(requireContext(), SoundType.RECAST)
                    recastPodcastViewModel.reCast(castId)
                } else {
                    recastPodcastViewModel.deleteRecast(castId)
                }
            },
            onCommentsClick = { cast, sku ->
                onCommentClick(cast, sku)
            },
            onShareClick = { cast, onShared ->
                ShareDialog.newInstance(cast).also { fragment ->
                    fragment.setOnSharedListener(onShared)
                    fragment.show(parentFragmentManager, fragment.requireTag())
                }
            },
            onReloadData = { _, _ ->
                reloadCurrentCasts()
            },
            onHashTagClick = { hashtag ->
                (activity as? PlayerViewManager)?.navigateToHashTag(hashtag)
            },
            onUserMentionClick = { username, userId ->
                context?.let { context -> UserProfileActivity.show(context, username, userId) }
            },
            onEditPreviewClick = {
                EditPreviewDialog.newInstance(it).also { fragment ->
                    fragment.show(parentFragmentManager, fragment.requireTag())
                }
            },
            onPlayPreviewClick = { cast, play ->
                cast.audio?.mapToAudioTrack()?.let { it1 ->
                    cast.patronDetails?.startsAt?.let { it2 ->
                        cast.patronDetails.endsAt?.let { it3 ->
                            if (play) {
                                (activity as? PlayerViewManager)?.playPreview(
                                    it1, it2.toInt(), it3.toInt()
                                )
                            } else {
                                (activity as? PlayerViewManager)?.stopPreview(true)
                            }
                        }
                    }
                }
            },
            onEditPriceClick = { cast ->
                val intent = Intent(requireActivity(), ChangePriceActivity::class.java)
                intent.putExtra(ChangePriceActivity.CHANGE_PRICE_FOR_ALL_CASTS, false)
                intent.putExtra(ChangePriceActivity.CAST_ID, cast.id)
                intent.putExtra(ChangePriceActivity.SELECTED_PRICE_ID, cast.patronDetails?.priceId)
                editPriceLauncher.launch(intent)
            },
            onPurchaseCast = { cast, sku ->
                launchPurchaseCast(cast, sku)
            },
            productDetailsFetcher = playBillingHandler
        )
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvHome.itemAnimator = null

        binding.rvHome.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        binding.rvHome.addItemDecoration(MarginItemDecoration(itemMargin))
        rvHome.adapter = homeFeedAdapter
    }

    var launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (sharedPodcastId != -1) {
                sharePodcastViewModel.share(sharedPodcastId)
                sharedPodcastId = -1
            }
        }

    var editPriceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                reloadCurrentCasts()
            }
        }

    private fun onCastClick(cast: CastUIModel, sku: SkuDetails?) {
        if (cast.patronDetails?.purchased == false && cast.owner?.id != PrefsHandler.getCurrentUserId(
                requireContext()
            )
        ) {

            if (sku == null) {
                LimorDialog(layoutInflater).apply {
                    setTitle(R.string.purchase_cast_title)
                    setMessage(R.string.purchase_cast_description)
                    setIcon(R.drawable.ic_purchase)
                    addButton(R.string.ok, false)
                }.show()

            } else {
                LimorDialog(layoutInflater).apply {
                    setTitle(R.string.purchase_cast_title)
                    setMessage(R.string.purchase_cast_description)
                    setIcon(R.drawable.ic_purchase)
                    addButton(R.string.cancel, false)
                    addButton(R.string.buy_now, true) {
                        launchPurchaseCast(cast, sku)
                    }
                }.show()
            }
        } else {
            openPlayer(cast)
        }
    }

    private fun onCommentClick(cast: CastUIModel, sku: SkuDetails?) {
        if (BuildConfig.DEBUG) {
            println(
                "Cast owner is ${cast.owner?.id}, current user is ${
                    PrefsHandler.getCurrentUserId(
                        requireContext()
                    )
                }"
            );
        }

        if (cast.patronDetails?.purchased == false && cast.owner?.id != PrefsHandler.getCurrentUserId(
                requireContext()
            )
        ) {
            LimorDialog(layoutInflater).apply {
                setTitle(R.string.purchase_cast_title)
                setMessage(R.string.purchase_cast_description_for_comment)
                setIcon(R.drawable.ic_comment_purchase)
                addButton(R.string.cancel, false)
                addButton(R.string.buy_now, true) {
                    launchPurchaseCast(cast, sku)
                }
            }.show()
        } else {
            RootCommentsFragment.newInstance(cast).also { fragment ->
                fragment.show(parentFragmentManager, fragment.requireTag())
            }
        }
    }

    private fun openPlayer(cast: CastUIModel) {
        (activity as? PlayerViewManager)?.showPlayer(
            PlayerViewManager.PlayerArgs(
                PlayerViewManager.PlayerType.EXTENDED,
                cast.id
            )
        )
    }

    private fun scrollList() {
        binding.rvHome.scrollBy(0, 10)
    }

    private fun showEditPreviewDialog() {
        /*val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val dialogView = layoutInflater.inflate(R.layout.sheet_edit_preview, null)
        val behaviour = bottomSheetDialog.behavior
        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.setCancelable(true)

        val mediaPlayer = MediaPlayer()

        dialogView.playButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                dialogView.playButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_play
                    )
                )
            } else {
                lifecycleScope.launch {
                    mediaPlayer.setDataSource("/storage/emulated/0/Android/data/com.limor.app.dev/files/limorv2/1637835053364.wav")
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

                    val seekHandler: Handler = Handler(Looper.getMainLooper())
                    val seekUpdater: Runnable = object : Runnable {
                        override fun run() {
                            seekHandler.postDelayed(this, 100)
                            mediaPlayer.let {
                                if (it.isPlaying) {
                                    val currentPosition = it.currentPosition
                                    dialogView.playVisualizer.updateTime(currentPosition.toLong(), true)
                                }
                            }
                        }
                    }

                    val mRecorder = WaveRecorder("/storage/emulated/0/Android/data/com.limor.app.dev/files/limorv2/1637835053364.wav")
                    mRecorder.waveConfig.sampleRate = 44100
                    mRecorder.waveConfig.channels = AudioFormat.CHANNEL_IN_STEREO
                    mRecorder.waveConfig.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
                    val amps: List<Int> = loadAmps("/storage/emulated/0/Android/data/com.limor.app.dev/files/limorv2/1637835053364.wav", mRecorder.bufferSize)

                    mediaPlayer.prepareAsync()
                    dialogView.playVisualizer.visibility = View.VISIBLE

                    mediaPlayer.setOnCompletionListener {
                        dialogView.playVisualizer.updateTime(mediaPlayer.duration.toLong(), false)
                        it.pause()
                        dialogView.playButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_play
                            )
                        )
                    }
                    mediaPlayer.setOnPreparedListener {
                        it.start()
                        dialogView.playButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_pause
                            )
                        )
                        seekHandler.post(seekUpdater)
                    }

                    dialogView.playVisualizer.apply {
                        ampNormalizer = { sqrt(it.toFloat()).toInt() }
                    }
                    dialogView.playVisualizer.setWaveForm(
                        amps,
                        mRecorder.tickDuration
                    )
                }
            }
        }

        dialogView.rewindButton.setOnClickListener{

        }

        dialogView.forwardButton.setOnClickListener{

        }

        dialogView.saveButton.setOnClickListener{
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.apply {
            show()
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }*/
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadAmps(recordFile: String, bufferSize: Int): List<Int> =
        withContext(Dispatchers.IO) {
            val amps = mutableListOf<Int>()
            val buffer = ByteArray(bufferSize)
            File(recordFile).inputStream().use {
                it.skip(44.toLong())

                var count = it.read(buffer)
                while (count > 0) {
                    amps.add(buffer.calculateAmplitude())
                    count = it.read(buffer)
                }
            }
            amps
        }

    override fun onDestroy() {
        super.onDestroy()
        playBillingHandler.close()
    }
}