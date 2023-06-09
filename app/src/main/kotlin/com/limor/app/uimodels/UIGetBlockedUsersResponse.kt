package com.limor.app.uimodels

data class UIGetBlockedUsersResponse(
    var code: Int,
    var message: String,
    var data: UIBlockedUsersDataArray = UIBlockedUsersDataArray()
)

data class UIBlockedUsersDataArray(
    var blocked_users: ArrayList<UIUser> = ArrayList()
)
