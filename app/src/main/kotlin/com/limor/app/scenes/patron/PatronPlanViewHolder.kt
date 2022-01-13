package com.limor.app.scenes.patron

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.databinding.ItemPatronPlanBinding
import org.jetbrains.anko.imageResource
import timber.log.Timber

class PatronPlanViewHolder(
    val binding: ItemPatronPlanBinding, val listener: PricingPlansAdapter.OnPlanClickListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun getTitle(skuDetails: SkuDetails): String {
        // Sample SKU IDs:
        // com.limor.prod.monthly_plan
        // com.limor.prod.annual_plan

        if (skuDetails.sku.contains("monthly")) {
            return binding.root.context.getString(R.string.sub_tier_monthly)
        } else if (skuDetails.sku.contains("annual")) {
            return binding.root.context.getString(R.string.sub_tier_yearly)
        } else {
            return skuDetails.title
        }
    }

    fun bind(skuDetails: SkuDetails, position: Int, selectedSku: String?) {
        setPlanState(selectedSku, skuDetails.sku)
        binding.apply {
            price.text = getTitle(skuDetails)
            description.text = skuDetails.originalPrice

            if (BuildConfig.DEBUG) {
                Timber.d("SUBS TRIAL PERIOD ${skuDetails.freeTrialPeriod}")
                Timber.d("SUBS INTRO PRICE ${skuDetails.introductoryPrice}")
                Timber.d("SUBS SUB PERIOD ${skuDetails.subscriptionPeriod}")
            }

            root.setOnClickListener {
                listener.onUserClicked(skuDetails, position)
            }
        }
    }

    private fun setPlanState(selectedSku: String?, sku: String) {
        binding.apply {
            if (selectedSku == sku) {
                root.background =
                    ContextCompat.getDrawable(root.context, R.drawable.bg_round_plan_selected)
                imageView10.imageResource = R.drawable.plan_selected_tick
            } else {
                root.background =
                    ContextCompat.getDrawable(root.context, R.drawable.bg_round_plan_normal)
                imageView10.imageResource = R.drawable.bg_circle_grey
            }
        }

    }
}