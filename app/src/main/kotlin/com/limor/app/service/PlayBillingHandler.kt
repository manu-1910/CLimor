package com.limor.app.service

import android.app.Activity
import android.content.Context
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.limor.app.BuildConfig
import com.limor.app.apollo.PublishRepository
import com.limor.app.common.Constants
import com.limor.app.uimodels.CastUIModel
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

interface DetailsAvailableListener {
    fun onDetailsAvailable(details: Map<String, SkuDetails>)
}

interface ProductDetails {
    fun getPrice(productId: String, listener: DetailsAvailableListener)
    fun getPrices(listener: DetailsAvailableListener)
    fun getPrices(): LiveData<List<SkuDetails>>
}

data class PurchaseTarget(
    val sku: SkuDetails,
    val cast: CastUIModel
)

@Singleton
class PlayBillingHandler @Inject constructor(
    private val context: Context,
    val publishRepository: PublishRepository,
) : ProductDetails {

    private var currentTarget: PurchaseTarget? = null
    private var onPurchaseDone: ((success: Boolean) -> Unit)? = null

    private val mainThreadJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainThreadJob)

    private val billingJob = SupervisorJob()
    private val billingScope = CoroutineScope(Dispatchers.IO + billingJob)

    private val productSkuDetails = mutableMapOf<String, SkuDetails>()

    private var lastFetchTime = 0L

    private val detailsListeners = mutableListOf<WeakReference<DetailsAvailableListener>>()

    private val purchasesUpdatedListener by lazy {
        PurchasesUpdatedListener { billingResult, purchases ->
            // This is called once there is some update about purchase after launching the billing flow
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                // showProgressBar()
                for (purchase in purchases) {
                    // Handle Purchase
                    handlePurchase(purchase)
                }
            } else {
                onPurchaseDone?.invoke(false)
            }
        }

    }

    private fun notifySuccess(success: Boolean) {
        uiScope.launch {
            onPurchaseDone?.invoke(success)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val target = currentTarget ?: return
        billingScope.launch {

            val response = publishRepository.createCastPurchase(target.cast, purchase, target.sku)

            if (response == Constants.API_SUCCESS) {
                ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build().also {
                    consumePurchase(it)
                }

                notifySuccess(true)

            } else {
                notifySuccess(false)
            }

        }
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()
    }

    private suspend fun queryInAppSKUDetails(skuIds: List<String>): List<SkuDetails> {

        // unfortunately, Android has an undocumented limit on the size of the list in this request.
        // 20 is a number used in one of the Google samples, namely "Trivial Drive", source code of which
        // can be found here:
        // https://github.com/googlesamples/android-play-billing/blob/master/TrivialDrive/app/src/main/java/com/example/android/trivialdrivesample/util/IabHelper.java
        // more info here as well:
        // https://github.com/serso/android-checkout/issues/56

        return skuIds
            .chunked(20)
            .map {
                SkuDetailsParams.newBuilder()
                    .setSkusList(skuIds)
                    .setType(BillingClient.SkuType.INAPP)
            }
            .map {
                getSkusFromParams(it)
            }
            .flatten()
    }

    private suspend fun getSkusFromParams(params: SkuDetailsParams.Builder): List<SkuDetails> {
        val result = billingClient.querySkuDetails(params.build())
        if (BuildConfig.DEBUG) {
            Timber.d("Billing SKUs-> ${result.skuDetailsList}")
        }
        return result.skuDetailsList ?: listOf()
    }

    fun close() {
        billingClient.endConnection()
    }

    fun launchBillingFlowFor(
        target: PurchaseTarget,
        requireActivity: Activity,
        onDone: (success: Boolean) -> Unit,
    ) {
        if (currentTarget != null) {
            onDone(false)
            return
        }

        currentTarget = target
        onPurchaseDone = onDone
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(target.sku)
            .build()
        billingClient.launchBillingFlow(requireActivity, flowParams)
    }

    suspend fun consumePurchase(consumeParams: ConsumeParams) {
        billingClient.consumePurchase(consumeParams)
    }

    fun connectToBillingClient(connection: (Boolean) -> Unit) {
        if (billingClient.connectionState == BillingClient.ConnectionState.CONNECTED) {
            connection(true)
            return
        }

        if (billingClient.connectionState == BillingClient.ConnectionState.CONNECTING) {
            connection(false)
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                connectToBillingClient(connection)
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                uiScope.launch {
                    connection(true)
                }
            }

        })
    }

    private suspend fun fetchProducts(): List<SkuDetails> {
        // TODO use a constant for the type
        val productIds = publishRepository.getInAppPricesTiers("castPriceTiers")
        if (BuildConfig.DEBUG) {
            Timber.d("Product IDs from backend: $productIds")
        }
        val details = queryInAppSKUDetails(productIds)
        if (BuildConfig.DEBUG) {
            Timber.d("Got SKU Details -> $details")
        }

        return details
    }

    private fun onDetails(details: List<SkuDetails>, notifyListeners: Boolean = true) {
        lastFetchTime = System.currentTimeMillis()
        productSkuDetails.apply {
            clear()
            putAll(details.map { it.sku to it })
        }
        if (notifyListeners) {
            notifyListeners()
        }

    }

    /**
     * This connects to the billing client if not connected and fetches the product IDs.
     */
    private fun fetchProductIDs(onDone: ((details: List<SkuDetails>) -> Unit)? = null) {
        connectToBillingClient { connected ->
            if (connected) {
                billingScope.launch {
                    val details = fetchProducts()
                    uiScope.launch {
                        onDetails(details, onDone == null)
                        onDone?.invoke(details)
                    }
                }
            }
        }
    }

    private fun addListener(detailsAvailableListener: DetailsAvailableListener) {
        for (listener in detailsListeners) {
            // Listener already in the list of listeners
            if (listener.get() == detailsAvailableListener) {
                return
            }
        }
        detailsListeners.add(WeakReference(detailsAvailableListener))
    }

    private fun removeListener(detailsAvailableListener: DetailsAvailableListener) {
        for (listener in detailsListeners) {
            if (listener.get() == detailsAvailableListener) {
                detailsListeners.remove(listener)
                return
            }
        }
    }

    private fun notifyListeners() {
        for (listener in detailsListeners) {
            listener.get()?.onDetailsAvailable(productSkuDetails)
        }
    }

    /**
     * This ensures the PlayBillingHandler has fresh details for all product details and in
     * particular for the specified productId. We pass the productId so that the billing client
     * can fetch the prices again if a new product was added on Google Play and thus that productId
     * isn't available in the cached data.
     *
     * This should only be called on the main thread otherwise it throws an exception.
     */
    override fun getPrice(productId: String, listener: DetailsAvailableListener) {
        if (!Looper.getMainLooper().isCurrentThread) {
            throw RuntimeException("getPrice must be called on the main thread.")
        }

        addListener(listener)

        // Always refresh the product IDs if the refresh time limit is reached
        //
        val delta = System.currentTimeMillis() - lastFetchTime
        if (delta > refreshTimeLimit) {
            fetchProductIDs()
            return
        }

        // If we already have the SKU Details for this particular product we immediately notify
        // its listener.
        if (productSkuDetails.containsKey(productId)) {
            listener.onDetailsAvailable(productSkuDetails)
            return
        }

        // At this point the passed product ID does not exist in the cached list so we refresh
        // the list
        fetchProductIDs()
    }

    /**
     * Get pricing details and notify all listeners. This always makes an API call to get the latest
     * product IDs and then an SDK call to get their SKU details.
     */
    override fun getPrices(listener: DetailsAvailableListener) {
        if (!Looper.getMainLooper().isCurrentThread) {
            throw RuntimeException("getPrice must be called on the main thread.")
        }

        addListener(listener)

        fetchProductIDs()
    }

    /**
     * Util function to get the prices
     */
    override fun getPrices(): LiveData<List<SkuDetails>> {
        val liveData = MutableLiveData<List<SkuDetails>>()
        fetchProductIDs {
            liveData.postValue(it)
        }
        return liveData
    }

    companion object {
        // 1 day
        private const val refreshTimeLimit = 1 * 24 * 60 * 60 * 1000L
    }

}