package com.limor.app.service

import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayBillingHandler @Inject constructor(private val context: Context) {

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .build()

    suspend fun querySUBSKUDetails(skuIds: ArrayList<String>): List<SkuDetails>? {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuIds).setType(BillingClient.SkuType.SUBS)
        return getSkusFromParams(params).skuDetailsList
    }

    suspend fun queryInAppSKUDetails(skuIds: ArrayList<String>): List<SkuDetails>? {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuIds).setType(BillingClient.SkuType.INAPP)
        return getSkusFromParams(params).skuDetailsList
    }

    private suspend fun getSkusFromParams(params: SkuDetailsParams.Builder): SkuDetailsResult {
        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }
        Timber.d("Billing SKUs-> ${skuDetailsResult.skuDetailsList}")
        return skuDetailsResult
    }


}