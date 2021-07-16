package com.limor.app.scenes.main_new.adapters.vh

import android.content.Intent
import android.widget.TextView
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.loadImage
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel

class ViewHolderPodcast(
    val binding: ItemHomeFeedBinding,
    private val onLikeClick: (castId: Int, like: Boolean) -> Unit,
    private val onCastClick: (cast: CastUIModel) -> Unit,
    private val onRecastClick: (castId: Int) -> Unit,
    private val onCommentsClick: (CastUIModel) -> Unit,
) : ViewHolderBindable<CastUIModel>(binding) {
    override fun bind(item: CastUIModel) {
        setPodcastGeneralInfo(item)
        setPodcastOwnerInfo(item)
        setPodcastCounters(item)
        setAudioInfo(item)
        loadImages(item)
        setOnClicks(item)
        addTags(item)
        initLikeState(item)
        initRecastState(item)
    }

    private fun setPodcastGeneralInfo(item: CastUIModel) {
        binding.tvPodcastLength.text = item.audio?.duration?.let {
            "${it.toMinutes()}m ${it.minusMinutes(it.toMinutes()).seconds}s"
        }
        binding.tvPodcastTitle.text = item.title
        binding.tvPodcastSubtitle.text = item.caption
    }

    private fun setPodcastOwnerInfo(item: CastUIModel) {
        binding.tvPodcastUserName.text = item.owner?.username
        binding.tvPodcastUserSubtitle.text = item.getCreationDateAndPlace(context)
    }

    private fun setPodcastCounters(item: CastUIModel) {
        binding.tvPodcastLikes.text = item.likesCount?.toString()
        binding.tvPodcastRecast.text = item.recastsCount?.toString()
        binding.tvPodcastComments.text = item.commentsCount?.toString()
        binding.tvPodcastReply.text = item.sharesCount?.toString()
        binding.tvPodcastNumberOfListeners.text = item.listensCount?.toString()
    }

    private fun setAudioInfo(item: CastUIModel) {
        binding.cpiPodcastListeningProgress.progress = itemViewType
    }

    private fun loadImages(item: CastUIModel) {
        item.owner?.imageLinks?.small?.let {
            binding.ivPodcastAvatar.loadCircleImage(it)
            binding.ivAvatarImageListening.loadCircleImage(it)
        }

        item.imageLinks?.medium?.let {
            binding.ivPodcastBackground.loadImage(it)
        }
    }

    private fun setOnClicks(item: CastUIModel) {
        binding.btnPodcastMore.setOnClickListener {
            val bundle = bundleOf(DialogPodcastMoreActions.CAST_KEY to item)

            it.findNavController()
                .navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)
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

        binding.btnPodcastRecast.setOnClickListener {
            onRecastClick(item.id)
        }
        binding.btnPodcastComments.setOnClickListener {
            onCommentsClick(item)
        }
    }

    private fun openUserProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.owner?.id)
        context.startActivity(userProfileIntent)
    }

    private fun addTags(item: CastUIModel) {
        item.tags?.forEach {
            addTags(it)
        }
    }

    private fun addTags(tag: TagUIModel) {
        binding.llPodcastTags.removeAllViews()
        AsyncLayoutInflater(binding.root.context)
            .inflate(R.layout.item_podcast_tag, binding.llPodcastTags) { v, _, _ ->
                (v as TextView).text = StringBuilder("#").append(tag.tag)
                binding.llPodcastTags.addView(v)
            }
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

            btnPodcastLikes.setOnClickListener {
                val isLiked = btnPodcastLikes.isLiked
                val likesCount = binding.tvPodcastLikes.text.toString().toInt()

                applyLikeStyle(isLiked)
                binding.tvPodcastLikes.text = (if (isLiked) likesCount + 1 else likesCount - 1).toString()

                onLikeClick(item.id, isLiked)
            }
        }
    }

    private fun initRecastState(item: CastUIModel){
        binding.tvPodcastRecast.text = item.recastsCount.toString()
        binding.btnPodcastRecast.recasted = item.isRecasted == true
        applyRecastStyle(item.isRecasted == true)
    }

    private fun applyRecastStyle(isRecasted : Boolean){
        binding.tvPodcastRecast.setTextColor(
            if(isRecasted) ContextCompat.getColor(
                binding.root.context,
                R.color.textAccent
            ) else
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.white
                )
        )
    }

}