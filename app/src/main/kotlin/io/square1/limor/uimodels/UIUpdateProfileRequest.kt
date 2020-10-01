package io.square1.limor.uimodels

data class UIUpdateProfileRequest(
    var user: UIUpdateUser
)

data class UIUpdateUser(
    var first_name: String?,
    var last_name: String?,
    var username: String?,
    var website: String?,
    var description: String?,
    var email: String?,
    var phone_number: String?,
    var date_of_birth: Int?,
    var gender: String?,
    var image_url: String?
)
