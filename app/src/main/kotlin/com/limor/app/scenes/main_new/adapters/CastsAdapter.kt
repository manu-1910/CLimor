package com.limor.app.scenes.main_new.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.android.billingclient.api.ProductDetails
import com.limor.app.databinding.*
import com.limor.app.dm.ShareResult
import com.limor.app.scenes.main.fragments.profile.casts.CastItem
import com.limor.app.service.ProductDetailsFetcher
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel

class CastsAdapter(
    val userId: Int,
    private val onCastClick: (CastUIModel, ProductDetails?) -> Unit,
    private val onLikeClick: (CastUIModel, like: Boolean) -> Unit,
    private val onMoreDialogClick: (CastUIModel) -> Unit,
    private val onRecastClick: (CastUIModel, isRecasted: Boolean) -> Unit,
    private val onCommentsClick: (CastUIModel, ProductDetails?) -> Unit,
    private val onShareClick: (CastUIModel, onShared: ((shareResult: ShareResult) -> Unit)?) -> Unit,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit,
    private val isPurchased: Boolean = false,
    private val onPurchaseCast: (cast: CastUIModel, sku: ProductDetails?) -> Unit,
    private val onEditPreviewClick: (cast: CastUIModel) -> Unit,
    private val onPlayPreviewClick: (cast: CastUIModel, play: Boolean) -> Unit,
    private val onEditPriceClick: (cast: CastUIModel) -> Unit,
    private val productDetailsFetcher: ProductDetailsFetcher? = null
) : PagingDataAdapter<CastUIModel, CastItem>(CastDiffCallback())  {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): CastItem {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(
        viewGroup: ViewGroup,
        viewType: Int
    ): CastItem {
        val inflater = LayoutInflater.from(viewGroup.context)
        return CastItem(
            userId = userId,
            binding = ItemUserCastBinding.inflate(inflater, viewGroup, false),
            onCastClick = onCastClick,
            onLikeClick = onLikeClick,
            onRecastClick = onRecastClick,
            onCommentsClick = onCommentsClick,
            onShareClick = onShareClick,
            onHashTagClick = onHashTagClick,
            onEditPreviewClick = onEditPreviewClick,
            onPlayPreviewClick = onPlayPreviewClick,
            onEditPriceClick = onEditPriceClick,
            onPurchaseCast = onPurchaseCast,
            productDetailsFetcher = productDetailsFetcher,
            onMoreDialogClick = onMoreDialogClick,
        )
    }

    override fun onBindViewHolder(holder: CastItem, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

}

class CastDiffCallback : DiffUtil.ItemCallback<CastUIModel>() {
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
