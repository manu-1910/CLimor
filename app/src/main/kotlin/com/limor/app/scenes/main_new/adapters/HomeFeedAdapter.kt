package com.limor.app.scenes.main_new.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.databinding.ItemLoadMoreBinding
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderPodcast
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderRecast
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel

class HomeFeedAdapter(
    private val onLikeClick: (castId: Int, like: Boolean) -> Unit,
    private val onCastClick: (cast: CastUIModel) -> Unit,
    private val onReCastClick: (castId: Int, isRecasted: Boolean) -> Unit,
    private val onReloadData: (castId: Int, reload: Boolean) -> Unit,
    private val onCommentsClick: (CastUIModel) -> Unit,
    private val onShareClick: (CastUIModel) -> Unit,
    private val onLoadMore: () -> Unit,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit,
    private val onUserMentionClick: (username: String, userId: Int) -> Unit,
    private val onEditPreviewClick: (cast: CastUIModel) -> Unit,
    private val onPlayPreviewClick:(cast: CastUIModel, play: Boolean) -> Unit
) : ListAdapter<CastUIModel, RecyclerView.ViewHolder>(
    HomeFeedDiffCallback()
) {

    var loadMore = false
    var isLoading = false
        set(value) {
            field = value
            if (loadMore) {
                notifyItemChanged(itemCount)
            }
        }

    override fun getItemCount(): Int {
        val superCount = super.getItemCount()
        if (superCount == 0) {
            return 0
        }

        return superCount + if (loadMore) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMore && position > 0 && position == itemCount - 1) {
            return ITEM_TYPE_LOAD_MORE
        }
        val recasted = getItem(position).recasted == true
        return if (recasted) ITEM_TYPE_RECASTED else ITEM_TYPE_PODCAST
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        return when (viewType) {
            ITEM_TYPE_RECASTED -> {
                val binding =
                    ItemHomeFeedRecastedBinding.inflate(inflater, viewGroup, false)
                ViewHolderRecast(
                    binding,
                    onLikeClick,
                    onReCastClick,
                    onCommentsClick,
                    onShareClick,
                    onHashTagClick,
                    onUserMentionClick
                )
            }
            ITEM_TYPE_PODCAST -> {
                val binding = ItemHomeFeedBinding.inflate(inflater, viewGroup, false)
                ViewHolderPodcast(
                    binding,
                    onLikeClick,
                    onCastClick,
                    onReCastClick,
                    onCommentsClick,
                    onShareClick,
                    onReloadData,
                    onHashTagClick,
                    onUserMentionClick,
                    onEditPreviewClick,
                    onPlayPreviewClick
                )
            }
            else -> {
                val binding = ItemLoadMoreBinding.inflate(inflater, viewGroup, false)
                ViewHolderLoadMore(binding, onLoadMore = onLoadMore)
            }
        }
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (getItemViewType(position) == ITEM_TYPE_LOAD_MORE) {
            val vh = viewHolder as ViewHolderLoadMore
            vh.isEnabled = !isLoading
            return
        }
        // this is bad but this "load more" button will be replaced by automatic continuous loading
        // so we don't need to have the greatest arch here..
        (viewHolder as ViewHolderBindable<CastUIModel>).bind(getItem(position))
    }

    companion object {
        private const val ITEM_TYPE_PODCAST = 1
        private const val ITEM_TYPE_RECASTED = 2
        private const val ITEM_TYPE_LOAD_MORE = 3
    }
}

class HomeFeedDiffCallback : DiffUtil.ItemCallback<CastUIModel>() {
    override fun areItemsTheSame(
        oldItem: CastUIModel,
        newItem: CastUIModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: CastUIModel,
        newItem: CastUIModel
    ): Boolean {
        return oldItem == newItem
    }
}

class ViewHolderLoadMore(
    val binding: ItemLoadMoreBinding,
    private val onLoadMore: () -> Unit
) : ViewHolderBindable<CastUIModel>(binding) {

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

    override fun bind(item: CastUIModel) {
        updateStyle()
    }

    private fun updateStyle() {
        binding.loadMore.alpha = if (isEnabled) 1.0f else 0.3f
    }
}
