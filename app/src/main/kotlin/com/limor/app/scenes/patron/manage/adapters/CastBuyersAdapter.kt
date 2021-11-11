package com.limor.app.scenes.patron.manage.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.databinding.ItemBuyersListBinding
import com.limor.app.databinding.ItemLoadMoreBinding
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable

class CastBuyersAdapter(
    private val onLoadMore: () -> Unit
) : ListAdapter<String, RecyclerView.ViewHolder>(
    BuyersDiffCallback()
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
            CastBuyersAdapter.ITEM_TYPE_BUYER -> {
                val binding =
                    ItemBuyersListBinding.inflate(inflater, viewGroup, false)
                BuyerViewHolder(binding)
            }
            else -> {
                val binding = ItemLoadMoreBinding.inflate(inflater, viewGroup, false)
                ViewHolderLoadMore(binding, onLoadMore = onLoadMore)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return getViewHolderByViewType(parent, viewType)
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (getItemViewType(position) == CastBuyersAdapter.ITEM_TYPE_LOAD_MORE) {
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
            return CastBuyersAdapter.ITEM_TYPE_LOAD_MORE
        }
        return CastBuyersAdapter.ITEM_TYPE_BUYER
    }

    override fun getItemCount(): Int {
        val superCount = super.getItemCount()
        if (superCount == 0) {
            return 0
        }

        return superCount + if (loadMore) 1 else 0
    }

    fun refreshItems(list: List<String>) {
        notifyDataSetChanged()
    }

    fun updateItem(item: String, position: Int) {
        notifyDataSetChanged()
    }

    companion object {
        private const val ITEM_TYPE_BUYER = 1
        private const val ITEM_TYPE_LOAD_MORE = 2
    }

}

class BuyerViewHolder(
    val binding: ItemBuyersListBinding
): ViewHolderBindable<String>(binding){
    override fun bind(name: String) {
        binding.castName.text = name
    }
}

class BuyersDiffCallback : DiffUtil.ItemCallback<String>() {
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

class ViewHolderLoadMore(
    val binding: ItemLoadMoreBinding,
    private val onLoadMore: () -> Unit
) : ViewHolderBindable<String>(binding) {

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

    override fun bind(item: String) {
        updateStyle()
    }

    private fun updateStyle() {
        binding.loadMore.alpha = if (isEnabled) 1.0f else 0.3f
    }
}