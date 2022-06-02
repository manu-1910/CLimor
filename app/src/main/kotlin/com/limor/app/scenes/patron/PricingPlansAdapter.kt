package com.limor.app.scenes.patron

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.SkuDetails
import com.limor.app.databinding.ItemPatronPlanBinding
import org.jetbrains.anko.layoutInflater

data class ExtraProductDetails(val defaultProductDetails: ProductDetails, val discountedProductDetails: ProductDetails? = null) {
    val  currentProductDetails get() = discountedProductDetails ?: defaultProductDetails
}

class PricingPlansAdapter(var list: List<ExtraProductDetails>, val listener: OnPlanClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedProductId: String? = null

    interface OnPlanClickListener {
        fun onUserClicked(item: ExtraProductDetails, position: Int)
        fun onSelectedProductChange(item: ExtraProductDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PatronPlanViewHolder(ItemPatronPlanBinding.inflate(parent.context.layoutInflater,
            parent,
            false), listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val extraDetails = list[position]
        val product = extraDetails.discountedProductDetails ?: extraDetails.defaultProductDetails

        if ((product.productId.contains("annual")) && selectedProductId == null) {
            selectedProductId = product.productId
            listener.onSelectedProductChange(extraDetails)
        }
        (holder as PatronPlanViewHolder).bind(extraDetails, position, selectedProductId)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun refreshItems(newItems: List<ExtraProductDetails>) {
        list = newItems.toList()
        if (selectedProductId?.contains("annual") == true) {
            val currentSku = newItems.firstOrNull {
                it.discountedProductDetails?.productId?.contains("annual") == true
            } ?: newItems.firstOrNull {
                it.defaultProductDetails.productId.contains("annual")
            }
            val skuDetails = currentSku?.discountedProductDetails ?: currentSku?.defaultProductDetails
            if (skuDetails != null && currentSku != null) {
                selectedProductId = skuDetails.productId
                listener.onSelectedProductChange(currentSku)
            }
        }
        notifyDataSetChanged()
    }

    fun updateItem(item: SkuDetails, position: Int) {
        notifyDataSetChanged()
    }
}