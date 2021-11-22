package com.limor.app.apollo

import com.android.billingclient.api.Purchase
import com.apollographql.apollo.api.Input
import com.limor.app.*
import com.limor.app.type.CreatePodcastInput
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject
import kotlin.Exception

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

    suspend fun getPlans(): List<GetPlansQuery.Plan?>? {
        val query = GetPlansQuery()
        val queryResult = apollo.launchQuery(query)
        val createPodcastResult: GetPlansQuery.GetPlans? =
            queryResult?.data?.getPlans
        Timber.d("GetPlans Query -> ${createPodcastResult?.status}")
        return createPodcastResult?.plans
    }

}