package com.limor.app.uimodels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

@Parcelize
data class SuggestedPersonUIModel(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val imageLinks: ImageLinks,
    val isBlocked: Boolean,
    val isFollowed: Boolean,
    val isBlockedBy: Boolean,
    val isFollowedBy: Boolean,
    val followingCount: Int,
    val followersCount: Int,
    val description: String,
    val website: String,
    val gender: String,
    val dateOfBirth: LocalDate,
    val areNotificationsEnabled: Boolean,
    val isActive: Boolean,
    val isSuspended: Boolean,
    val isVerified: Boolean,
    val isAutoplayEnabled: Boolean,
    val sharingUrl: String,
): Parcelable {

    @Parcelize
    data class ImageLinks(
        val small: String,
        val medium: String,
        val large: String,
        val original: String,
    ): Parcelable

    fun getFullName() = String.format("%s %s", firstName, lastName)
}