package com.limor.app.scenes.patron.manage.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.databinding.ItemCastEarningsBinding
import com.limor.app.databinding.ItemLoadMoreBinding
import com.limor.app.scenes.main_new.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable

class CastEarningsAdapter(
    private val onLoadMore: () -> Unit,
    private val onClick: () -> Unit
) : ListAdapter<String, RecyclerView.ViewHolder>(
    CastEarningDiffCallback()
) {
    var loadMore = false
    var isLoading = false
        set(value) {
            field = value
            if (loadMore) {
                notifyItemChanged(itemCount)
            }
        }

    private fun getViewHolderByViewType(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        return when (viewType) {
            CastEarningsAdapter.ITEM_TYPE_CAST_EARNINGS -> {
                val binding =
                    ItemCastEarningsBinding.inflate(inflater, viewGroup, false)
                CastEarningViewHolder(binding, onClick = onClick)
            }
            else -> {
                val binding = ItemLoadMoreBinding.inflate(inflater, viewGroup, false)
                ViewHolderLoadMore(binding, onLoadMore = onLoadMore)
            }
        }
    }

    override fun getItemCount(): Int {
        val superCount = super.getItemCount()
        if (superCount == 0) {
            return 0
        }

        return superCount + if (loadMore) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return getViewHolderByViewType(parent, viewType)
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (getItemViewType(position) == CastEarningsAdapter.ITEM_TYPE_LOAD_MORE) {
            val vh = viewHolder as ViewHolderLoadMore
            vh.isEnabled = !isLoading
            return
        }
        // this is bad but this "load more" button will be replaced by automatic continuous loading
        // so we don't need to have the greatest arch here..
        (viewHolder as ViewHolderBindable<String>).bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMore && position > 0 && position == itemCount - 1) {
            return CastEarningsAdapter.ITEM_TYPE_LOAD_MORE
        }
        return CastEarningsAdapter.ITEM_TYPE_CAST_EARNINGS
    }

    companion object {
        private const val ITEM_TYPE_CAST_EARNINGS = 1
        private const val ITEM_TYPE_LOAD_MORE = 2
    }

}

class CastEarningViewHolder(
    val binding: ItemCastEarningsBinding,
    val onClick: () -> Unit
) : ViewHolderBindable<String>(binding) {
    override fun bind(name: String) {
        binding.castNameTextView.text = name
        binding.root.setOnClickListener {
            onClick()
        }
    }
}

class CastEarningDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }
}