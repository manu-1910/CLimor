package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentExtendedPlayerBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.auth_new.util.colorStateList
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.uimodels.TagUIModel
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
    private val podcast: CastUIModel by lazy { requireArguments()[CAST_KEY] as CastUIModel }

    private val playerBinder: PlayerBinder by lazy { (requireActivity() as PlayerViewManager).getPlayerBinder() }

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

    private fun bindViews() {
        setPodcastGeneralInfo()
        setPodcastOwnerInfo()
        setPodcastCounters()
        setAudioInfo()
        loadImages()
        setOnClicks()
        addTags()
        setActiveIcons()
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
    }

    private fun setAudioInfo() {
        binding.tvRecastPlayMaxPosition.text = podcast.audio?.duration?.toString() ?: ""
    }

    private fun subscribeToPlayerUpdates() {
        playerBinder.currentPlayPositionLiveData.observe(viewLifecycleOwner) {
            binding.lpiPodcastProgress.progress = it.second
            binding.tvRecastPlayCurrentPosition.text = (it.first).toString()
        }

        playerBinder.playerStatusLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is PlayerStatus.Cancelled -> Timber.d("Player Canceled")
                is PlayerStatus.Ended -> binding.btnPodcastPlayExtended.setImageResource(R.drawable.ic_player_play)
                is PlayerStatus.Error -> Timber.d("Player Error")
                is PlayerStatus.Other -> Timber.d("Player Other")
                is PlayerStatus.Paused -> binding.btnPodcastPlayExtended.setImageResource(R.drawable.ic_player_play)
                is PlayerStatus.Playing -> binding.btnPodcastPlayExtended.setImageResource(R.drawable.pause)
            }
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
            playerBinder.playPause()
        }

        binding.btnPodcastRewindBack.setOnClickListener {
            playerBinder.rewind(5000L)
        }

        binding.btnPodcastRewindForward.setOnClickListener {
            playerBinder.forward(5000L)
        }

        binding.btnPodcastLikes.setOnClickListener {
            likePodcastViewModel.likeCast(podcast.id, binding.btnPodcastLikes.isLiked)
        }

        binding.llExtendCommentsHeader.setOnClickListener {
            val fragment = FragmentComments.newInstance(podcast)
            parentFragmentManager.beginTransaction()
                .addToBackStack(FragmentComments.TAG)
                .replace(R.id.fragment_comments_container, fragment, FragmentComments.TAG)
                .commit()
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

    private fun setActiveIcons() {
        val tint = colorStateList(
            binding.root.context,
            if (podcast.isLiked == true) R.color.colorAccent else R.color.subtitle_text_color
        )
        binding.btnPodcastLikes.isLiked = podcast.isLiked!!
        binding.tvPodcastLikes.setTextColor(tint)
    }
}