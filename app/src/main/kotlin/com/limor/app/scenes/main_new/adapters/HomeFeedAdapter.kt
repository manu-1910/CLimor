package com.limor.app.scenes.main_new.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.android.billingclient.api.SkuDetails
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.dm.ShareResult
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderPodcast
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderRecast
import com.limor.app.service.ProductDetails
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel

class HomeFeedAdapter(
    private val onLikeClick: (castId: Int, like: Boolean) -> Unit,
    private val onCastClick: (cast: CastUIModel, sku: SkuDetails?) -> Unit,
    private val onReCastClick: (castId: Int, isRecasted: Boolean) -> Unit,
    private val onReloadData: (castId: Int, reload: Boolean) -> Unit,
    private val onCommentsClick: (CastUIModel, sku: SkuDetails?) -> Unit,
    private val onShareClick: (CastUIModel, onShared: ((shareResult: ShareResult) -> Unit)?) -> Unit,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit,
    private val onUserMentionClick: (username: String, userId: Int) -> Unit,
    private val onEditPreviewClick: (cast: CastUIModel) -> Unit,
    private val onPlayPreviewClick: (cast: CastUIModel, play: Boolean) -> Unit,
    private val onEditPriceClick: (cast: CastUIModel) -> Unit,
    private val onPurchaseCast: (cast: CastUIModel, sku: SkuDetails?) -> Unit,
    private val productDetailsFetcher: ProductDetails
) : PagingDataAdapter<CastUIModel, ViewHolderBindable<CastUIModel>>(HomeFeedDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        val recasted = getItem(position)?.recasted == true
        return if (recasted) ITEM_TYPE_RECASTED else ITEM_TYPE_PODCAST
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<CastUIModel> {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<CastUIModel> {
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
        } else {
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
        }
    }

    override fun onBindViewHolder(holder: ViewHolderBindable<CastUIModel>, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    companion object {
        private const val ITEM_TYPE_PODCAST = 1
        private const val ITEM_TYPE_RECASTED = 2
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