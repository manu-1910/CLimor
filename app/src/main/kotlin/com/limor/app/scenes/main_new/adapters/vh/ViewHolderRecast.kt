package com.limor.app.scenes.main_new.adapters.vh

import android.widget.TextView
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.loadImage
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
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

        binding.btnPodcastMore.setOnClickListener {
            val bundle = bundleOf(DialogPodcastMoreActions.CAST_ID_KEY to item.id)

            it.findNavController()
                .navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)
        }
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