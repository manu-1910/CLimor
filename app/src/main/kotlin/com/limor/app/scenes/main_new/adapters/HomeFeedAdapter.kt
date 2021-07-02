package com.limor.app.scenes.main_new.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.limor.app.FeedItemsQuery
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderPodcast
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderRecast
import com.limor.app.scenes.main_new.view_model.PodcastControlViewModel

class HomeFeedAdapter(
    private val model: PodcastControlViewModel
) : ListAdapter<FeedItemsQuery.GetFeedItem, ViewHolderBindable<FeedItemsQuery.GetFeedItem>>(
    HomeFeedDiffCallback()
) {

    override fun getItemViewType(position: Int): Int {

        val recasted = getItem(position).recasted == true
        return if (recasted) ITEM_TYPE_RECASTED else ITEM_TYPE_PODCAST
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<FeedItemsQuery.GetFeedItem> {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<FeedItemsQuery.GetFeedItem> {
        val inflater = LayoutInflater.from(viewGroup.context)
        return when (viewType) {
            ITEM_TYPE_RECASTED -> {
                val binding =
                    ItemHomeFeedRecastedBinding.inflate(inflater, viewGroup, false)
                ViewHolderRecast(binding, model)
            }
            else -> {
                val binding = ItemHomeFeedBinding.inflate(inflater, viewGroup, false)
                ViewHolderPodcast(binding, model)
            }
        }
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolderBindable<FeedItemsQuery.GetFeedItem>,
        position: Int
    ) {
        viewHolder.bind(getItem(position))
    }

    companion object {
        private const val ITEM_TYPE_PODCAST = 1
        private const val ITEM_TYPE_RECASTED = 2
    }
}

class HomeFeedDiffCallback : DiffUtil.ItemCallback<FeedItemsQuery.GetFeedItem>() {
    override fun areItemsTheSame(
        oldItem: FeedItemsQuery.GetFeedItem,
        newItem: FeedItemsQuery.GetFeedItem
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: FeedItemsQuery.GetFeedItem,
        newItem: FeedItemsQuery.GetFeedItem
    ): Boolean {
        return oldItem == newItem
    }
}

