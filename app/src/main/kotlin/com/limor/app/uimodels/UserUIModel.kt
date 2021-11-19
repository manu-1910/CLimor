package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.*
import com.limor.app.extensions.toLocalDate
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

@Parcelize
data class UserUIModel(
    val id: Int,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val imageLinks: ImageLinksUIModel?,
    val isBlocked: Boolean?,
    val isFollowed: Boolean?,
    val isBlockedBy: Boolean?,
    val isFollowedBy: Boolean?,
    val followingCount: Int?,
    val followersCount: Int?,
    val description: String?,
    val website: String?,
    val gender: String?,
    val dateOfBirth: LocalDate? = null,
    val areNotificationsEnabled: Boolean?,
    val isActive: Boolean?,
    val isSuspended: Boolean?,
    val isVerified: Boolean?,
    val isAutoplayEnabled: Boolean?,
    val sharingUrl: String?,
    var voiceBioURL: String?,
    var durationSeconds: Double?,

    // Patron stuff
    var patronInvitationStatus: String? = null,
    var patronOnBoardingStatus: String? = null,
    var isPatron: Boolean? = false,
    val patronAudioURL: String? = null,
    val patronAudioDurationSeconds: Double?
) : Parcelable {

    fun getFullName() = if(firstName == null && lastName == null) username else String.format("%s %s", firstName, lastName)

    /**
     * This function returns the best suited image url to be used as the avatar picture everywhere
     * in the app. The function tries to deliver the medium version first and if it's not available
     * tries the following in the order listed:
     * - large
     * - original
     * - small
     */
    fun getAvatarUrl(): String? = imageLinks?.medium ?: imageLinks?.large ?: imageLinks?.original ?: imageLinks?.small

    fun hasNotificationsEnabled(): Boolean = areNotificationsEnabled ?: false
}

fun GetFeaturedCastsQuery.Owner.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun SuggestedPeopleQuery.GetSuggestedUser.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetTopCastsQuery.Owner.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun SearchUsersQuery.SearchUser.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetPodcastsByCategoryQuery.Owner.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetPodcastsByHashtagQuery.Owner.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )


fun GetUserProfileQuery.GetUser.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration,
        patronOnBoardingStatus = patronOnboardingStatus
    )


fun GetUserProfileByIdQuery.GetUserById.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetUserPodcastsQuery.Owner.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetPatronPodcastsQuery.Owner.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun FeedItemsQuery.Owner.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun FeedItemsQuery.Recaster.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetCommentsByPodcastsQuery.User.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetCommentsByPodcastsQuery.User1.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetCommentsByIdQuery.User.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetCommentsByIdQuery.User1.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )

fun GetPodcastByIdQuery.Owner.mapToUIModel(): UserUIModel =
    UserUIModel(
        id = id!!, username = username, firstName = first_name, lastName = last_name,
        imageLinks = images?.mapToUIModel(), isBlocked = blocked, isFollowed = followed,
        isBlockedBy = blocked_by, isFollowedBy = followed_by,
        followingCount = following_count, followersCount = followers_count,
        description = description, website = website, gender = gender,
        dateOfBirth = date_of_birth?.toLocalDate(),
        areNotificationsEnabled = notifications_enabled, isActive = active,
        isSuspended = suspended, isVerified = verified, isAutoplayEnabled = autoplay_enabled,
        sharingUrl = sharing_url,
        voiceBioURL = voice_bio_url,
        durationSeconds = duration,
        patronInvitationStatus = patronInvitationStatus,
        isPatron = isPatron,
        patronAudioURL = patronAudioURL,
        patronAudioDurationSeconds = patronAudioDuration
    )