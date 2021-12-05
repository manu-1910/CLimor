package com.limor.app.service

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayBillingHandler @Inject constructor(private val context: Context) {

    lateinit var handlePurchases: (Purchase) -> Unit
    private val purchasesUpdatedListener by lazy {
        PurchasesUpdatedListener { billingResult, purchases ->
            //This is called once there is some update about purchase after launching the billing flow
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                //showProgressBar()
                for (purchase in purchases) {
                    //Handle Purchase
                    handlePurchases.invoke(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {

            } else {

            }
        }

    }
    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()
    }

    suspend fun querySUBSKUDetails(
        skuIds: ArrayList<String>,
        handlePurchase: (Purchase) -> Unit,
    ): List<SkuDetails>? {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuIds)
            .setType(BillingClient.SkuType.SUBS)
        handlePurchases = handlePurchase
        return getSkusFromParams(params).skuDetailsList
    }

    suspend fun queryInAppSKUDetails(
        skuIds: ArrayList<String>,
        handlePurchase: (Purchase) -> Unit,
    ): List<SkuDetails>? {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuIds).setType(BillingClient.SkuType.INAPP)
        handlePurchases = handlePurchase
        return getSkusFromParams(params).skuDetailsList
    }

    private suspend fun getSkusFromParams(params: SkuDetailsParams.Builder): SkuDetailsResult {
        val skuDetailsResult = withContext(Dispatchers.IO) {
            /*billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP,object : PurchasesResponseListener{
                override fun onQueryPurchasesResponse(
                    p0: BillingResult,
                    p1: MutableList<Purchase>,
                ) {
                    p1.forEach{
                        purchase ->
                        val consumeParams =
                            ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                    }
                }
            })*/
            billingClient.querySkuDetails(params.build())

        }

        Timber.d("Billing SKUs-> ${skuDetailsResult.skuDetailsList}")
        skuDetailsResult.skuDetailsList?.forEach { item ->
            PrefsHandler.saveSkuDetails(context, item.sku, item)
        }
        return skuDetailsResult
    }

    fun close() {
        billingClient.endConnection()
    }

    fun launchBillingFlowFor(sku: SkuDetails, requireActivity: Activity) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(sku)
            .build()
        billingClient.launchBillingFlow(requireActivity, flowParams)
    }

    suspend fun consumePurchase(consumeParams: ConsumeParams) {
        billingClient.consumePurchase(consumeParams)
    }

    fun connectToBillingClient(connection: (Boolean) -> Unit) {
        if (billingClient.connectionState == BillingClient.ConnectionState.DISCONNECTED) {

            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    connectToBillingClient(connection)
                }

                override fun onBillingSetupFinished(p0: BillingResult) {
                    connection(true)
                }

            })


        } else {
            connection(true)
        }
    }


}