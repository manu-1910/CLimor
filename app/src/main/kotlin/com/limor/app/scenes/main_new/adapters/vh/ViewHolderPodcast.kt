package com.limor.app.scenes.main_new.adapters.vh

import android.widget.TextView
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.limor.app.FeedItemsQuery
import com.limor.app.R
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.extensions.loadImage
import com.limor.app.scenes.main_new.utils.ArgsConverter
import com.limor.app.scenes.main_new.utils.ArgsConverter.Companion.LABEL_DIALOG_REPORT_PODCAST
import com.limor.app.scenes.main_new.view_model.PodcastMiniPlayerViewModel

class ViewHolderPodcast(val binding: ItemHomeFeedBinding,val model: PodcastMiniPlayerViewModel) : ViewHolderBindable(binding) {
    override fun bind(item: FeedItemsQuery.FeedItem) {
        setPodcastGeneralInfo(item)
        setPodcastOwnerInfo(item)
        setPodcastCounters(item)
        setAudioInfo(item)
        loadImages(item)
        setOnClicks(item)
        addTags(item)
    }

    private fun setPodcastGeneralInfo(item: FeedItemsQuery.FeedItem) {
        binding.tvPodcastLength.text = item.podcast?.audio?.duration?.toString() ?: ""
        binding.tvPodcastTitle.text = item.podcast?.title ?: ""
        binding.tvPodcastSubtitle.text = item.podcast?.caption ?: ""
    }

    private fun setPodcastOwnerInfo(item: FeedItemsQuery.FeedItem) {
        binding.tvPodcastUserName.text =
            StringBuilder(item.podcast?.owner?.first_name ?: "").append("_")
                .append((item.podcast?.owner?.last_name ?: ""))

        binding.tvPodcastUserSubtitle.text =
            StringBuilder(item.podcast?.created_at.toString()).append(" ")
                .append(item.podcast?.address)
    }

    private fun setPodcastCounters(item: FeedItemsQuery.FeedItem) {
        binding.tvPodcastLikes.text = item.podcast?.number_of_likes?.toString() ?: ""
        binding.tvPodcastRecast.text = item.podcast?.number_of_recasts?.toString() ?: ""
        binding.tvPodcastComments.text = item.podcast?.number_of_comments?.toString() ?: ""
        binding.tvPodcastReply.text = item.podcast?.number_of_shares?.toString() ?: ""
        binding.tvPodcastNumberOfListeners.text = item.podcast?.number_of_listens?.toString() ?: ""
    }

    private fun setAudioInfo(item: FeedItemsQuery.FeedItem) {
        binding.cpiPodcastListeningProgress.progress = itemViewType
    }

    private fun loadImages(item: FeedItemsQuery.FeedItem) {
        binding.ivPodcastAvatar.loadImage(
            item.podcast?.owner?.images?.small_url ?: ""
        )

        binding.ivAvatarImageListening.loadImage(
            item.podcast?.owner?.images?.small_url ?: ""
        )

        binding.ivPodcastBackground.loadImage(
            item.podcast?.images?.medium_url ?: ""
        )
    }

    private fun setOnClicks(item: FeedItemsQuery.FeedItem) {
        binding.btnPodcastMore.setOnClickListener {
            val bundle = bundleOf(
                LABEL_DIALOG_REPORT_PODCAST to ArgsConverter.encodeFeedItemAsReportDialogArgs(item)
            )

            it.findNavController()
                .navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)
        }
        binding.clItemPodcastFeed.setOnClickListener {
            model.changePodcastFullScreenVisibility(item)
//            it.findNavController()
//                .navigate(R.id.action_navigation_home_to_navigation_podcast_activity)
        }
    }

    private fun addTags(item: FeedItemsQuery.FeedItem) {
        item.podcast?.tags?.caption?.forEach {
            addTags(it)
        }
    }

    private fun addTags(caption: FeedItemsQuery.Caption?) {
        binding.llPodcastTags.removeAllViews()
        AsyncLayoutInflater(binding.root.context)
            .inflate(R.layout.item_podcast_tag, binding.llPodcastTags) { v, _, _ ->
                (v as TextView).text = StringBuilder("#").append(caption?.tag ?: "")
                binding.llPodcastTags.addView(v)
            }
    }
}