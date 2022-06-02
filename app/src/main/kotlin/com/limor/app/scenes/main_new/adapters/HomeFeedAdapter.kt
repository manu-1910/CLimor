package com.limor.app.scenes.main_new.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.SkuDetails
import com.limor.app.databinding.ItemFeedRecommendedCastsBinding
import com.limor.app.databinding.ItemFeedSuggestedPeopleBinding
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.dm.ShareResult
import com.limor.app.scenes.main_new.adapters.vh.*
import com.limor.app.scenes.main_new.fragments.DataItem
import com.limor.app.uimodels.*
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderPodcast
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderRecast
import com.limor.app.service.ProductDetailsFetcher
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel

class HomeFeedAdapter(
    private val onLikeClick: (castId: Int, like: Boolean) -> Unit,
    private val onCastClick: (cast: CastUIModel, sku: ProductDetails?) -> Unit,
    private val onReCastClick: (castId: Int, isRecasted: Boolean) -> Unit,
    private val onReloadData: (castId: Int, reload: Boolean) -> Unit,
    private val onCommentsClick: (CastUIModel, sku: ProductDetails?) -> Unit,
    private val onShareClick: (CastUIModel, onShared: ((shareResult: ShareResult) -> Unit)?) -> Unit,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit,
    private val onUserMentionClick: (username: String, userId: Int) -> Unit,
    private val onEditPreviewClick: (cast: CastUIModel) -> Unit,
    private val onPlayPreviewClick: (cast: CastUIModel, play: Boolean) -> Unit,
    private val onEditPriceClick: (cast: CastUIModel) -> Unit,
    private val onPurchaseCast: (cast: CastUIModel, sku: ProductDetails?) -> Unit,
    private val productDetailsFetcher: ProductDetailsFetcher
) : PagingDataAdapter<DataItem, ViewHolderBindable<DataItem>>(HomeFeedDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CastUIModel -> {
                val recasted = (getItem(position) as CastUIModel).recasted == true
                return if (recasted) ITEM_TYPE_RECASTED else ITEM_TYPE_PODCAST
            }
            is FeedSuggestedPeople -> ITEM_TYPE_SUGGESTED_PEOPLE
            is FeedRecommendedCasts -> ITEM_TYPE_FEATURED_CASTS
            else -> ITEM_TYPE_PODCAST
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<DataItem> {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<DataItem> {
        val inflater = LayoutInflater.from(viewGroup.context)
        if (viewType == ITEM_TYPE_RECASTED) {
            val binding =
                ItemHomeFeedRecastedBinding.inflate(inflater, viewGroup, false)
            return ViewHolderRecast(
                binding,
                onLikeClick,
                onCastClick,
                onReCastClick,
                onCommentsClick,
                onShareClick,
                onHashTagClick,
                onUserMentionClick,
                onEditPreviewClick,
                onPlayPreviewClick,
                onEditPriceClick,
                onPurchaseCast,
                productDetailsFetcher
            )
        } else if (viewType == ITEM_TYPE_PODCAST) {
            val binding = ItemHomeFeedBinding.inflate(inflater, viewGroup, false)
            return ViewHolderPodcast(
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
                onPlayPreviewClick,
                onEditPriceClick,
                onPurchaseCast,
                productDetailsFetcher
            )
        } else if (viewType == ITEM_TYPE_SUGGESTED_PEOPLE) {
            val binding = ItemFeedSuggestedPeopleBinding.inflate(inflater, viewGroup, false)
            return ViewHolderSuggestedPeople(binding)
        } else {
            val binding = ItemFeedRecommendedCastsBinding.inflate(inflater, viewGroup, false)
            return ViewHolderRecommendedCasts(binding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolderBindable<DataItem>, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    companion object {
        private const val ITEM_TYPE_PODCAST = 1
        private const val ITEM_TYPE_RECASTED = 2
        private const val ITEM_TYPE_SUGGESTED_PEOPLE = 3
        private const val ITEM_TYPE_FEATURED_CASTS = 4
    }
}

class HomeFeedDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(
        oldItem: DataItem,
        newItem: DataItem
    ): Boolean {
        return if (oldItem is CastUIModel && newItem is CastUIModel) {
            oldItem.id == newItem.id
        } else if (oldItem is FeedSuggestedPeople && newItem is FeedSuggestedPeople) {
            oldItem.id == newItem.id
        } else if (oldItem is FeedRecommendedCasts && newItem is FeedRecommendedCasts) {
            oldItem.id == newItem.id
        } else {
            false
        }
    }

    override fun areContentsTheSame(
        oldItem: DataItem,
        newItem: DataItem
    ): Boolean {
        return if (oldItem is CastUIModel && newItem is CastUIModel) {
            oldItem == newItem
        } else if (oldItem is FeedSuggestedPeople && newItem is FeedSuggestedPeople) {
            oldItem.isEqualTo(newItem)
        } else if (oldItem is FeedRecommendedCasts && newItem is FeedRecommendedCasts) {
            oldItem.isEqualTo(newItem)
        } else {
            false
        }
    }
}
