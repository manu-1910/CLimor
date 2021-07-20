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
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.uimodels.TagUIModel
import com.limor.app.uimodels.mapToAudioTrack
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class ExtendedPlayerFragment : BaseFragment() {

    companion object {
        private const val CAST_KEY = "CAST_KEY"
        fun newInstance(cast: CastUIModel): ExtendedPlayerFragment {
            return ExtendedPlayerFragment().apply {
                arguments = bundleOf(CAST_KEY to cast)
            }
        }
    }

    private var _binding: FragmentExtendedPlayerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val likePodcastViewModel: LikePodcastViewModel by viewModels { viewModelFactory }
    private val commentsViewModel: CommentsViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }
    private val podcast: CastUIModel by lazy { requireArguments()[CAST_KEY] as CastUIModel }
    private val podcastAudio get() = podcast.audio!!.mapToAudioTrack()

    @Inject
    lateinit var playerBinder: PlayerBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(owner = this) {
            (activity as? PlayerViewManager)?.showPlayer(
                PlayerViewManager.PlayerArgs(
                    PlayerViewManager.PlayerType.SMALL,
                    podcast
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
        bindViews()
        subscribeToPlayerUpdates()
        subscribeToCommentUpdates()
        subscribeToRecastUpdate()
        loadFirstComment()
        return binding.root
    }

    private fun loadFirstComment() {
        commentsViewModel.loadComments(podcast.id, limit = 1)
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

    private fun bindViews() {
        setPodcastGeneralInfo()
        setPodcastOwnerInfo()
        setPodcastCounters()
        setAudioInfo()
        loadImages()
        setOnClicks()
        addTags()
        initLikeState()
    }

    private fun setPodcastGeneralInfo() {
        binding.tvPodcastTitle.text = podcast.title
        binding.tvPodcastSubtitle.text = podcast.caption
    }

    private fun setPodcastOwnerInfo() {
        binding.tvPodcastUserName.text = podcast.owner?.getFullName()
        binding.tvPodcastUserSubtitle.text = podcast.getCreationDateAndPlace(requireContext())
    }

    private fun setPodcastCounters() {
        binding.tvPodcastLikes.text = podcast.likesCount.toString()
        binding.tvPodcastRecast.text = podcast.recastsCount?.toString()
        binding.tvPodcastComments.text = podcast.commentsCount?.toString()
        binding.tvPodcastNumberOfListeners.text = podcast.listensCount?.toString()

        applyRecastStyle(podcast.isRecasted == true)
        binding.btnPodcastRecast.recasted = podcast.isRecasted == true
    }

    private fun setAudioInfo() {
        binding.tvRecastPlayMaxPosition.text = podcast.audio?.duration?.toReadableFormat(
            DURATION_READABLE_FORMAT_1
        )
    }

    private fun subscribeToPlayerUpdates() {
        lifecycleScope.launchWhenCreated {
            playerBinder.getCurrentPlayingPosition(podcastAudio)
                .onEach { duration ->
                    binding.lpiPodcastProgress.progress =
                        ((duration.seconds * 100) / podcast.audio?.duration?.seconds!!).toInt()
                    binding.tvRecastPlayCurrentPosition.text =
                        duration.toReadableFormat(DURATION_READABLE_FORMAT_1)
                }
                .launchIn(this)

            playerBinder.getPlayerStatus(podcastAudio)
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

    private fun loadImages() {
        podcast.owner?.imageLinks?.small?.let {
            binding.ivPodcastAvatar.loadCircleImage(it)
        }

        podcast.imageLinks?.medium?.let {
            binding.ivPodcastBackground.loadImage(it)
        }
    }

    private fun setOnClicks() {
        binding.btnPodcastMore.setOnClickListener {
        }

        binding.btnPodcastPlayExtended.setOnClickListener {
            podcast.audio?.let { audio ->
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
            recastPodcastViewModel.reCast(castId = podcast.id)
        }

        binding.llExtendCommentsHeader.setOnClickListener {
            RootCommentsFragment.newInstance(podcast).also { fragment ->
                fragment.show(parentFragmentManager, fragment.requireTag())
            }
        }

        binding.btnPodcastSendComment.setOnClickListener {
            commentsViewModel.addComment(
                podcast.id,
                binding.commentText.text.toString(),
                ownerId = podcast.id,
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

    private fun addTags() {
        podcast.tags?.forEach {
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

    private fun initLikeState() {
        fun applyLikeStyle(isLiked: Boolean) {
            binding.tvPodcastLikes.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isLiked) R.color.textAccent else R.color.subtitle_text_color
                )
            )
        }

        binding.apply {
            tvPodcastLikes.text = podcast.likesCount.toString()
            applyLikeStyle(podcast.isLiked!!)
            btnPodcastLikes.isLiked = podcast.isLiked!!

            btnPodcastLikes.setOnClickListener {
                val isLiked = btnPodcastLikes.isLiked
                val likesCount = binding.tvPodcastLikes.text.toString().toInt()

                applyLikeStyle(isLiked)
                binding.tvPodcastLikes.text =
                    (if (isLiked) likesCount + 1 else likesCount - 1).toString()

                likePodcastViewModel.likeCast(podcast.id, isLiked)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
