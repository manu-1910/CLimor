package com.limor.app.apollo

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.apollographql.apollo.api.Input
import com.limor.app.*
import com.limor.app.service.PlayBillingHandler
import com.limor.app.type.CreatePodcastInput
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.VerifyPromoCodeResult
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import javax.inject.Inject

class PublishRepository @Inject constructor(val apollo: Apollo) {

    suspend fun createPodcast(podcast: CreatePodcastInput): CreatePodcastMutation.CreatePodcast? {
        val query = CreatePodcastMutation(podcast)
        val queryResult = apollo.mutate(query)
        val createPodcastResult: CreatePodcastMutation.CreatePodcast? =
            queryResult?.data?.createPodcast
        Timber.d("CreatePodcastMutation -> ${createPodcastResult?.status}")
        return createPodcastResult
    }

    suspend fun updatePodcast(
        podcastId: Int,
        title: String,
        caption: String,
        matureContent: Boolean
    ): UpdatePodcastMutation.UpdatePodcast? {
        val mutation = UpdatePodcastMutation(
            podcastId,
            Input.fromNullable(title),
            Input.fromNullable(caption),
            Input.fromNullable(matureContent)
        )
        val result = apollo.mutate(mutation)
        return result?.data?.updatePodcast
    }

    suspend fun markPodcastAsMature(podcastId: Int): String? {
        val mutation = UpdatePodcastMutation(
            podcastId = podcastId,
            title = Input.absent(),
            caption = Input.absent(),
            matureContent = Input.fromNullable(true)
        )
        val result = apollo.mutate(mutation)
        val updatePodcastResult: UpdatePodcastMutation.UpdatePodcast? =
            result?.data?.updatePodcast
        return updatePodcastResult?.status
    }

    suspend fun updateSubscriptionDetails(purchase: Purchase, code: String?): String? {
        val product = purchase.products.first()

        if (product == null ) {
            if (BuildConfig.DEBUG) {
                println("Could not updateSubscriptionDetails, due to missing products.")
            }
            return "error, no sku"
        }

        val planId = PlayBillingHandler.getBackendProductId(product)
        val storePlanId = if (PlayBillingHandler.isNewProductId(product)) Input.fromNullable(product) else Input.absent()

        if (BuildConfig.DEBUG) {
            println("updateSubscriptionDetails plan IDs: $storePlanId -> $planId")
        }

        val mutation = CreatePatronSubscriptionMutation(
            "and",
            planId,
            purchase.purchaseToken,
            Input.fromNullable(code),
            storePlanId
        )

        if (BuildConfig.DEBUG) {
            Timber.tag("updateSubscriptionDetails").d("%s___%s", product, purchase.purchaseToken)
            println("Has ${purchase.products.size} skus.")
        }

        return try{
            val result = apollo.mutate(mutation)
            if (BuildConfig.DEBUG) {
                println("updateSubscriptionDetails Result from CreatePatronSubscriptionMutation -> $result")
            }
            val updatePodcastResult: CreatePatronSubscriptionMutation.CreatePatronSubscription? =
                result?.data?.createPatronSubscription
            updatePodcastResult?.status
        }catch (e: Exception){
            if (BuildConfig.DEBUG) {
                println("updateSubscriptionDetails Error in CreatePatronSubscriptionMutation")
                e.printStackTrace()
            }
            null
        }

    }

    suspend fun addPatronCategories(categories: ArrayList<Int>): String? {
        val mutation = AddPatronCategoriesMutation(categories)
        return try{
            val result = apollo.mutate(mutation)
            val updatePodcastResult: AddPatronCategoriesMutation.AddPatronCategories? =
                result?.data?.addPatronCategories
            updatePodcastResult?.status
        }catch (e: Exception){
            null
        }
    }

