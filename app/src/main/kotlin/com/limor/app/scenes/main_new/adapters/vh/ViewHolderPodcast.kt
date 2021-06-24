package com.limor.app.scenes.main_new.adapters.vh

import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.limor.app.FeedItemsQuery
import com.limor.app.R
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.scenes.main_new.utils.ArgsConverter
import com.limor.app.scenes.main_new.utils.ArgsConverter.Companion.LABEL_DIALOG_REPORT_PODCAST
import com.limor.app.util.GlideHelper

class ViewHolderPodcast(val binding: ItemHomeFeedBinding) : ViewHolderBindable(binding) {
    override fun bind(item: FeedItemsQuery.FeedItem) {
        binding.cpiPodcastListeningProgress.progress = itemViewType

        binding.tvPodcastUserName.text =
            StringBuilder(item.podcast?.owner?.first_name ?: "").append("_")
                .append((item.podcast?.owner?.last_name ?: ""))

        binding.tvPodcastUserSubtitle.text =
            StringBuilder(item.podcast?.created_at.toString()).append(" ")
                .append(item.podcast?.address)

        binding.tvPodcastLikes.text = item.podcast?.number_of_likes?.toString() ?: ""
        binding.tvPodcastRecast.text = item.podcast?.number_of_recasts?.toString() ?: ""
        binding.tvPodcastComments.text = item.podcast?.number_of_comments?.toString() ?: ""
        binding.tvPodcastReply.text = item.podcast?.number_of_shares?.toString() ?: ""
        binding.tvPodcastNumberOfListeners.text = item.podcast?.number_of_listens?.toString() ?: ""

        binding.tvPodcastLength.text = item.podcast?.audio?.duration?.toString() ?: ""
        binding.tvPodcastTitle.text = item.podcast?.title ?: ""
        binding.tvPodcastSubtitle.text = item.podcast?.caption ?: ""


        GlideHelper.loadImageSimple(
            binding.ivPodcastAvatar,
            item.podcast?.owner?.images?.small_url ?: ""
        )

        GlideHelper.loadImageSimple(
            binding.ivAvatarImageListening,
            item.podcast?.owner?.images?.small_url ?: ""
        )

        GlideHelper.loadImageSimple(
            binding.ivPodcastBackground,
            item.podcast?.images?.medium_url ?: ""
        )

        item.podcast?.tags?.caption?.forEach {
            addTags(it)
        }
        binding.btnPodcastMore.setOnClickListener {
            val bundle = bundleOf(
                LABEL_DIALOG_REPORT_PODCAST to ArgsConverter.encodeFeedItemAsReportDialogArgs(item)
            )

            it.findNavController().navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)
        }
    }

    private fun addTags(caption: FeedItemsQuery.Caption?) {
        binding.llPodcastTags.removeAllViews()
        val tagView = LayoutInflater.from(context)
            .inflate(R.layout.item_podcast_tag, binding.llPodcastTags, false)
        (tagView as TextView).text = caption?.tag ?:""
        binding.llPodcastTags.addView(tagView)
    }
}