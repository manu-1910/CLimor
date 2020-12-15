package com.limor.app.scenes.notifications


fun getNotificationTypeByValue(value: String): NotificationType? {
    return enumValues<NotificationType>().find {
        it.value == value
    }
}

enum class NotificationType(val value :String) {
    NOTIFICATION_TYPE_GENERAL("general"),
    NOTIFICATION_TYPE_FOLLOW("follow"),
    NOTIFICATION_TYPE_MENTION("mention"),
    NOTIFICATION_TYPE_PODCAST_BOOKMARK_SHARE("podcast_bookmark_share"),
    NOTIFICATION_TYPE_PODCAST_LIKE("podcast_like"),
    NOTIFICATION_TYPE_PODCAST_RECAST("podcast_recast"),
    NOTIFICATION_TYPE_PODCAST_COMMENT("podcast_comment"),
    NOTIFICATION_TYPE_COMMENT_LIKE("comment_like"),
    NOTIFICATION_TYPE_AD_COMMENT("ad_comment"),
    NOTIFICATION_TYPE_CONVERSATION_REQUEST("conversation_request"),
    NOTIFICATION_TYPE_CONVERSATION_PARTICIPANT("conversation_participant"),
    NOTIFICATION_TYPE_MESSAGE_SENT("message_sent"),
    NOTIFICATION_TYPE_COMMENT_COMMENT("comment_comment"),
    NOTIFICATION_TYPE_FACEBOOK_FRIEND("facebook_friend") //Not needed now
}



