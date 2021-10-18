package com.limor.app.dm

import com.limor.app.SearchFollowersQuery

data class ChatTarget(
    val limorUserId: Int,
    val limorProfileUrl: String?,
    val limorUserName: String?,
    val limorDisplayName: String?,
    val lastMessage: String = ""
) {
    companion object {
        fun fromSearch(follower: SearchFollowersQuery.SearchFollower) = ChatTarget(
            limorUserId = follower.id!!,
            limorUserName = follower.username,
            limorDisplayName = if (follower.first_name.isNullOrEmpty() && follower.last_name.isNullOrEmpty()) follower.username else "${follower.first_name} ${follower.last_name}",
            limorProfileUrl = follower.images?.medium_url ?: follower.images?.large_url ?: follower.images?.original_url ?: follower.images?.small_url
        )

        fun fromSession(sessionWithUser: ChatSessionWithUser) = ChatTarget(
            limorUserId = sessionWithUser.user.limorUserId,
            limorUserName = sessionWithUser.user.limorUserName,
            limorDisplayName = sessionWithUser.user.limorDisplayName,
            limorProfileUrl = sessionWithUser.user.limorProfileUrl,
            lastMessage = sessionWithUser.session.lastMessageContent
        )
    }

    fun getInfo() = if (lastMessage.isBlank()) "@${limorUserName}" else lastMessage
}