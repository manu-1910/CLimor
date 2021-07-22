package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentExtendedPlayerBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.PodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.uimodels.TagUIModel
import com.limor.app.uimodels.mapToAudioTrack
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class ExtendedPlayerFragment : BaseFragment() {

    companion object {
        private const val CAST_ID_KEY = "CAST_ID_KEY"
        private const val AUTO_PLAY_KEY = "AUTO_PLAY_KEY"
        fun newInstance(castId: Int, autoPlay: Boolean = false): ExtendedPlayerFragment {
            return ExtendedPlayerFragment().apply {
                arguments = bundleOf(CAST_ID_KEY to castId, AUTO_PLAY_KEY to autoPlay)
            }
        }
    }

    private var _binding: FragmentExtendedPlayerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val likePodcastViewModel: LikePodcastViewModel by viewModels { viewModelFactory }
    private val commentsViewModel: CommentsViewModel by viewModels { viewModelFactory }
    private val podcastViewModel: PodcastViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }
    private val castId: Int by lazy { requireArguments()[CAST_ID_KEY] as Int }

    private var playerUpdatesJob: Job? = null

    @Inject
    lateinit var playerBinder: PlayerBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(owner = this) {
            (activity as? PlayerViewManager)?.showPlayer(
                PlayerViewManager.PlayerArgs(
                    PlayerViewManager.PlayerType.SMALL,
                    castId
                )
            )
            isEnabled = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtendedPlayerBinding.inflate(inflater, container, false)
        podcastViewModel.loadCast(castId)
        subscribeToCastUpdates()
        subscribeToCommentUpdates()
        subscribeToRecastUpdate()
        loadFirstComment()
        return binding.root
    }

    private fun loadFirstComment() {
        commentsViewModel.loadComments(castId, limit = 1)
    }

    private fun subscribeToCastUpdates() {
        podcastViewModel.cast.observe(viewLifecycleOwner) { cast ->
            bindViews(cast)
            subscribeToPlayerUpdates(cast)

            if (requireArguments().getBoolean(AUTO_PLAY_KEY, false)) {
                playerBinder.playPause(cast.audio!!.mapToAudioTrack(), true)
                // To prevent autoplay on every cast update
                requireArguments().putBoolean(AUTO_PLAY_KEY, false)
            }
        }
    }

    private fun subscribeToCommentUpdates() {
        commentsViewModel.comments.observe(viewLifecycleOwner) { comments ->
            val firstComment = comments.firstOrNull()
            if (firstComment != null) {
                binding.tvFirstCollapsedComment.text = firstComment.content
                firstComment.user?.imageLinks?.small?.let { imageUrl ->
                    binding.ivAvatarFirstCollapsedComment.loadCircleImage(imageUrl)
                }
                binding.firstCollapsedCommentVisibilityGroup.makeVisible()
                binding.noCommentsMessage.makeGone()
                binding.llExtendCommentsHeader.isEnabled = true
            } else {
                binding.firstCollapsedCommentVisibilityGroup.makeGone()
                binding.noCommentsMessage.makeVisible()
                binding.llExtendCommentsHeader.isEnabled = false
            }
        }

        commentsViewModel.commentAddEvent.observe(viewLifecycleOwner) {
            loadFirstComment()
        }
    }

    private fun subscribeToRecastUpdate() {
        recastPodcastViewModel.recatedResponse.observe(viewLifecycleOwner, {
            binding.tvPodcastRecast.text = it?.count.toString()
            applyRecastStyle(it?.recasted == true)
            binding.btnPodcastRecast.recasted = it?.recasted == true
        })
    }

    private fun bindViews(cast: CastUIModel) {
        setPodcastGeneralInfo(cast)
        setPodcastOwnerInfo(cast)
        setPodcastCounters(cast)
        setAudioInfo(cast)
        loadImages(cast)
        setOnClicks(cast)
        addTags(cast)
        initLikeState(cast)
    }

    private fun setPodcastGeneralInfo(cast: CastUIModel) {
        binding.tvPodcastTitle.text = cast.title
        binding.tvPodcastSubtitle.text = cast.caption
    }

    private fun setPodcastOwnerInfo(cast: CastUIModel) {
        binding.tvPodcastUserName.text = cast.owner?.getFullName()
        binding.tvPodcastUserSubtitle.text = cast.getCreationDateAndPlace(requireContext())
    }

    private fun setPodcastCounters(cast: CastUIModel) {
        binding.tvPodcastLikes.text = cast.likesCount.toString()
        binding.tvPodcastRecast.text = cast.recastsCount?.toString()
        binding.tvPodcastComments.text = cast.commentsCount?.toString()
        binding.tvPodcastNumberOfListeners.text = cast.listensCount?.toString()

        applyRecastStyle(cast.isRecasted == true)
        binding.btnPodcastRecast.recasted = cast.isRecasted == true
    }

    private fun setAudioInfo(cast: CastUIModel) {
        binding.tvRecastPlayMaxPosition.text = cast.audio?.duration?.toReadableFormat(
            DURATION_READABLE_FORMAT_1
        )
    }

    private fun subscribeToPlayerUpdates(cast: CastUIModel) {
        playerUpdatesJob?.cancel()
        playerUpdatesJob = lifecycleScope.launchWhenCreated {
            val audioModel = cast.audio!!.mapToAudioTrack()
            playerBinder.getCurrentPlayingPosition(audioModel)
                .onEach { duration ->
                    binding.lpiPodcastProgress.progress =
                        ((duration.seconds * 100) / audioModel.duration.seconds).toInt()
                    binding.tvRecastPlayCurrentPosition.text =
                        duration.toReadableFormat(DURATION_READABLE_FORMAT_1)
                }
                .launchIn(this)

            playerBinder.getPlayerStatus(audioModel)
                .onEach {
                    when (it) {
                        is PlayerStatus.Ended -> {
                            binding.btnPodcastPlayExtended.setImageResource(R.drawable.ic_play)
                            binding.audioBufferingView.visibility = View.GONE
                        }
                        is PlayerStatus.Error -> binding.audioBufferingView.visibility = View.GONE
                        is PlayerStatus.Buffering -> binding.audioBufferingView.visibility = View.VISIBLE
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
                .launchIn(this)
        }
    }

    private fun loadImages(cast: CastUIModel) {
        cast.owner?.imageLinks?.small?.let {
            binding.ivPodcastAvatar.loadCircleImage(it)
        }

        cast.imageLinks?.medium?.let {
            binding.ivPodcastBackground.loadImage(it)
        }
    }

    private fun setOnClicks(cast: CastUIModel) {
        binding.btnPodcastMore.setOnClickListener {
        }

        binding.btnPodcastPlayExtended.setOnClickListener {
            cast.audio?.let { audio ->
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

        binding.llExtendCommentsHeader.setOnClickListener {
            RootCommentsFragment.newInstance(cast).also { fragment ->
                fragment.show(parentFragmentManager, fragment.requireTag())
            }
        }

        binding.btnPodcastSendComment.setOnClickListener {
            commentsViewModel.addComment(
                cast.id,
                binding.commentText.text.toString(),
                ownerId = cast.id,
                ownerType = CommentUIModel.OWNER_TYPE_PODCAST
            )
            binding.commentText.text = null
            binding.commentText.hideKeyboard()
        }
    }

    private fun applyRecastStyle(recasted: Boolean) {
        binding.tvPodcastRecast.setTextColor(
            if (recasted) {
                ContextCompat.getColor(
                    binding.tvPodcastRecast.context,
                    R.color.textAccent
                )
            } else {
                ContextCompat.getColor(
                    binding.tvPodcastRecast.context,
                    R.color.subtitle_text_color
                )
            }
        )
    }

    private fun addTags(cast: CastUIModel) {
        cast.tags?.forEach {
            addTagsItems(it)
        }
    }

    private fun addTagsItems(tag: TagUIModel) {
        binding.llPodcastTags.removeAllViews()
        AsyncLayoutInflater(requireContext())
            .inflate(R.layout.item_podcast_tag, binding.llPodcastTags) { v, _, _ ->
                (v as TextView).text = StringBuilder("#").append(tag.tag)
                binding.llPodcastTags.addView(v)
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
            btnPodcastLikes.isLiked = cast.isLiked!!

            btnPodcastLikes.setOnClickListener {
                val isLiked = btnPodcastLikes.isLiked
                val likesCount = binding.tvPodcastLikes.text.toString().toInt()

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
}
