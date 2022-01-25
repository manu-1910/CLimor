package com.limor.app.scenes.main_new.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.dynamiclinks.ktx.*
import com.limor.app.R
import com.limor.app.databinding.FragmentExtendedPlayerBinding
import com.limor.app.dm.ui.ShareDialog
import com.limor.app.extensions.*
import com.limor.app.playlists.PlaylistsViewModel
import com.limor.app.playlists.SaveToPlaylistFragment
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.PodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.fragments.comments.UserMentionFragment
import com.limor.app.scenes.main_new.view_model.ListenPodcastViewModel
import com.limor.app.scenes.main_new.view_model.PodcastInteractionViewModel
import com.limor.app.scenes.utils.*
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.*
import com.limor.app.util.requestRecordPermissions
import kotlinx.android.synthetic.main.fragment_extended_player.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Integer.max
import java.lang.Math.min
import java.time.Duration
import javax.inject.Inject

class ExtendedPlayerFragment : UserMentionFragment(),
    DialogPodcastMoreActions.UpdatePodcastListener, PlayerFragment {

    companion object {
        private const val CAST_ID_KEY = "CAST_ID_KEY"
        private const val KEY_CAST_IDS = "KEY_CAST_IDS"
        private const val AUTO_PLAY_KEY = "AUTO_PLAY_KEY"
        private const val RESTARTED = "RESTARTED"
        fun newInstance(
            castId: Int,
            castIds: List<Int>?,
            autoPlay: Boolean = false,
            restarted: Boolean = false,
        ): ExtendedPlayerFragment {
            return ExtendedPlayerFragment().apply {
                arguments = bundleOf(
                    CAST_ID_KEY to castId,
                    KEY_CAST_IDS to castIds?.toIntArray(),
                    AUTO_PLAY_KEY to autoPlay,
                    RESTARTED to restarted
                )
            }
        }
    }

    private var _binding: FragmentExtendedPlayerBinding? = null
    private val binding get() = _binding!!

    private val likePodcastViewModel: LikePodcastViewModel by viewModels { viewModelFactory }

    private val podcastViewModel: PodcastViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }
    private val playlistsViewModel: PlaylistsViewModel by viewModels { viewModelFactory }

    private var castId: Int = 0
    private val castIds: List<Int>? by lazy {
        (requireArguments()[KEY_CAST_IDS] as? IntArray)?.asList()
    }
    private val isInPlaylist by lazy {
        (castIds?.size ?: 0) > 1
    }
    private val sharePodcastViewModel: SharePodcastViewModel by viewModels { viewModelFactory }
    private val podcastInteractionViewModel: PodcastInteractionViewModel by activityViewModels { viewModelFactory }
    private val listenPodcastViewModel: ListenPodcastViewModel by viewModels { viewModelFactory }

    private var playerUpdatesJob: Job? = null
    private var updatePodcasts: Boolean = false
    private var isStartedPlayingInThisObject = false

    private var restarted: Boolean = false
    private var sharedPodcastId = -1

    private var currentCast: CastUIModel? = null

    @Inject
    lateinit var playerBinder: PlayerBinder

    override fun reload() {
        loadFirstComment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        castId = requireArguments()[CAST_ID_KEY] as Int

        requireActivity().onBackPressedDispatcher.addCallback(owner = this) {
            podcastInteractionViewModel.reload.postValue(true)
            (activity as? PlayerViewManager)?.showPlayer(
                PlayerViewManager.PlayerArgs(
                    PlayerViewManager.PlayerType.SMALL,
                    castId,
                    castIds
                )
            )
            isEnabled = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExtendedPlayerBinding.inflate(inflater, container, false)

        subscribeToCastUpdates()
        subscribeToCommentUpdates()
        subscribeToShareUpdate()

        setPlaylistUI()

        return binding.root
    }

    private fun togglePlaylistButton(button: View, enable: Boolean) {
        button.isEnabled = enable
        button.alpha = if (enable) 1.0f else 0.4f
    }

    private fun playNextPodcast() {
        if (!isInPlaylist) {
            return
        }

        onPlaylistNavigation(1)
    }

    private fun setPlaylistButtons() {
        val ids = castIds ?: return
        val index = ids.indexOf(castId)

        var enablePrevious = true
        var enableNext = true

        if (index == 0) {
            enablePrevious = false
        } else if (index == ids.size - 1) {
            enableNext = false
        }

        togglePlaylistButton(binding.btnPodcastPlayPrevious, enablePrevious)
        togglePlaylistButton(binding.btnPodcastPlayNext, enableNext)
    }

    private fun onPlaylistNavigation(direction: Int) {
        if (!isInPlaylist) {
            return
        }
        val ids = castIds ?: return
        val index = ids.indexOf(castId)
        val next = index + direction

        if (next < 0 || next == ids.size) {
            return
        }

        castId = ids[next]
        setPlaylistButtons()

        podcastViewModel.loadCast(castId)
        loadFirstComment()
    }

    private fun setPlaylistButton(button: View, direction: Int) {
        button.apply {
            visibleIf(isInPlaylist)
            setOnClickListener {
                onPlaylistNavigation(direction)
            }
        }
    }

    private fun setPlaylistUI() {
        setPlaylistButton(binding.btnPodcastPlayNext, 1)
        setPlaylistButton(binding.btnPodcastPlayPrevious, -1)

        setPlaylistButtons()
    }

    private fun setSeekbar() {
        binding.lpiPodcastProgress.progress = 0
        binding.lpiPodcastProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean,
            ) {
                if (fromUser) {
                    val progressMs = progress * 1000
                    playerBinder.seekTo(progressMs)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        podcastViewModel.loadCast(castId)
        loadFirstComment()

        setUpPopup(binding.taviVoice.editText, binding.taviVoice)
    }

    private fun loadFirstComment() {
        commentsViewModel.loadComments(castId, limit = 1)
    }

    private fun subscribeToCastUpdates() {
        podcastViewModel.cast.observe(viewLifecycleOwner) { cast ->
            currentCast = cast
            bindViews(cast)
            subscribeToPlayerUpdates(cast)

            val autoPlay = requireArguments().getBoolean(
                AUTO_PLAY_KEY,
                false
            )

            restarted = requireArguments().getBoolean(RESTARTED, false)

            val audioTrack = cast.audio!!.mapToAudioTrack()
            if (autoPlay && playerBinder.audioTrackIsNotPlaying(audioTrack)) {
                playerBinder.playPause(audioTrack, true)
            }
            if (autoPlay || restarted) {
                listenPodcastViewModel.listenPodcast(castId)
            }
        }
    }

    private fun setFirstCommentContent(comment: CommentUIModel) {
        binding.tvFirstCollapsedComment.setTextWithTagging(
            comment.content,
            comment.mentions,
            comment.tags,
            { username, userId ->
                context?.let { context -> UserProfileActivity.show(context, username, userId) }
            },
            { hashTag -> onHashTagClick(hashTag) }
        )
    }

    private fun subscribeToCommentUpdates() {
        commentsViewModel.comments.observe(viewLifecycleOwner) { comments ->
            val firstComment = comments.firstOrNull()
            if (firstComment != null) {
                setFirstCommentContent(firstComment)
                firstComment.user?.getAvatarUrl()?.let { imageUrl ->
                    binding.ivAvatarFirstCollapsedComment.loadCircleImage(imageUrl)
                }
                binding.tvCommentName.text = firstComment.user?.username
                binding.firstCollapsedCommentVisibilityGroup.makeVisible()
                binding.noCommentsMessage.makeGone()
                binding.llExtendCommentsHeader.isEnabled = true
                binding.llExtendCommentsHeader.makeVisible()
            } else {
                binding.firstCollapsedCommentVisibilityGroup.makeGone()
                binding.noCommentsMessage.makeVisible()
                binding.llExtendCommentsHeader.isEnabled = false
                binding.llExtendCommentsHeader.makeGone()
            }
        }

        commentsViewModel.commentAddEvent.observe(viewLifecycleOwner) {
            if (it == -1) {
                reportError(getString(R.string.could_not_save_comment))
            } else {
                loadFirstComment()
            }
            binding.taviVoice.reset()
        }
    }

    private fun bindViews(cast: CastUIModel) {
        setCommentsCount(cast)
        setPodcastGeneralInfo(cast)
        setPodcastOwnerInfo(cast)
        setPodcastCounters(cast)
        setAudioInfo(cast)
        setSeekbar()
        loadImages(cast)
        setViewsVisibility()
        setOnClicks(cast)
        initLikeState(cast)
        initRecastState(cast)
        initListenState(cast)
    }

    private fun setCommentsCount(cast: CastUIModel) {
        val count = cast.commentsCount ?: 0
        if (count < 2) {
            binding.textComments.text = getString(R.string.view_all_comments_label)
        } else {
            binding.textComments.text = getString(R.string.view_all_n_comments__with_format, count)
        }

    }

    private fun subscribeToShareUpdate() {
        sharePodcastViewModel.sharedResponse.observe(viewLifecycleOwner) {
            binding.btnPodcastReply.shared = it?.shared == true
        }
    }

    private fun setPodcastGeneralInfo(cast: CastUIModel) {
        binding.tvPodcastTitle.text = cast.title
        binding.tvPodcastSubtitle.setTextWithTagging(
            cast.caption,
            cast.mentions,
            cast.tags,
            { username, userId ->
                context?.let { context -> UserProfileActivity.show(context, username, userId) }
            },
            { hashTag -> onHashTagClick(hashTag) }
        )
        if (cast.patronCast == true) {
            binding.patronCastIndicator.visibility = View.VISIBLE
        } else {
            binding.patronCastIndicator.visibility = View.GONE
        }
    }

    private fun setPodcastOwnerInfo(cast: CastUIModel) {
        binding.tvPodcastUserName.text = cast.owner?.username
        binding.tvPodcastUserSubtitle.text = cast.getCreationDateAndPlace(requireContext(), true)
        binding.ivVerifiedAvatar.visibility =
            if (cast.owner?.isVerified == true) View.VISIBLE else View.GONE
    }

    private fun setPodcastCounters(cast: CastUIModel) {
        binding.tvPodcastLikes.text = cast.likesCount.toString()
        binding.tvPodcastRecast.text = cast.recastsCount?.toString()
        binding.tvPodcastComments.text = cast.commentsCount?.toString()

        var listenCount = cast.listensCount ?: 0
        binding.tvPodcastNumberOfListeners.tag = listenCount.toString()
        binding.tvPodcastNumberOfListeners.text = listenCount.toLong().formatHumanReadable

        //applyRecastStyle(cast.isRecasted == true)
        initRecastState(cast)
        binding.btnPodcastRecast.recasted = cast.isRecasted == true
        binding.btnPodcastReply.shared = cast.isShared == true
    }

    private fun updateListenCount() {
        binding.tvPodcastNumberOfListeners.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                R.color.textAccent
            )
        )
        binding.ivPodcastListening.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.textAccent))
        binding.tvPodcastNumberOfListeners.text =
            (binding.tvPodcastNumberOfListeners.tag.toString().toLong() + 1).formatHumanReadable
        binding.tvPodcastNumberOfListeners.tag =
            (binding.tvPodcastNumberOfListeners.tag.toString().toLong() + 1).toString()
    }

    private fun initListenState(cast: CastUIModel) {
        binding.tvPodcastNumberOfListeners.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                if (cast.isListened == true) R.color.textAccent else R.color.subtitle_text_color
            )
        )
        if (cast.isListened == true) {
            binding.ivPodcastListening.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.textAccent
                )
            )
        } else {
            binding.ivPodcastListening.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.subtitle_text_color
                )
            )
        }
    }

    private fun setAudioInfo(cast: CastUIModel) {
        binding.tvRecastPlayMaxPosition.text = cast.audio?.duration?.toReadableStringFormat(
            DURATION_READABLE_FORMAT_1
        )
        cast.audio?.duration?.seconds?.also { binding.lpiPodcastProgress.max = it.toInt() }
    }

    private fun subscribeToPlayerUpdates(cast: CastUIModel) {
        playerUpdatesJob?.cancel()
        playerUpdatesJob = lifecycleScope.launch {

            val audioModel = cast.audio?.mapToAudioTrack() ?: return@launch

            playerBinder.apply {
                getCurrentPlayingPosition(audioModel)
                    .onEach { handlePlayPosition(it) }
                    .launchIn(this@launch)

                getPlayerStatus(audioModel)
                    .onEach { handlePlayerStatus(it) }
                    .launchIn(this@launch)
            }
        }
    }

    private fun handlePlayPosition(position: Duration) {
        binding.lpiPodcastProgress.progress = position.seconds.toInt()
        // ((duration.seconds * 100) / audioModel.duration.seconds).toInt()
        binding.tvRecastPlayCurrentPosition.text =
            position.toReadableStringFormat(DURATION_READABLE_FORMAT_3)
    }

    private fun handlePlayerStatus(status: PlayerStatus) {
        when (status) {
            is PlayerStatus.Ended -> {
                binding.btnPodcastPlayExtended.setImageResource(R.drawable.ic_play)
                binding.audioBufferingView.visibility = View.GONE

                val autoPlay = requireArguments().getBoolean(
                    AUTO_PLAY_KEY,
                    false
                )

                if (isStartedPlayingInThisObject || autoPlay || restarted) {
                    updateListenCount()
                    restarted = false
                }

                playNextPodcast()
            }
            is PlayerStatus.Error -> binding.audioBufferingView.visibility = View.GONE
            is PlayerStatus.Buffering -> binding.audioBufferingView.visibility =
                View.VISIBLE
            is PlayerStatus.Paused -> {
                binding.audioBufferingView.visibility = View.GONE
                binding.btnPodcastPlayExtended.setImageResource(R.drawable.ic_play)
            }
            is PlayerStatus.Playing -> {
                binding.audioBufferingView.visibility = View.GONE
                binding.btnPodcastPlayExtended.setImageResource(R.drawable.pause)
            }
            is PlayerStatus.Cancelled -> Timber.d("Player Canceled")
            is PlayerStatus.Init -> Timber.d("Player Init")
            is PlayerStatus.Other -> Timber.d("Player Other")
        }
    }

    private fun loadImages(cast: CastUIModel) {
        cast.owner?.getAvatarUrl()?.let {
            binding.ivPodcastAvatar.loadCircleImage(it)
        }

        cast.imageLinks?.large?.let {
            binding.ivPodcastBackground.loadImage(it)
        }
        //Handling the color background for podcast
        CommonsKt.handleColorFeed(cast, colorFeedText, requireContext())
    }

    private fun setViewsVisibility() {
        binding.btnPodcastMore.makeVisible()
    }

    private fun setOnClicks(cast: CastUIModel) {
        binding.btnPodcastMore.setOnClickListener {
            it.findViewTreeLifecycleOwner()?.let {
                val dialog = DialogPodcastMoreActions.newInstance(cast)
                dialog.setUpdatePodcastListener(this@ExtendedPlayerFragment)
                dialog.show(parentFragmentManager, DialogPodcastMoreActions.TAG)
            }
        }

        binding.btnPodcastPlayExtended.setOnClickListener {
            cast.audio?.let { audio ->
                if (playerBinder.audioTrackIsInInitState(cast.audio.mapToAudioTrack())) {
                    listenPodcastViewModel.listenPodcast(castId)
                    isStartedPlayingInThisObject = true
                }
                playerBinder.playPause(audio.mapToAudioTrack(), showNotification = true)
            }
        }

        binding.btnPodcastRewindBack.setOnClickListener {
            playerBinder.rewind(5000L)
        }

        binding.btnPodcastRewindForward.setOnClickListener {
            playerBinder.forward(5000L)
        }

        binding.btnPodcastRecast.setOnClickListener {
            recastPodcastViewModel.reCast(castId = cast.id)
        }

        val openCommentsClickListener: (view: View) -> Unit = {
            RootCommentsFragment.newInstance(cast).also { fragment ->
                fragment.show(parentFragmentManager, fragment.requireTag())
            }
        }

        binding.llExtendCommentsHeader.throttledClick(onClick = openCommentsClickListener)
        binding.btnPodcastComments.throttledClick(onClick = openCommentsClickListener)
        binding.tvPodcastComments.throttledClick(onClick = openCommentsClickListener)

        binding.btnPodcastReply.throttledClick {
            ShareDialog.newInstance(cast).also { fragment ->
                fragment.setOnSharedListener {
                    binding.btnPodcastReply.shared = (cast.isShared ?: false) || it.hasShared
                }
                fragment.show(parentFragmentManager, fragment.requireTag())
            }
        }

        // This is copy pasted from FragmentComments, will need to be refactored later...
        binding.taviVoice.initListenerStatus {
            when (it) {
                is MissingPermissions -> requestRecordPermissions(requireActivity())
                is SendData -> {

                    if (it.filePath != null) {
                        uploadVoiceComment(it.filePath) { audioUrl ->
                            commentsViewModel.addComment(
                                cast.id,
                                content = it.text,
                                ownerId = cast.id,
                                ownerType = CommentUIModel.OWNER_TYPE_PODCAST,
                                audioURI = audioUrl,
                                duration = it.duration
                            )
                        }
                    } else {
                        commentsViewModel.addComment(
                            cast.id,
                            it.text,
                            ownerId = cast.id,
                            ownerType = CommentUIModel.OWNER_TYPE_PODCAST
                        )
                    }
                }
                else -> {

                }
            }
        }
        binding.tvPodcastUserName.setOnClickListener {
            openUserProfile(cast)
        }

        binding.ivPodcastAvatar.setOnClickListener {
            openUserProfile(cast)
        }

        binding.ivProxyAvatar.setOnClickListener {
            openUserProfile(cast)
        }

        binding.ivAddToPlaylist.throttledClick {
            playlistsViewModel.getPlaylistsOfCasts(cast.id).observe(viewLifecycleOwner){
                val playlistSize = it?.size ?: 0
                if(playlistSize == 0){
                    FragmentCreatePlaylist.createPlaylist(cast.id)
                        .show(parentFragmentManager, SaveToPlaylistFragment.TAG)
                } else{
                    SaveToPlaylistFragment.newInstance(cast.id)
                        .show(parentFragmentManager, SaveToPlaylistFragment.TAG)
                }
            }
        }
    }

    private fun openUserProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.owner?.id)
        startActivity(userProfileIntent)
    }

    var launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (sharedPodcastId != -1) {
                sharePodcastViewModel.share(sharedPodcastId)
                sharedPodcastId = -1
            }
        }

    private fun initRecastState(item: CastUIModel) {
        fun applyRecastState(isRecasted: Boolean) {
            binding.tvPodcastRecast.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isRecasted) R.color.textAccent else R.color.subtitle_text_color
                )
            )
        }
        binding.apply {
            applyRecastState(item.isRecasted!!)
            btnPodcastRecast.recasted = item.isRecasted

            btnPodcastRecast.setOnClickListener {
                val isRecasted = !btnPodcastRecast.recasted
                val recastCount = binding.tvPodcastRecast.text.toString().toInt()

                applyRecastState(isRecasted)
                binding.tvPodcastRecast.text =
                    (if (isRecasted) recastCount + 1 else recastCount - 1).toString()
                binding.btnPodcastRecast.recasted = isRecasted

                updatePodcasts = true

                if (isRecasted) {
                    recastPodcastViewModel.reCast(castId = item.id)
                } else {
                    recastPodcastViewModel.deleteRecast(castId = item.id)
                }

            }
        }
    }

    private fun onHashTagClick(hashtag: TagUIModel) {
        val activity = activity as? PlayerViewManager ?: return

        // 1. minimize the extended player
        activity.showPlayer(
            PlayerViewManager.PlayerArgs(
                PlayerViewManager.PlayerType.SMALL,
                castId,
                castIds
            )
        ) {
            // 2. navigate to hash tag fragment
            activity.navigateToHashTag(hashtag)
        }
    }

    private fun initLikeState(cast: CastUIModel) {
        fun applyLikeStyle(isLiked: Boolean) {
            binding.tvPodcastLikes.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isLiked) R.color.textAccent else R.color.subtitle_text_color
                )
            )
        }

        binding.apply {
            tvPodcastLikes.text = cast.likesCount.toString()
            applyLikeStyle(cast.isLiked!!)
            btnPodcastLikes.isLiked = cast.isLiked

            btnPodcastLikes.setOnClickListener {
                val isLiked = btnPodcastLikes.isLiked
                val likesCount = binding.tvPodcastLikes.text.toString().toInt()

                updatePodcasts = true

                applyLikeStyle(isLiked)
                binding.tvPodcastLikes.text =
                    (if (isLiked) likesCount + 1 else likesCount - 1).toString()

                likePodcastViewModel.likeCast(cast.id, isLiked)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun update() {
        requireArguments().putBoolean(RESTARTED, false)
        requireArguments().putBoolean(AUTO_PLAY_KEY, false)
        podcastViewModel.loadCast(castId)
    }

    override fun getCastId(): Int {
        return castId
    }
}
