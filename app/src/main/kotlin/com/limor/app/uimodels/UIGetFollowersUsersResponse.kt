package com.limor.app.uimodels


data class UIGetFollowersUsersResponse(
    var code: Int,
    var message: String,
    var data: UIFollowersUsersDataArray = UIFollowersUsersDataArray()
)

data class UIFollowersUsersDataArray(
    var following_users: ArrayList<UIUser> = ArrayList()
)
