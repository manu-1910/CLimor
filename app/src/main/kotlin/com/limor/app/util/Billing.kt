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

    // First try those offers which only have a 'base' tag
    for (detail in this) {
        if (detail.offerTags.size == 1 && detail.offerTags.contains("base")) {
            return detail.pricingPhases.pricingPhaseList.first()
        }
    }

    // This is a workaround for incorrectly fetched google play product details, where there are
    // no offerTags, in such cases we presume that is the base price
    for (detail in this) {
        if (detail.offerTags.isEmpty()) {
            return detail.pricingPhases.pricingPhaseList.first()
        }
    }

    // Finally try any offer containing the 'base' tag
    for (detail in this) {
        if (detail.offerTags.contains("base")) {
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
