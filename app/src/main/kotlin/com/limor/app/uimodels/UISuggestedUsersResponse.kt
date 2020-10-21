package com.limor.app.uimodels

data class UISuggestedUsersResponse(
    val code: Int,
    val message: String,
    val data: UIUsersArray = UIUsersArray()

)

data class UIUsersArray(
    val users: ArrayList<UIUser> = ArrayList()
)