package io.square1.limor.uimodels

data class UICreateDeleteFriendResponse(
    var code: Int,
    var message: String,
    var data: UIFollowed?
)

data class UIFollowed (
    var followed: Boolean
)