package com.limor.app.dm

data class LeanUser(
    val limorUserId: Int,
    val userName: String?,
    val displayName: String?,
    val profileUrl: String?,
    var selected: Boolean = false
) {
    companion object {
        fun fromSession(sessionWithUser: ChatSessionWithUser) = LeanUser(
            limorUserId = sessionWithUser.user.limorUserId,
            userName = sessionWithUser.user.limorUserName,
            displayName = sessionWithUser.user.limorDisplayName,
            profileUrl = sessionWithUser.user.limorProfileUrl
        )
    }
}