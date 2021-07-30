package com.limor.app.scenes.main_new.adapters.vh

import android.content.Intent
import android.widget.TextView
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.extensions.getActivity
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.loadImage
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel

class ViewHolderRecast(
    val binding: ItemHomeFeedRecastedBinding
) : ViewHolderBindable<CastUIModel>(binding) {
    override fun bind(item: CastUIModel) {

        binding.tvRecastUserName.text = item.recaster?.username
        binding.tvRecastUserSubtitle.text = item.getCreationDateAndPlace(context)

        binding.tvRecastMessage.text = ""

        binding.tvRecastPlayCurrentPosition.text = "???"
        binding.tvRecastPlayMaxPosition.text = "???"

        binding.tvPodcastUserName.text = item.owner?.username

        binding.tvPodcastUserSubtitle.text = item.getCreationDateAndPlace(context)

        binding.tvPodcastLength.text = item.audio?.duration?.let {
            "${it.toMinutes()}m ${it.minusMinutes(it.toMinutes()).seconds}s"
        }
        binding.tvPodcastTitle.text = item.title
        binding.tvPodcastSubtitle.text = item.caption


        item.owner?.imageLinks?.small?.let {
            binding.ivPodcastAvatar.loadCircleImage(it)
        }

        item.recaster?.imageLinks?.small?.let {
            binding.ivRecastAvatar.loadCircleImage(it)
        }

        item.imageLinks?.medium?.let {
            binding.ivPodcastBackground.loadImage(it)
        }

        addTags(item)

        setPodcastCounters(item)
        setInterationStatus(item)

        binding.btnPodcastMore.setOnClickListener {
            val bundle = bundleOf(DialogPodcastMoreActions.CAST_KEY to item)

            it.findNavController()
                .navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)
        }

        binding.ivRecastAvatar.setOnClickListener {
            openRecasterProfile(item)
        }
        binding.tvRecastUserName.setOnClickListener {
            openRecasterProfile(item)
        }

        binding.ivPodcastAvatar.setOnClickListener {
            openUserProfile(item)
        }
        binding.tvPodcastUserName.setOnClickListener {
            openUserProfile(item)
        }

        binding.castCard.setOnClickListener {
            onCastClick(item)
        }
    }

    private fun setPodcastCounters(item: CastUIModel) {
        binding.tvPodcastLikes.text = item.likesCount?.toString()
        binding.tvPodcastRecast.text = item.recastsCount?.toString()
        binding.tvPodcastComments.text = item.commentsCount?.toString()
        binding.tvPodcastNumberOfListeners.text = item.listensCount?.toString()
    }

    private fun setInterationStatus(item: CastUIModel){
        binding.btnPodcastLikes.isLiked = item.isLiked ?: false
        binding.tvPodcastLikes.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                if (item.isLiked == true) R.color.textAccent else R.color.subtitle_text_color
            )
        )
        binding.btnPodcastRecast.recasted = item.isRecasted ?: false
        binding.tvPodcastRecast.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                if (item.isRecasted == true) R.color.textAccent else R.color.subtitle_text_color
            )
        )
        binding.btnPodcastReply.shared = item.isShared ?: false
    }

    private fun onCastClick(item: CastUIModel) {
        (binding.root.context.getActivity() as? PlayerViewManager)?.showExtendedPlayer(item.id)
    }

    private fun openUserProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.owner?.id)
        context.startActivity(userProfileIntent)
    }

    private fun openRecasterProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.recaster?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.recaster?.id)
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
}