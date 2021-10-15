package com.limor.app.scenes.main_new.adapters.vh

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.dm.ShareResult
import com.limor.app.extensions.*
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel

class ViewHolderPodcast(
    val binding: ItemHomeFeedBinding,
    private val onLikeClick: (castId: Int, like: Boolean) -> Unit,
    private val onCastClick: (cast: CastUIModel) -> Unit,
    private val onRecastClick: (castId: Int, isRecasted: Boolean) -> Unit,
    private val onCommentsClick: (CastUIModel) -> Unit,
    private val onShareClick: (CastUIModel, onShared: ((shareResult: ShareResult) -> Unit)?) -> Unit,
    private val onReloadData: (castId: Int, reload: Boolean) -> Unit,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit,
    private val onUserMentionClick: (username: String, userId: Int) -> Unit,
) : ViewHolderBindable<CastUIModel>(binding) {
    override fun bind(item: CastUIModel) {
        setPodcastGeneralInfo(item)
        setPodcastOwnerInfo(item)
        setPodcastCounters(item)
        setAudioInfo(item)
        loadImages(item)
        setOnClicks(item)
        initLikeState(item)
        initRecastState(item)
        initShareState(item)
    }

    private fun setPodcastGeneralInfo(item: CastUIModel) {
        binding.tvPodcastLength.text = item.audio?.duration?.let {
            "${it.toMinutes()}m ${it.minusMinutes(it.toMinutes()).seconds}s"
        }
        binding.tvPodcastTitle.text = item.title
        binding.tvPodcastSubtitle.setTextWithTagging(
            item.caption,
            item.mentions,
            item.tags,
            onUserMentionClick,
            onHashTagClick
        )
    }

    private fun setPodcastOwnerInfo(item: CastUIModel) {
        binding.tvPodcastUserName.text = item.owner?.username
        binding.tvPodcastUserSubtitle.text = item.getCreationDateAndPlace(context, true)
        binding.ivVerifiedAvatar.visibility = if(item.owner?.isVerified == true) View.VISIBLE else View.GONE
    }

    private fun setPodcastCounters(item: CastUIModel) {
        binding.tvPodcastLikes.text = item.likesCount?.toString()
        binding.tvPodcastRecast.text = item.recastsCount?.toString()
        binding.tvPodcastComments.text = item.commentsCount?.toString()
        binding.tvPodcastReply.text = item.sharesCount?.toString()
        binding.tvPodcastNumberOfListeners.text = if(item.listensCount == 0) "0" else item.listensCount?.toLong()?.formatHumanReadable
    }

    private fun setAudioInfo(item: CastUIModel) {
        binding.cpiPodcastListeningProgress.progress = itemViewType
    }

    private fun loadImages(item: CastUIModel) {
        item.owner?.getAvatarUrl()?.let {
            binding.ivPodcastAvatar.loadCircleImage(it)
            binding.ivAvatarImageListening.loadCircleImage(it)
        }

        item.imageLinks?.large?.let {
            binding.ivPodcastBackground.loadImage(it)
        }
    }

    private fun setOnClicks(item: CastUIModel) {
        binding.btnPodcastMore.setOnClickListener {
            val bundle = bundleOf(DialogPodcastMoreActions.CAST_KEY to item)
            val navController = it.findNavController()
            it.findViewTreeLifecycleOwner()?.let{
                ownerLife ->
                navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("reload_feed")?.observe(
                    ownerLife
                ){
                    onReloadData.invoke(item.id,true)
                }
                navController.navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)

            }

        }

        binding.clItemPodcastFeed.setOnClickListener {
            onCastClick(item)
        }

        binding.tvPodcastUserName.setOnClickListener {
            openUserProfile(item)
        }
        binding.ivPodcastAvatar.setOnClickListener {
            openUserProfile(item)
        }

        binding.btnPodcastComments.throttledClick {
            onCommentsClick(item)
        }

        binding.tvPodcastComments.throttledClick {
            onCommentsClick(item)
        }

        binding.sharesLayout.setOnClickListener {
            onShareClick(item) { shareResult ->
                item.updateShares(shareResult)
                initShareState(item)
            }
        }
    }

    private fun openUserProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.owner?.id)
        context.startActivity(userProfileIntent)
    }

    private fun initLikeState(item: CastUIModel) {
        fun applyLikeStyle(isLiked: Boolean) {
            binding.tvPodcastLikes.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isLiked) R.color.textAccent else R.color.white
                )
            )
        }

        binding.apply {
            applyLikeStyle(item.isLiked!!)
            btnPodcastLikes.isLiked = item.isLiked

            likeLayout.setOnClickListener {
                btnPodcastLikes.isLiked = !btnPodcastLikes.isLiked
                val isLiked = btnPodcastLikes.isLiked
                val likesCount = binding.tvPodcastLikes.text.toString().toInt()

                applyLikeStyle(isLiked)
                binding.tvPodcastLikes.text =
                    (if (isLiked) likesCount + 1 else likesCount - 1).toString()

                onLikeClick(item.id, isLiked)
            }
        }
    }

    private fun initRecastState(item: CastUIModel) {
        fun applyRecastState(isRecasted: Boolean) {
            binding.tvPodcastRecast.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isRecasted) R.color.textAccent else R.color.white
                )
            )
        }
        binding.apply {
            applyRecastState(item.isRecasted!!)
            btnPodcastRecast.recasted = item.isRecasted

            recastLayout.setOnClickListener {
                val isRecasted = !btnPodcastRecast.recasted
                val recastCount = binding.tvPodcastRecast.text.toString().toInt()

                applyRecastState(isRecasted)
                binding.tvPodcastRecast.text =
                    (if (isRecasted) recastCount + 1 else recastCount - 1).toString()
                binding.btnPodcastRecast.recasted = isRecasted

                onRecastClick(item.id, isRecasted)
            }
        }
    }

    private fun applyRecastStyle(isRecasted: Boolean) {
        binding.tvPodcastRecast.setTextColor(
            if (isRecasted) ContextCompat.getColor(
                binding.root.context,
                R.color.textAccent
            ) else
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.white
                )
        )
    }

    private fun initShareState(item: CastUIModel) {
        binding.tvPodcastReply.text = item.sharesCount.toString()
        binding.btnPodcastReply.shared = item.isShared == true
        applySharedState(item.isShared == true)
    }

    private fun applySharedState(isShared: Boolean) {
        binding.tvPodcastReply.setTextColor(
            if (isShared) {
                ContextCompat.getColor(binding.root.context, R.color.textAccent)
            } else {
                ContextCompat.getColor(binding.root.context, R.color.white)
            }
        )
    }

}