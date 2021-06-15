package com.limor.app.scenes.auth_new.data

data class SuggestedUser(
    val id: Int?,
    val name: String?,
    val nickname: String?,
    val avatar: String?,
    var selected: Boolean = false
) {}

fun createMockedSuggestedUsers(): List<SuggestedUser> {
    return (1..9).toList().map {
        SuggestedUser(
            it,
            "PrettyLongName$it",
            nickname = "@prettyLongNickName$it",
            avatar = "https://img2.freepng.ru/20180623/iqh/kisspng-computer-icons-avatar-social-media-blog-font-aweso-avatar-icon-5b2e99c40ce333.6524068515297806760528.jpg"
        )
    }
}