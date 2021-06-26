package com.limor.app.scenes.main_new.adapters.vh

import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.limor.app.FeedItemsQuery
import com.limor.app.R
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.scenes.main_new.utils.ArgsConverter
import com.limor.app.extensions.loadImage

class ViewHolderRecast(val binding: ItemHomeFeedRecastedBinding) :
    ViewHolderBindable(binding) {
    override fun bind(item: FeedItemsQuery.FeedItem) {

        binding.tvRecastUserName.text =
            StringBuilder(item.recaster?.first_name ?: "").append("_")
                .append((item.recaster?.last_name ?: ""))

        binding.tvRecastUserSubtitle.text =
            StringBuilder(item.created_at.toString()).append(" ")
                .append("???")

        binding.tvRecastMessage.text = "???"

        binding.tvRecastPlayCurrentPosition.text = "???"
        binding.tvRecastPlayMaxPosition.text = "???"

        binding.tvPodcastUserName.text =
            StringBuilder(item.podcast?.owner?.first_name ?: "").append("_")
                .append((item.podcast?.owner?.last_name ?: ""))

        binding.tvPodcastUserSubtitle.text =
            StringBuilder(item.podcast?.created_at.toString()).append(" ")
                .append(item.podcast?.address)

        binding.tvPodcastLength.text = item.podcast?.audio?.duration?.toString() ?: ""
        binding.tvPodcastTitle.text = item.podcast?.title ?: ""
        binding.tvPodcastSubtitle.text = item.podcast?.caption ?: ""


        binding.ivPodcastAvatar.loadImage(
            item.podcast?.owner?.images?.small_url ?: ""
        )

        binding.ivRecastAvatar.loadImage(
            item.recaster?.images?.small_url ?: ""
        )

        binding.ivPodcastBackground.loadImage(
            item.podcast?.images?.medium_url ?: ""
        )

        item.podcast?.tags?.caption?.forEach {
            addTags(it)
        }
        binding.btnPodcastMore.setOnClickListener {
            val bundle = bundleOf(
                ArgsConverter.LABEL_DIALOG_REPORT_PODCAST to ArgsConverter.encodeFeedItemAsReportDialogArgs(
                    item
                )
            )

            it.findNavController()
                .navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)
        }
    }

    private fun addTags(caption: FeedItemsQuery.Caption?) {
        binding.llPodcastTags.removeAllViews()
        val tagView = LayoutInflater.from(context)
            .inflate(R.layout.item_podcast_tag, binding.llPodcastTags, false)
        (tagView as TextView).text = caption?.tag ?: ""
        binding.llPodcastTags.addView(tagView)
    }
}