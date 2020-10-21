package com.limor.app.uimodels


data class UITokenFBRequest(

    var client_id: String,
    var client_secret: String,
    var grant_type: String,
    var facebook_access_token: String,
    var referral_code: String,
    var user: UISignUpUser
)
