package com.limor.app.apollo

import android.util.Log
import com.apollographql.apollo.api.Input
import com.limor.app.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class UserRepository @Inject constructor(val apollo: Apollo) {

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

    suspend fun updateFirstNameAndLastName(firstName: String, lastName: String): String? {
        val query = UpdateFirstNameAndLastNameMutation(firstName, lastName)
        val queryResult = apollo.mutate(query)
        val updateResult = queryResult?.data?.updateUser?.status
        return updateResult
    }

    suspend fun updateUserDOB(dob: String): String? {
        val query = UpdateUserDOBMutation(dob)
        val queryResult = apollo.mutate(query)
        val updateUserDOBResult = queryResult?.data?.updateUser?.status
        Timber.d("updateUserDOB -> $updateUserDOBResult")
        return updateUserDOBResult
    }

    suspend fun getUserByPhoneNumber(phoneNumber: String): Boolean {
        val query = GetUserByPhoneNumberQuery(phoneNumber)
        val queryResult = apollo.launchQuery(query)
        if (BuildConfig.DEBUG) {
            println("getUserByPhoneNumber -> ${queryResult?.data}")
        }
        return queryResult?.data?.getUserByPhoneNumber?.isFound ?: false
    }

    suspend fun updateUserProfile(
        userName: String,
        firstName: String,
        lastName: String,
        bio: String,
        website: String,
        imageURL: String?,
        voiceBioURL: String?,
        duration: Double?
    ): String? {
        var imageUrl: Input<String> = if (imageURL == null) {
            Input.absent()
        } else Input.fromNullable(imageURL)
        val query = UpdateUserProfileMutation(
            userName,
            firstName,
            lastName,
            website,
            bio,
            imageUrl,
            // always defined, even if null, because if 'absent' server wouldn't set to null
            Input.fromNullable(voiceBioURL),
            Input.fromNullable(duration)
        )
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

    suspend fun updateUserGender(
        gender: Int?
    ): String? {
        val inputGender = Input.fromNullable(gender)
        val query = UpdateUserGenderMutation(inputGender)
        val queryResult = apollo.mutate(query)
        val updateUserOnboardingData =
            queryResult?.data?.updateUser
        Timber.d("updateUserOnboardingDataMutation -> ${updateUserOnboardingData?.status}")
        return updateUserOnboardingData?.status
    }

    suspend fun updateLanguagesAndCategories(
        categories: List<Int?>,
        languages: List<String?>
    ): String? {
        val inputCategories = Input.fromNullable(categories)
        val inputLanguages = Input.fromNullable(languages)
        val query = UpdateUserCategoriesAndLanguagesMutation(inputCategories, inputLanguages)
        val queryResult = apollo.mutate(query)
        val updateUserOnboardingData =
            queryResult?.data?.updateUser
        Timber.d("updateUserCategoriesAndLanguagesMutation -> ${updateUserOnboardingData?.status}")
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

    suspend fun createUserDevice(token: String): Boolean {
        val query = CreateUserDevicesMutation(App.getDeviceId(), token)
        try{
            val result = apollo.mutate(query)
            val id = result?.data?.createUserDevices?.id
            id?.let {
                PrefsHandler.saveUserDeviceToken(App.instance, token)
            }
            Timber.d("createUserDevice -> $id")
        } catch (e: Exception){
            Log.d("sdvsdv",e.toString())
        }
        return true
    }

    suspend fun requestPatronInvitation(userId: Int): String? {
        val query = CreatePatronInvitationRequestMutation()
        val result = apollo.mutate(query)
        val status: String? = result?.data?.createPatronInvitationRequest?.status
        Timber.d("patronInvitationRequestStatus -> $status")
        return status
    }

    suspend fun reportUser(id: Int, reason: String) {
        val query = CreateReportsMutation(reason, "User", id)
        val result = apollo.mutate(query)
        val reported = result?.data?.createReports?.reported
        Timber.d("  -> $reported")
    }

    suspend fun reportComment(id: Int, reason: String) {
        val query = CreateReportsMutation(reason, "Comment", id)
        val result = apollo.mutate(query)
        val reported = result?.data?.createReports?.reported
        Timber.d("  -> $reported")
    }

    suspend fun updateUserNotificationStatus(enabled: Boolean) {
        val query = UpdateUserNotificationStatusMutation(Input.fromNullable(enabled))
        val result = apollo.mutate(query)
        val status = result?.data?.updateUser?.status
        Timber.d("updateUserNotificationStatus -> $status")
    }

    suspend fun getVendorOnBoardingUrl(): String?{
        val query = GetVendorOnBoardingUrlQuery()
        val result = apollo.launchQuery(query)
        val onboardingURL : String? = result?.data?.getVendorOnboardingUrl?.data?.onboardingURL
        Timber.d("getUserOnboardingStatus -> $onboardingURL")
        return onboardingURL
    }

    suspend fun deleteUserDevice(): Boolean{
        val query = DeleteUserDeviceMutation(App.getDeviceId())
        val result = apollo.mutate(query)
        Timber.d("deleteUserDevice -> ${result?.data?.deleteUserDevice?.deleted}")
        return result?.data?.deleteUserDevice?.deleted ?: false
    }

    suspend fun saveOneSignalId(playerId: String): String{
        val query = SaveOneSignalPlayerIdMutation(App.getDeviceId(), playerId)
        val result = apollo.mutate(query)
        Timber.d("saveOneSignalId-> $result?.data?.saveOneSignalPlayerId?.status")
        return result?.data?.saveOneSignalPlayerId?.status.toString() ?: "FAILURE"
    }

}