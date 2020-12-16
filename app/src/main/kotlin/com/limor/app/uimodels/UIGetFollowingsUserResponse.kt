package com.limor.app.uimodels


data class UIGetFollowingsUsersResponse(
    var code: Int,
    var message: String,
    var data: UIFollowingsUsersDataArray = UIFollowingsUsersDataArray()
)

data class UIFollowingsUsersDataArray(
    var followed_users: ArrayList<UIUser> = ArrayList()
)
