package com.limor.app.uimodels

import java.io.Serializable


data class UISignUpResponse(
    var code: Int,
    var data: UIData,
    var message: String
)


data class UIData(
    var access_token: UIAccessToken,
    var user: UIUser
)


data class UIAccessToken(
    var token: UIToken
)


data class UILinks(
    var website: ArrayList<UIWebsite>,
    var content: ArrayList<UIContent>,
    var caption: ArrayList<UICaption>
): Serializable {
    constructor() : this(ArrayList(),ArrayList(),ArrayList())
}

data class UIWebsite (
    var id: Int,
    var link: String,
    var startIndex: Int,
    var endIndex: Int
) : Serializable

data class UIContent (
    var id: Int,
    var link: String,
    var startIndex: Int,
    var endIndex: Int
) : Serializable

data class UICaption (
    var id: Int,
    var link: String,
    var startIndex: Int,
    var endIndex: Int
) : Serializable


data class UIImages(
    var large_url: String,
    var medium_url: String,
    var original_url: String,
    var small_url: String
): Serializable {
    constructor() : this("","","","")
}


data class UIUser(
    var active: Boolean,
    var autoplay_enabled: Boolean,
    var blocked: Boolean,
    var blocked_by: Boolean,
    var date_of_birth: Long?,
    var description: String?,
    var email: String?,
    var first_name: String?,
    var followed: Boolean,
    var followed_by: Boolean,
    var followers_count: Int?,
    var following_count: Int?,
    var gender: String?,
    var id: Int,
    var images: UIImages,
    var last_name: String?,
    var links: UILinks,
    var notifications_enabled: Boolean,
    var phone_number: String?,
    var sharing_url: String?,
    var suspended: Boolean,
    var unread_conversations: Boolean,
    var unread_conversations_count: Int?,
    var unread_messages: Boolean,
    var unread_messages_count: Int?,
    var username: String?,
    var verified: Boolean,
    var website: String?
):  Serializable {
    constructor() : this( false, false, false, false, 0,"", "",
        "", false, false, 0, 0, "",0, UIImages(), "", UILinks(),
    false,"","",false,false,0,
        false, 0, "", false, ""
    )
}
