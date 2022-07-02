package com.limor.app.scenes.patron

import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.databinding.ItemPatronPlanBinding
import com.limor.app.util.basePrice
import com.limor.app.util.discountPrice
import org.jetbrains.anko.imageResource
import timber.log.Timber

class PatronPlanViewHolder(
    val binding: ItemPatronPlanBinding, val listener: PricingPlansAdapter.OnPlanClickListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun getTitle(productDetails: ExtraProductDetails): SpannableString {
        // Sample SKU IDs:
        // com.limor.prod.monthly_plan
        // com.limor.prod.annual_plan

        if (productDetails.defaultProductDetails.productId.contains("monthly")) {
            val spannableString = SpannableString(binding.root.context.getString(R.string.sub_tier_monthly))
            spannableString.setSpan(
                AbsoluteSizeSpan(18, true),
                0,
                6,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                AbsoluteSizeSpan(16, true),
                6,
                spannableString.length - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableString
        } else if (productDetails.defaultProductDetails.productId.contains("annual")) {
            val spannableString = if(productDetails.discountedProductDetails != null) SpannableString(binding.root.context.getString(R.string.sub_tier_yearly_discounted)) else SpannableString(binding.root.context.getString(R.string.sub_tier_yearly))
            spannableString.setSpan(
                AbsoluteSizeSpan(18, true),
                0,
                10,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                AbsoluteSizeSpan(16, true),
                10,
                spannableString.length - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableString
        } else {
            return SpannableString(productDetails.defaultProductDetails.title)
        }
    }

    fun getStrikedDescription(productDetails: ExtraProductDetails): String{
        // Sample SKU IDs:
        // com.limor.prod.monthly_plan
        // com.limor.prod.annual_plan

        if (productDetails.defaultProductDetails.productId.contains("monthly")) {
            return binding.root.context.getString(R.string.monthly_subs_striked_description, productDetails.defaultProductDetails.subscriptionOfferDetails.basePrice()?.formattedPrice)
        } else if (productDetails.defaultProductDetails.productId.contains("annual")) {
            return binding.root.context.getString(R.string.yearly_subs_striked_description, productDetails.defaultProductDetails.subscriptionOfferDetails.basePrice()?.formattedPrice)
        } else {
            return productDetails.defaultProductDetails.title
        }
    }

    fun getMonthlyPriceFromYearly(priceMicros: Long): String {
        val microsPerMonth = priceMicros / 12
        val perMonth = microsPerMonth / 1_000_000f
        return "%.2f".format(perMonth)
    }

    fun bind(productDetails: ExtraProductDetails, position: Int, selectedSku: String?) {

        val shouldSelect =
            selectedSku != null && (selectedSku == productDetails.defaultProductDetails.productId || selectedSku == productDetails.discountedProductDetails?.productId)

        setPlanState(shouldSelect)
        binding.apply {
            price.text = getTitle(productDetails)
            strikedDescription.text = getStrikedDescription(productDetails)
            strikedDescription.paintFlags = strikedDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            description.text = when {
                productDetails.defaultProductDetails.productId.contains("monthly") -> {
                    "Introductory Monthly Offer Price: " + productDetails.defaultProductDetails.subscriptionOfferDetails.discountPrice()?.formattedPrice
                }
                productDetails.defaultProductDetails.productId.contains("annual") -> {
                    if(productDetails.discountedProductDetails == null){
                        "Introductory Annual Offer Price: " + productDetails.defaultProductDetails.subscriptionOfferDetails.discountPrice()?.formattedPrice + System.getProperty("line.separator") + "12 Months at " +
                                productDetails.defaultProductDetails.subscriptionOfferDetails.basePrice()?.priceCurrencyCode + " " +
                                getMonthlyPriceFromYearly(productDetails.defaultProductDetails.subscriptionOfferDetails.discountPrice()?.priceAmountMicros ?: 0) + " / month"
                    } else{
                        "Introductory Annual Offer Price: " + productDetails.discountedProductDetails.subscriptionOfferDetails.discountPrice()?.formattedPrice + System.getProperty("line.separator") + "12 Months at " +
                                productDetails.defaultProductDetails.subscriptionOfferDetails.basePrice()?.priceCurrencyCode + " " +
                                getMonthlyPriceFromYearly(productDetails.discountedProductDetails.subscriptionOfferDetails.discountPrice()?.priceAmountMicros ?: 0) + " / month"
                    }
                }
                else -> {
                    productDetails.defaultProductDetails.subscriptionOfferDetails.basePrice()?.formattedPrice
                }
            }

            if (BuildConfig.DEBUG) {
                Timber.d("SUBS TRIAL PERIOD ${productDetails.defaultProductDetails}")
            }

            root.setOnClickListener {
                listener.onUserClicked(productDetails, position)
            }
        }
    }

    private fun setPlanState(select: Boolean) {
        binding.apply {
            if (select) {
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