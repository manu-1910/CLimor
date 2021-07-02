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
import com.limor.app.scenes.main_new.view_model.PodcastMiniPlayerViewModel

class HomeFeedAdapter(
    private val model: PodcastMiniPlayerViewModel
) : ListAdapter<FeedItemsQuery.FeedItem, ViewHolderBindable>(HomeFeedDiffCallback()) {

    override fun getItemViewType(position: Int): Int {

        val recasted = getItem(position).recasted == true
        return if (recasted) ITEM_TYPE_RECASTED else ITEM_TYPE_PODCAST
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderBindable {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(viewGroup: ViewGroup, viewType: Int): ViewHolderBindable {
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

    override fun onBindViewHolder(viewHolder: ViewHolderBindable, position: Int) {
        viewHolder.bind(getItem(position))
    }

    companion object {
        private const val ITEM_TYPE_PODCAST = 1
        private const val ITEM_TYPE_RECASTED = 2
    }
}

class HomeFeedDiffCallback : DiffUtil.ItemCallback<FeedItemsQuery.FeedItem>() {
    override fun areItemsTheSame(
        oldItem: FeedItemsQuery.FeedItem,
        newItem: FeedItemsQuery.FeedItem
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: FeedItemsQuery.FeedItem,
        newItem: FeedItemsQuery.FeedItem
    ): Boolean {
        return oldItem == newItem
    }
}