    suspend fun addPatronLanguages(languages: ArrayList<String>): String? {
        val mutation = AddPatronLanguagesMutation(languages)
        return try{
            val result = apollo.mutate(mutation)
            val updatePodcastResult: AddPatronLanguagesMutation.AddPatronLanguages? =
                result?.data?.addPatronLanguages
            updatePodcastResult?.status
        }catch (e: Exception){
            null
        }
    }

    suspend fun getPatronCategories(): List<PatronCategoriesQuery.GetPatronCategory?>{
        val query = PatronCategoriesQuery()
        return apollo.launchQuery(query)?.data?.getPatronCategories ?: emptyList()
    }

    suspend fun getInAppPricesTiers(type: String): List<String> {
        val query = GetCastTiersQuery(type)
        return apollo.launchQuery(query)?.data?.getInAppPrices?.castPriceTiers?.map { it.toString() } ?: emptyList()
    }

    suspend fun verifyPromoCode(code: String): VerifyPromoCodeResult {
        val query = VerifyDiscountCodeQuery(code)
        val res = apollo.launchQuery(query)?.data?.verifyDiscountCode
            ?: return VerifyPromoCodeResult(isDiscountCodeValid = false)
        return VerifyPromoCodeResult(isDiscountCodeValid = res.isDiscountCodeValid, res.priceId)
    }

    suspend fun getPlans(): List<GetPlansQuery.Plan?>? {
        val query = GetPlansQuery()
        val queryResult = apollo.launchQuery(query)
        val createPodcastResult: GetPlansQuery.GetPlans? =
            queryResult?.data?.getPlans
        Timber.d("GetPlans Query -> ${createPodcastResult?.status}")
        return createPodcastResult?.plans
    }

    suspend fun updatePriceForAllCasts(priceId: String): String{
        val mutation = UpdatePriceForAllCastsMutation(priceId)
        val result = apollo.mutate(mutation)
        return result?.data?.updatePriceForAllCasts?.status ?: ""
    }

    suspend fun updatePriceForCast(castId: Int, priceId: String): String {
        val mutation = UpdatePriceForSingleCastMutation(castId, priceId)
        val result = apollo.mutate(mutation)
        return result?.data?.updatePriceForSingleCast?.status ?: ""
    }


    suspend fun createCastPurchase(cast: CastUIModel, purchase: Purchase, product: ProductDetails): String {
        // TODO use constants for those 'and' and 'IN' values
        val details = product.oneTimePurchaseOfferDetails ?: return ""

        val localRawPrice = details.priceAmountMicros / 1_000_000.0
        val localPrice = "${BigDecimal(localRawPrice).setScale(2, RoundingMode.HALF_EVEN)}"

        val mutation = CreateCastPurchaseMutation(
            platform = "and",
            token = purchase.purchaseToken,
            podcastId = cast.id,
            regionCode = "", // as per Sasank we use an empty value
            // another option is sku.price.replace(Regex("\\p{Sc}"),""),
            purchasedAtLocalPrice =  localPrice,
            purchasedInLocalCurrency = details.priceCurrencyCode
        )

        if (BuildConfig.DEBUG) {
            println("Local price: $localPrice, sku.priceCurrencyCode -> ${details.priceCurrencyCode}, purchase.purchaseToken -> ${ purchase.purchaseToken}")
        }

        // TODO regionCode will be handled on BE, also last update from BE is getting error while
        //  acknowledging
        val result = apollo.mutate(mutation)
        return result?.data?.createCastPurchase?.status ?: ""
    }

    suspend fun inviteInternalUser(userId: Int): String {
        val mutation = SendInternalPatronInvitationMutation(userId)
        val result = apollo.mutate(mutation)
        return result?.data?.sendInternalPatronInvitation?.status ?: ""
    }

    suspend fun inviteExternal(numbers: List<String>): String {
        val mutation = SendExternalPatronInvitationMutation(numbers)
        val result = apollo.mutate(mutation)
        return result?.data?.sendExternalPatronInvitation?.status ?: ""
    }

}