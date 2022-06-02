package com.limor.app.util

import com.android.billingclient.api.ProductDetails

fun Iterable<ProductDetails.SubscriptionOfferDetails>?.base(): ProductDetails.SubscriptionOfferDetails? {
    if (this == null) {
        return null
    }

    for (detail in this) {
        if (detail.offerTags.size == 1 && detail.offerTags.contains("base")) {
            return detail
        }
    }

    return null
}


fun Iterable<ProductDetails.SubscriptionOfferDetails>?.discount(): ProductDetails.SubscriptionOfferDetails? {
    if (this == null) {
        return null
    }

    for (detail in this) {
        if (detail.offerTags.contains("discount")) {
            return detail
        }
    }

    return null
}

fun Iterable<ProductDetails.SubscriptionOfferDetails>?.basePrice(): ProductDetails.PricingPhase? {
    if (this == null) {
        return null
    }

    for (detail in this) {
        if (detail.offerTags.size == 1 && detail.offerTags.contains("base")) {
            return detail.pricingPhases.pricingPhaseList.first()
        }
    }

    return null
}


fun Iterable<ProductDetails.SubscriptionOfferDetails>?.discountPrice(): ProductDetails.PricingPhase? {
    if (this == null) {
        return null
    }

    for (detail in this) {
        if (detail.offerTags.contains("discount")) {
            return detail.pricingPhases.pricingPhaseList.first()
        }
    }

    return null
}
