package com.limor.app.apollo

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.apollographql.apollo.api.Input
import com.limor.app.*
import com.limor.app.type.CreatePodcastInput
import com.limor.app.uimodels.CastUIModel
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import javax.inject.Inject

class PublishRepository @Inject constructor(val apollo: Apollo) {

    suspend fun createPodcast(podcast: CreatePodcastInput): String? {
        val query = CreatePodcastMutation(podcast)
        val queryResult = apollo.mutate(query)
        val createPodcastResult: CreatePodcastMutation.CreatePodcast? =
            queryResult?.data?.createPodcast
        Timber.d("CreatePodcastMutation -> ${createPodcastResult?.status}")
        return createPodcastResult?.status
    }

    suspend fun updatePodcast(podcastId: Int, title: String, caption: String): String?{
        val mutation = UpdatePodcastMutation(podcastId, Input.fromNullable(title), Input.fromNullable(caption))
        val result = apollo.mutate(mutation)
        val updatePodcastResult: UpdatePodcastMutation.UpdatePodcast? =
            result?.data?.updatePodcast
        return updatePodcastResult?.status
    }

    suspend fun updateSubscriptionDetails(purchase: Purchase): String?{
        val mutation = CreatePatronSubscriptionMutation("and", purchase.skus[0],purchase.purchaseToken)

        return try{
            val result = apollo.mutate(mutation)
            val updatePodcastResult: CreatePatronSubscriptionMutation.CreatePatronSubscription? =
                result?.data?.createPatronSubscription
            updatePodcastResult?.status
        }catch (e: Exception){
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


    suspend fun createCastPurchase(cast: CastUIModel, purchase: Purchase, sku: SkuDetails): String {
        // TODO use constants for those 'and' and 'IN' values

        val localRawPrice = sku.priceAmountMicros / 1_000_000.0
        val localPrice = "${BigDecimal(localRawPrice).setScale(2, RoundingMode.HALF_EVEN)}"

        val mutation = CreateCastPurchaseMutation(
            platform = "and",
            token = purchase.purchaseToken,
            podcastId = cast.id,
            regionCode = "", // as per Sasank we use an empty value
            // another option is sku.price.replace(Regex("\\p{Sc}"),""),
            purchasedAtLocalPrice =  localPrice,
            purchasedInLocalCurrency = sku.priceCurrencyCode
        )

        if (BuildConfig.DEBUG) {
            println("Local price: $localPrice, sku.priceCurrencyCode -> ${sku.priceCurrencyCode}, purchase.purchaseToken -> ${ purchase.purchaseToken}")
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