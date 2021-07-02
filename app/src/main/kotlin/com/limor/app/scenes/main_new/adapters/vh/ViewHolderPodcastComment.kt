package com.limor.app.scenes.main_new.adapters.vh

import com.limor.app.GetCommentsByPodcastsQuery
import com.limor.app.databinding.ItemPodcastCommentBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.main_new.view_model.PodcastControlViewModel

class ViewHolderPodcastComment(
    val binding: ItemPodcastCommentBinding,
    val model: PodcastControlViewModel
) :
    ViewHolderBindable<GetCommentsByPodcastsQuery.GetCommentsByPodcast>(binding) {

    override fun bind(item: GetCommentsByPodcastsQuery.GetCommentsByPodcast) {
        setText(item)
        loadImages(item)
    }

    private fun setText(item: GetCommentsByPodcastsQuery.GetCommentsByPodcast) {
        binding.tvCommentName.text = StringBuilder(item.user?.first_name ?: "").append(" ")
            .append(item.user?.last_name ?: "")
        binding.tvCommentDate.text = item.created_at
        binding.tvCommentContent.text = item.content
    }

    private fun loadImages(item: GetCommentsByPodcastsQuery.GetCommentsByPodcast){
        binding.ivCommentAvatar.loadCircleImage(item.user?.images?.small_url?:"")
    }
}