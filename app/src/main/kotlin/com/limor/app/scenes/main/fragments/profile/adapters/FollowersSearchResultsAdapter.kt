package com.limor.app.scenes.main.fragments.profile.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.FollowersQuery
import com.limor.app.SearchFollowersQuery
import com.limor.app.databinding.ItemLoadMoreBinding
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable

class FollowersSearchResultsAdapter() :
    ListAdapter<SearchFollowersQuery.SearchFollower, RecyclerView.ViewHolder>(
        FollowerDiffCallback()
    ) {

    var loadMore = false
    var isLoading = false
        set(value) {
            field = value
            if (loadMore) {
                notifyItemChanged(itemCount)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        if (count == 0) {
            return 0
        }
        return count + if (loadMore) 1 else 0
    }
}

class FollowerDiffCallback : DiffUtil.ItemCallback<SearchFollowersQuery.SearchFollower>() {
    override fun areItemsTheSame(
        oldItem: SearchFollowersQuery.SearchFollower,
        newItem: SearchFollowersQuery.SearchFollower
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: SearchFollowersQuery.SearchFollower,
        newItem: SearchFollowersQuery.SearchFollower
    ): Boolean {
        return oldItem == newItem
    }

}

class ViewHolderLoadMore(
    val binding: ItemLoadMoreBinding,
    private val onLoadMore: () -> Unit
) : ViewHolderBindable<Unit>(binding) {

    var isEnabled = true
        set(value) {
            field = value
            updateStyle()
        }

    init {
        binding.loadMore.setOnClickListener {
            if (isEnabled) {
                isEnabled = false
                updateStyle()
                onLoadMore()
            }
        }
    }

    override fun bind(item: Unit) {
        updateStyle()
    }

    private fun updateStyle() {
        binding.loadMore.alpha = if (isEnabled) 1.0f else 0.3f
    }

}