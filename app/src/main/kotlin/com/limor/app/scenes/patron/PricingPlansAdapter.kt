package com.limor.app.scenes.patron

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.limor.app.databinding.ItemPatronPlanBinding
import org.jetbrains.anko.layoutInflater

class PricingPlansAdapter(var list: List<SkuDetails>, val listener: OnPlanClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedSku: String? = null

    interface OnPlanClickListener {
        fun onUserClicked(item: SkuDetails, position: Int)
        fun onSelectedSkuChange(item: SkuDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PatronPlanViewHolder(ItemPatronPlanBinding.inflate(parent.context.layoutInflater,
            parent,
            false), listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(selectedSku==null){
            selectedSku = list[position].sku
            listener.onSelectedSkuChange(list[position])
        }
        (holder as PatronPlanViewHolder)
            .bind(list[position], position, selectedSku)


    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun refreshItems(list: List<SkuDetails>) {
        notifyDataSetChanged()
    }

    fun updateItem(item: SkuDetails, position: Int) {
        notifyDataSetChanged()
    }
}