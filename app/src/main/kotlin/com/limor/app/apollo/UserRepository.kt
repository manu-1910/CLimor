package com.limor.app.apollo

import com.apollographql.apollo.api.Input
import com.limor.app.*
import timber.log.Timber
import javax.inject.Inject

class UserRepository @Inject constructor(val apollo: Apollo){

    suspend fun createUser(dob: String): String? {
        val query = CreateUserMutation(dob)
        val queryResult = apollo.mutate(query)
        val createUserResult: CreateUserMutation.CreateUser? =
            queryResult?.data?.createUser
        Timber.d("CreateUserMutation -> ${createUserResult?.status}")
        return createUserResult?.status
    }

    suspend fun updateUserName(userName: String): String? {
        val query = UpdateUserNameMutation(userName)
        val queryResult = apollo.mutate(query)
        val updateUserNameResult =
            queryResult?.data?.updateUser?.status
        Timber.d("UpdateUserNameMutation -> $updateUserNameResult")
        return updateUserNameResult
    }

    suspend fun updateUserOnboardingData(
        gender: Int?,
        categories: List<Int?>,
        languages: List<String?>
    ): String? {
        val inputGender = Input.fromNullable(gender)
        val inputCategories = Input.fromNullable(categories)
        val inputLanguages = Input.fromNullable(languages)
        val query = UpdateUserOnboardingDataMutation(inputGender, inputCategories, inputLanguages)
        val queryResult = apollo.mutate(query)
        val updateUserOnboardingData =
            queryResult?.data?.updateUser
        Timber.d("updateUserOnboardingDataMutation -> ${updateUserOnboardingData?.status}")
        return updateUserOnboardingData?.status
    }

    suspend fun updateFollowingUsersData(
        followingUsers: List<String>
    ): String? {

        // TODO replace with actual call of updating following users
//        val query = UpdateFollowingUsersDataMutation(followingUsers)
//        val queryResult = Apollo.mutate(query)
//        val updateUserOnboardingData =
//            queryResult?.data?.updateFollowingUsersData
//        Timber.d("updateFollowingUsersDataMutation -> ${updateUserOnboardingData?.status}")
//        return updateUserOnboardingData?.status
        return ""
    }

    suspend fun getUserOnboardingStatus(): String? {
        val query = GetUserOnboardingStatusQuery()
        val result = apollo.launchQuery(query)
        val nextStep = result?.data?.getUserOnboardingStatus?.nextStep
        Timber.d("getUserOnboardingStatus -> $nextStep")
        return nextStep
    }

    suspend fun updateUserOnboardingStatus(nextStep: String): String? {
        val query = UpdateUserOnboardingStatusMutation(Input.fromNullable(nextStep))
        val result = apollo.mutate(query)
        val status = result?.data?.updateUserOnboardingStatus?.status
        Timber.d("sendUserOnboardingStatus -> $status")
        return status
    }
}