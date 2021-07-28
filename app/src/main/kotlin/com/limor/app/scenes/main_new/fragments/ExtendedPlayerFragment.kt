package com.limor.app.scenes.main_new.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.net.Uri
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentExtendedPlayerBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main.viewmodels.CommentsViewModel
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.PodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.fragments.comments.FragmentComments
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.SendData
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.uimodels.TagUIModel
import com.limor.app.uimodels.mapToAudioTrack
import kotlinx.android.synthetic.main.fragment_extended_player.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.File
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
    private val sharePodcastViewModel: SharePodcastViewModel by viewModels { viewModelFactory }

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
        subscribeToShareUpdate()
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

            val autoPlay = requireArguments().getBoolean(
                AUTO_PLAY_KEY,
                false
            )
            val audioTrack = cast.audio!!.mapToAudioTrack()
            if (autoPlay && playerBinder.currentAudioTrack != audioTrack) {
                playerBinder.playPause(cast.audio.mapToAudioTrack(), true)
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

    private fun subscribeToShareUpdate(){
        sharePodcastViewModel.sharedResponse.observe(viewLifecycleOwner){
            binding.btnPodcastReply.shared = it?.shared == true
        }
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
        binding.btnPodcastReply.shared = cast.isShared == true
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

        binding.btnPodcastReply.setOnClickListener {
            btnPodcastReply.shared = true
            sharePodcast(cast)
        }
        binding.tvPodcastUserName.setOnClickListener {
            openUserProfile(cast)
        }

        binding.ivPodcastAvatar.setOnClickListener {
            openUserProfile(cast)
        }

        // This is copy pasted from FragmentComments, will need to be refactored later...
        binding.taviVoice.initListenerStatus {
            when(it) {
                is SendData -> {

                    if (it.filePath != null) {
                        Commons.getInstance().uploadAudio(
                            context,
                            File(it.filePath),
                            Constants.AUDIO_TYPE_COMMENT,
                            object : Commons.AudioUploadCallback {
                                override fun onSuccess(audioUrl: String?) {
                                    commentsViewModel.addComment(
                                        cast.id,
                                        content = it.text,
                                        ownerId = cast.id,
                                        ownerType = CommentUIModel.OWNER_TYPE_PODCAST,
                                        audioURI = audioUrl,
                                        duration = it.duration
                                    )
                                }

                                override fun onProgressChanged(
                                    id: Int,
                                    bytesCurrent: Long,
                                    bytesTotal: Long
                                ) {
                                }

                                override fun onError(error: String?) {
                                    Timber.d("Audio upload to AWS error: $error")
                                }
                            })
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
    }

    var launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK){
            val intent = result.data
            val podcastId = intent?.getIntExtra(Constants.SHARED_PODCAST_ID, -1) ?: -1
            sharePodcastViewModel.share(podcastId)
        }
    }

    val sharePodcast : (CastUIModel) -> Unit =  { cast ->

        val podcastLink = Constants.PODCAST_URL.format(cast.id)

        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse(podcastLink)
            domainUriPrefix = Constants.LIMER_DOMAIN_URL
            androidParameters(BuildConfig.APPLICATION_ID) {
                fallbackUrl = Uri.parse(Constants.DOMAIN_URL)
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

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, cast.title)
            putExtra(Intent.EXTRA_TEXT, dynamicLink.uri.toString())
            putExtra(Constants.SHARED_PODCAST_ID, cast.id)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        try{
            launcher.launch(shareIntent)
        } catch (e: ActivityNotFoundException){}

    }

    private fun openUserProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.owner?.id)
        startActivity(userProfileIntent)
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
