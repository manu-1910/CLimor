package com.limor.app.apollo

import com.apollographql.apollo.api.Input
import com.limor.app.CreateUserMutation
import com.limor.app.UpdateUserNameMutation
import com.limor.app.UpdateUserOnboardingDataMutation
import timber.log.Timber

object UserRepository {

    suspend fun createUser(dob: String): String? {
        val query = CreateUserMutation(dob)
        val queryResult = Apollo.mutate(query)
        val createUserResult: CreateUserMutation.CreateUser? =
            queryResult?.data?.createUser
        Timber.d("CreateUserMutation -> ${createUserResult?.status}")
        return createUserResult?.status
    }

    suspend fun updateUserName(userName: String): String? {
        val query = UpdateUserNameMutation(userName)
        val queryResult = Apollo.mutate(query)
        val updateUserNameResult =
            queryResult?.data?.updateUserName
        Timber.d("UpdateUserNameMutation -> ${updateUserNameResult?.status}")
        return updateUserNameResult?.status
    }

    suspend fun updateUserOnboardingData(
        gender: Int?,
        categories: List<Int>,
        languages: List<String>
    ): String? {
        val inputCategories = Input.fromNullable(categories)
        val inputLanguages = Input.fromNullable(languages)
        val query = UpdateUserOnboardingDataMutation(gender!!, inputCategories, inputLanguages)
        val queryResult = Apollo.mutate(query)
        val updateUserOnboardingData: UpdateUserOnboardingDataMutation.UpdateUserOnboardingData? =
            queryResult?.data?.updateUserOnboardingData
        Timber.d("updateUserOnboardingDataMutation -> ${updateUserOnboardingData?.status}")
        return updateUserOnboardingData?.status
    }
}