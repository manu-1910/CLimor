package com.limor.app.scenes.main_new.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.limor.app.GetCommentsByPodcastsQuery
import com.limor.app.databinding.ItemPodcastCommentBinding
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderPodcastComment
import com.limor.app.scenes.main_new.view_model.PodcastControlViewModel

class PodcastCommentsAdapter(
    private val model: PodcastControlViewModel
) : ListAdapter<GetCommentsByPodcastsQuery.GetCommentsByPodcast, ViewHolderBindable<GetCommentsByPodcastsQuery.GetCommentsByPodcast>>(
    CommentsDiffCallback()
) {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<GetCommentsByPodcastsQuery.GetCommentsByPodcast> {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<GetCommentsByPodcastsQuery.GetCommentsByPodcast> {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = ItemPodcastCommentBinding.inflate(inflater, viewGroup, false)
        return ViewHolderPodcastComment(binding, model)
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolderBindable<GetCommentsByPodcastsQuery.GetCommentsByPodcast>,
        position: Int
    ) {
        viewHolder.bind(getItem(position))
    }
}

class CommentsDiffCallback :
    DiffUtil.ItemCallback<GetCommentsByPodcastsQuery.GetCommentsByPodcast>() {
    override fun areItemsTheSame(
        oldItem: GetCommentsByPodcastsQuery.GetCommentsByPodcast,
        newItem: GetCommentsByPodcastsQuery.GetCommentsByPodcast
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: GetCommentsByPodcastsQuery.GetCommentsByPodcast,
        newItem: GetCommentsByPodcastsQuery.GetCommentsByPodcast
    ): Boolean {
        return oldItem == newItem
    }
}

