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
import com.limor.app.util.SoundType
import com.limor.app.util.Sounds
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

interface DetailsAvailableListener {
    fun onDetailsAvailable(details: Map<String, ProductDetails>)
}

interface ProductDetailsFetcher {
    fun getPrice(productId: String, listener: DetailsAvailableListener)
    fun getPrices(listener: DetailsAvailableListener)
    fun getPrices(): LiveData<List<ProductDetails>>
    fun getSubscriptionPrice(subscriptionId: String, listener: DetailsAvailableListener)
}

data class PurchaseTarget(
    val product: ProductDetails,
    val cast: CastUIModel
)

@Singleton
class PlayBillingHandler @Inject constructor(
    private val context: Context,
    val publishRepository: PublishRepository,
) : ProductDetailsFetcher {

    private val subscriptionIds = mutableListOf<String>();

    private var currentTarget: PurchaseTarget? = null
    private var onPurchaseDone: ((success: Boolean) -> Unit)? = null

    private val mainThreadJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainThreadJob)

    private val billingJob = SupervisorJob()
    private val billingScope = CoroutineScope(Dispatchers.IO + billingJob)

    private val productSkuDetails = mutableMapOf<String, ProductDetails>()

    private var lastFetchTime = 0L

    private val detailsListeners = mutableListOf<WeakReference<DetailsAvailableListener>>()

    private var mFetching = false

    private val purchasesUpdatedListener by lazy {
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                billingScope.launch {
                    consumeOldPurchases()
                    currentTarget = null
                    this@PlayBillingHandler.notifySuccess(false)
                }

                return@PurchasesUpdatedListener
            }

            if (purchases.isNullOrEmpty() ) {
                if (BuildConfig.DEBUG) {
                    println("No purchases and code = ${billingResult.responseCode}")
                }
                currentTarget = null
                this@PlayBillingHandler.notifySuccess(false)
                return@PurchasesUpdatedListener
            }

            // This is called once there is some update about purchase after launching the billing flow
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // showProgressBar()
                for (purchase in purchases) {
                    // Handle Purchase
                    handlePurchase(purchase)
                }
            } else {
                currentTarget = null
                this@PlayBillingHandler.notifySuccess(false)
            }
        }

    }

    private fun consumeOldPurchases() {
        billingScope.launch {
            val result = billingClient.queryPurchaseHistory(BillingClient.SkuType.INAPP)
            if (BuildConfig.DEBUG) {
                println("Result from history -> $result (${result.billingResult.responseCode}), has ${result.purchaseHistoryRecordList?.size ?: 0} old purchases.")
            }
            result.purchaseHistoryRecordList?.let {
                it.forEach {
                    consumePurchase(it.purchaseToken)
                }
            }
        }
    }

    private fun notifySuccess(success: Boolean) {
        uiScope.launch {
            onPurchaseDone?.invoke(success)
            if (success) {
                Sounds.playSound(context, SoundType.PAYMENT)
            }
        }
    }

    private suspend fun consumePurchase(purchase: Purchase) {
        consumePurchase(purchase.purchaseToken)
    }

    private suspend fun consumePurchase(purchaseToken: String) {
        ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build().also {
            consumePurchase(it)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val target = currentTarget ?: return

        billingScope.launch {

            // First we verify the purchase with the backend
            val response = publishRepository.createCastPurchase(target.cast, purchase, target.product)

            // If and only if the backend says the purchase is valid then we consume it
            if (response == Constants.API_SUCCESS) {
                consumePurchase(purchase)
                notifySuccess(true)

            } else {
                notifySuccess(false)
            }

            currentTarget = null

        }
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()
    }

    private suspend fun queryInAppSKUDetails(skuIds: List<String>): List<ProductDetails> {
        if (skuIds.isEmpty()) {
            return listOf()
        }

        // unfortunately, Android has an undocumented limit on the size of the list in this request.
        // 20 is a number used in one of the Google samples, namely "Trivial Drive", source code of which
        // can be found here:
        // https://github.com/googlesamples/android-play-billing/blob/master/TrivialDrive/app/src/main/java/com/example/android/trivialdrivesample/util/IabHelper.java
        // more info here as well:
        // https://github.com/serso/android-checkout/issues/56

        return skuIds
            .chunked(20)
            .map {
                QueryProductDetailsParams.newBuilder()
                    .setProductList(skuIds.map { productId ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    })
            }
            .map {
                getSkusFromParams(it)
            }
            .flatten()
    }

    private suspend fun querySubSKUDetails(subIds: List<String>): List<ProductDetails> {
        if (subIds.isEmpty()) {
            return listOf()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(subIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            })
        return getSkusFromParams(params)
    }

    private suspend fun getSkusFromParams(params: QueryProductDetailsParams.Builder): List<ProductDetails> {
        val result = billingClient.queryProductDetails(params.build())
        if (BuildConfig.DEBUG) {
            Timber.d("getSkusFromParams result is ${result.billingResult.responseCode} (${result.billingResult.debugMessage}), SKUs-> ${result.productDetailsList}")
        }
        return result.productDetailsList ?: listOf()
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
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(target.product)
                .build()))
            .build()
        billingClient.launchBillingFlow(requireActivity, flowParams)
    }

    private suspend fun consumePurchase(consumeParams: ConsumeParams) {
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

    private suspend fun fetchProducts(): List<ProductDetails> {
        // TODO use a constant for the type
        val productIds = publishRepository.getInAppPricesTiers("castPriceTiers")
        if (BuildConfig.DEBUG) {
            Timber.d("Product IDs from backend: $productIds")
        }
        val details = queryInAppSKUDetails(productIds)
        if (BuildConfig.DEBUG) {
            Timber.d("Got SKU Details -> $details")
        }

        val subDetails = querySubSKUDetails(subscriptionIds)
        if (BuildConfig.DEBUG) {
            Timber.d("Got Sub SKU Details -> $subDetails for $subscriptionIds")
        }

        return subDetails + details
    }

    private fun onDetails(details: List<ProductDetails>, notifyListeners: Boolean = true) {
        lastFetchTime = System.currentTimeMillis()
        productSkuDetails.apply {
            clear()
            putAll(details.map { it.productId to it })
        }
        if (notifyListeners) {
            notifyListeners()
        }

    }

    /**
     * This connects to the billing client if not connected and fetches the product IDs.
     */
    private fun fetchProductIDs(onDone: ((details: List<ProductDetails>) -> Unit)? = null) {
        // This simple flag works because all product fetch requests are done on the main thread
        if (mFetching) {
            return
        }
        mFetching = true
        connectToBillingClient { connected ->
            if (connected) {
                billingScope.launch {
                    val details = fetchProducts()
                    uiScope.launch {
                        onDetails(details, onDone == null)
                        onDone?.invoke(details)
                    }
                }
            } else {
                if (BuildConfig.DEBUG) {
                    println("Could not connect to the billing client. Billing client connection state: ${billingClient.connectionState}")
                }
            }
            mFetching = false
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

    public fun removeListener(detailsAvailableListener: DetailsAvailableListener) {
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

    override fun getSubscriptionPrice(subscriptionId: String, listener: DetailsAvailableListener) {
        if (!subscriptionIds.contains(subscriptionId)) {
            subscriptionIds.add(subscriptionId)
        }
        if (BuildConfig.DEBUG) {
            println("Subscription IDs to lookup -> $subscriptionIds")
        }
        getPrice(subscriptionId, listener)
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
            if (BuildConfig.DEBUG) {
                println("Will refetch from Google Play because $delta ms passed since last fetch.")
            }
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
    override fun getPrices(): LiveData<List<ProductDetails>> {
        val liveData = MutableLiveData<List<ProductDetails>>()
        fetchProductIDs { productDetailsList ->
            liveData.postValue(productDetailsList
                .filter { it.productId.contains("tier_") }
                .sortedBy { productDetails ->
                    // Here are some sample IDs:
                    //
                    // com.limor.dev.tier_1
                    // com.limor.staging.tier_15
                    // com.limor.prod.tier_23
                    //
                    // That is, they always end in tier_[number] and the number part is what we want to
                    // sort by.

                    productDetails.productId.split("tier_").lastOrNull()?.toIntOrNull() ?: 0
                })
        }
        return liveData
    }

    companion object {
        // 1 day
        private const val refreshTimeLimit = 1 * 24 * 60 * 60 * 1000L
        const val subscriptionSuffix = "_new"
    }

}