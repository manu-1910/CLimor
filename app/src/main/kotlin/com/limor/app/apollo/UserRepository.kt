package com.limor.app.apollo

import com.apollographql.apollo.api.Input
import com.limor.app.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
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

    suspend fun updateUserDOB(dob: String): String? {
        val query = UpdateUserDOBMutation(dob)
        val queryResult = apollo.mutate(query)
        val updateUserDOBResult = queryResult?.data?.updateUser?.status
        Timber.d("updateUserDOB -> $updateUserDOBResult")
        return updateUserDOBResult
    }

    suspend fun updateUserProfile(
        userName: String,
        firstName: String,
        lastName: String,
        bio: String,
        website: String,
        imageURL: String?
    ): String? {
        var imageUrl: Input<String> = if(imageURL == null){
            Input.absent()
        }else Input.fromNullable(imageURL)
        val query = UpdateUserProfileMutation(userName,firstName,lastName,website,bio, imageUrl)
        val queryResult = apollo.mutate(query)
        val updateUserNameResult =
            queryResult?.data?.updateUser?.status
        Timber.d("UpdateUserProfileMutation -> $updateUserNameResult")
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

    suspend fun startFollowingUser(id: Int): Boolean? {
        val query = CreateFriendMutation(id)
        val result = apollo.mutate(query)
        val status = result?.data?.createFriends?.followed
        Timber.d("sendUserOnboardingStatus -> $status")
        return status
    }

    suspend fun unFollowUser(id: Int): Boolean? {
        val query = DeleteFriendMutation(id)
        val result = apollo.mutate(query)
        val status = result?.data?.deleteFriends?.followed
        Timber.d("sendUserOnboardingStatus -> $status")
        return status
    }

    suspend fun blockUser(id: Int): Boolean? {
        val query = BlockUserMutation(id)
        val result = apollo.mutate(query)
        val status = result?.data?.blockUsers?.blocked
        Timber.d("sendUserOnboardingStatus -> $status")
        return status
    }

    suspend fun unblockUser(id: Int): Boolean? {
        val query = UnBlockUserMutation(id)
        val result = apollo.mutate(query)
        val status = result?.data?.unblockUsers?.blocked
        Timber.d("sendUserOnboardingStatus -> $status")
        return status
    }

    suspend fun createUserDevice(token:String) {
            val query = CreateUserDevicesMutation(App.getDeviceId(),token)
            val result = apollo.mutate(query)
            val id = result?.data?.createUserDevices?.id
            id?.let{
                PrefsHandler.saveUserDeviceToken(App.instance,token)
            }
            Timber.d("createUserDevice -> $id")
    }

    suspend fun reportUser(id: Int, reason: String) {
        val query = CreateReportsMutation(reason,"",id)
        val result = apollo.mutate(query)
        val reported = result?.data?.createReports?.reported
        Timber.d("  -> $reported")
    }
}