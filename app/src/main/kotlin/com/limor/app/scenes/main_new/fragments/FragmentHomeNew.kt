package com.limor.app.scenes.main_new.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.audio.wav.waverecorder.calculateAmplitude
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentHomeNewBinding
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view.BottomSheetEditPreview
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.scenes.main_new.view_model.PodcastInteractionViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayBillingHandler
import com.limor.app.uimodels.CastUIModel
import kotlinx.android.synthetic.main.fragment_home_new.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

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
    var inAppProducts: List<SkuDetails>? = null
    lateinit var binding: FragmentHomeNewBinding

    private var homeFeedAdapter: HomeFeedAdapter? = null
    private var castOffset = 0
    private val currentCasts = mutableListOf<CastUIModel>()

    private var sharedPodcastId = -1

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
        initPlayHandler()
        initSwipeToRefresh()
        createAdapter()
        setUpRecyclerView()
        subscribeToViewModel()
        setOnClicks()
    }

    private fun initPlayHandler() {
        playBillingHandler.connectToBillingClient{ connected ->
            if (connected) {
                // The BillingClient is ready. You can query purchases here.
                lifecycleScope.launch {
                    fetchProducts()
                }
            }
        }
    }

    private suspend fun fetchProducts() {
        val tiers = CommonsKt.getLocalPriceTiers(requireContext())
        val ids = tiers.keys.toTypedArray().toCollection(ArrayList())
        //val ids = arrayListOf("com.limor.dev.monthly_plan")
        Timber.d("Billing $ids")
        inAppProducts = playBillingHandler.queryInAppSKUDetails(ids) { purchase ->
            //Handle Purchase, should be consumed it right away
            val consumeParams =
                ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            lifecycleScope.launch {
                playBillingHandler.consumePurchase(consumeParams)
            }
        }
        Timber.d("Saved $inAppProducts")


    }

    private fun launchPurchaseCast(sku: SkuDetails?) {
        sku?.let{
            playBillingHandler.launchBillingFlowFor(it,requireActivity())
        }
    }

    private fun setOnClicks() {
        binding.btnNotification.setOnClickListener {
           findNavController().navigate(R.id.navigation_notifications)
        }
    }

    private fun loadFeeds() {
        homeFeedViewModel.loadHomeFeed(
            offset = castOffset,
            limit = Constants.HOME_FEED_ITEM_BATCH_SIZE
        )
    }

    private fun reload() {
        castOffset = 0
        homeFeedViewModel.loadHomeFeed()
    }

    private fun initSwipeToRefresh() {
        binding.swipeToRefresh.setColorSchemeResources(R.color.colorAccent)
        binding.swipeToRefresh.setOnRefreshListener {
            reload()
        }
    }

    private fun onLoadCasts(casts: List<CastUIModel>) {
        if (castOffset == 0) {
            currentCasts.clear()
            when {
                casts.isEmpty() -> {
                    binding.noFeedLayout.visibility = View.VISIBLE
                }
                else -> {
                    binding.noFeedLayout.visibility = View.GONE
                }
            }
        }

        currentCasts.addAll(casts)

        val all = mutableListOf<CastUIModel>()
        all.addAll(currentCasts)


        val recyclerViewState = binding.rvHome.layoutManager?.onSaveInstanceState()
        homeFeedAdapter?.apply {
            loadMore =
                currentCasts.size >= Constants.HOME_FEED_ITEM_BATCH_SIZE &&
                        casts.size >= Constants.HOME_FEED_ITEM_BATCH_SIZE
            submitList(all)
            isLoading = false
        }
        binding.rvHome.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun subscribeToViewModel() {
        homeFeedViewModel.homeFeedData.observe(viewLifecycleOwner) { casts ->
            binding.swipeToRefresh.isRefreshing = false
            onLoadCasts(casts)
        }
        likePodcastViewModel.reload.observe(viewLifecycleOwner){
            reloadCurrentCasts()
        }
        recastPodcastViewModel.recastedResponse.observe(viewLifecycleOwner){
            reloadCurrentCasts()
        }
        recastPodcastViewModel.deleteRecastResponse.observe(viewLifecycleOwner){
            reloadCurrentCasts()
        }
        sharePodcastViewModel.sharedResponse.observe(viewLifecycleOwner){
            println("Will reload...")
            reloadCurrentCasts()
        }
        podcastInteractionViewModel.reload.observe(viewLifecycleOwner){
            if(it == true){
                reloadCurrentCasts()
            }
        }
    }

    private fun reloadCurrentCasts() {
        val loadedCount = currentCasts.size
        castOffset = 0
        homeFeedViewModel.loadHomeFeed(
            offset = castOffset,
            limit = loadedCount
        )
    }

    private fun createAdapter() {
        homeFeedAdapter = HomeFeedAdapter(
            onLikeClick = { castId, like ->
                likePodcastViewModel.likeCast(castId, like)
            },
            onCastClick = { cast ->
                openPlayer(cast)
            },
            onReCastClick = { castId, isRecasted ->
                if(isRecasted){
                    recastPodcastViewModel.reCast(castId)
                } else{
                    recastPodcastViewModel.deleteRecast(castId)
                }
            },
            onCommentsClick = {cast ->
                RootCommentsFragment.newInstance(cast).also { fragment ->
                    fragment.show(parentFragmentManager, fragment.requireTag())
                }
            },
            onShareClick = { cast ->
                sharePodcast(cast)
            },
            onReloadData = { _, _ ->
                reloadCurrentCasts()
            },
            onHashTagClick = { hashtag ->
                (activity as? PlayerViewManager)?.navigateToHashTag(hashtag)
            },
            onLoadMore = {
                castOffset = currentCasts.size
                homeFeedAdapter?.isLoading = true
                loadFeeds()
            },
            onUserMentionClick = { username, userId ->
                context?.let { context -> UserProfileActivity.show(context, username, userId) }
            },
            onEditPreviewClick = {
                BottomSheetEditPreview.newInstance(it).show(requireActivity().supportFragmentManager, BottomSheetEditPreview.TAG)
            },
            onPurchaseCast = { cast, sku ->
                launchPurchaseCast(sku)
            }
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

    var launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(sharedPodcastId != -1) {
            sharePodcastViewModel.share(sharedPodcastId)
            sharedPodcastId = -1
        }
    }

    val sharePodcast : (CastUIModel) -> Unit =  { cast ->

        val podcastLink = Constants.PODCAST_URL.format(cast.id)

        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse(podcastLink)
            domainUriPrefix = Constants.LIMOR_DOMAIN_URL
            androidParameters(BuildConfig.APPLICATION_ID) {
                fallbackUrl = Uri.parse(podcastLink)
            }
            iosParameters(BuildConfig.IOS_BUNDLE_ID) {
            }
            socialMetaTagParameters {
                title = cast.title.toString()
                description = cast.caption.toString()
                cast.imageLinks?.large?.let {
                    imageUrl = Uri.parse(cast.imageLinks.large)
                }
            }
        }

        Firebase.dynamicLinks.shortLinkAsync {
            longLink = dynamicLink.uri
        }.addOnSuccessListener { (shortLink, flowChartLink) ->
            try{
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_SUBJECT, cast.title)
                    putExtra(Intent.EXTRA_TEXT, "Hey, check out this podcast: $shortLink")
                    type = "text/plain"
                }
                sharedPodcastId = cast.id
                val shareIntent = Intent.createChooser(sendIntent, null)
                launcher.launch(shareIntent)
            } catch (e: ActivityNotFoundException){}

        }.addOnFailureListener {
            Timber.d("Failed in creating short dynamic link")
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

    private fun scrollList(){
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
    suspend fun loadAmps(recordFile: String, bufferSize: Int): List<Int> = withContext(Dispatchers.IO) {
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