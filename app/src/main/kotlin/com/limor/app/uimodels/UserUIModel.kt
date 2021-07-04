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
) : Parcelable {

    fun getFullName() = String.format("%s %s", firstName, lastName)
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
        sharingUrl = sharing_url
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
        sharingUrl = sharing_url
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
        sharingUrl = sharing_url
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
        sharingUrl = sharing_url
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
        sharingUrl = sharing_url
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
        sharingUrl = sharing_url
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
        sharingUrl = sharing_url
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
        sharingUrl = sharing_url
    )