package com.limor.app.common

import com.limor.app.BuildConfig


class Constants{


    companion object {
        val TERMS_URL = "https://www.limor.ie/terms-and-conditions-of-use"
        val PRIVACY_URL = "https://www.limor.ie/privacy-policy"
        val SUPPORT_EMAIL = "support@limor.ie"

        // Autofollow Limor
        val LIMOR_ACCOUNT_ID: Int = BuildConfig.LIMOR_ACCOUNT_ID

        // Authorization
        val CLIENT_ID: String = BuildConfig.CLIENT_ID
        val CLIENT_SECRET: String = BuildConfig.CLIENT_SECRET
        const val GRANT_TYPE_FACEBOOK = "facebook"
        const val GRANT_TYPE = "password"
        const val SCOPES = "user"

        // AWS and Image Handling
        const val AWS_IDENTITY_POOL: String = BuildConfig.AWS_S3_IDENTITY_POOL
        const val AWS_BUCKET: String = BuildConfig.AWS_S3_BUCKET
        const val AWS_IMAGE_BASE_URL = "https://$AWS_BUCKET.s3.amazonaws.com/"

        const val AWS_FOLDER_PROFILE_IMAGE = "user_image_direct_upload/"
        const val AWS_FOLDER_AUDIO_COMMENT = "podcast_comment_audio_direct_upload/"
        const val AWS_FOLDER_PODCAST_IMAGE = "podcast_image_direct_upload/"
        const val AWS_FOLDER_AUDIO_PODCAST = "podcast_audio_direct_upload/"

        const val AWS_FILE_PROFILE_IMAGE_IDENTIFIER = "user_avatar"
        const val AWS_FILE_PODCAST_IMAGE_IDENTIFIER = "podcast_image"

        const val AWS_FOLDER_MESSAGE_ATTACHMENTS = "direct_message_attachment_upload/"
        const val AWS_FILE_MESSAGE_ATTACHMENT = "message_attachment"
        const val AWS_FOLDER_TIMELINE_MEDIA_ITEM = "timeline_media_items/"
        const val AWS_FILE_TIMELINE_MEDIA = "timeline_media"
        const val AWS_FOLDER_IMAGE = "image/"
        const val AWS_FOLDER_VIDEO = "video/"
        const val AWS_FOLDER_AUDIO = "audio/"

        const val LOCAL_FOLDER = "/limorv2"
        const val LOCAL_FOLDER_CROPPED_IMAGES = "$LOCAL_FOLDER/cropped-images/"

        const val AUDIO_TYPE_PODCAST = 2
        const val AUDIO_TYPE_COMMENT = 3
        const val AUDIO_TYPE_ATTACHMENT = 6

        const val MAX_API_COMMENTS_PER_COMMENT = 2


        const val TAB_FOLLOWERS = "followers"
        const val TAB_FOLLOWINGS = "followings"
        const val TAB_KEY = "followings"

        // Errors
        private val ERROR_CODE_NO_AUTHENTICATED_USER: Int = 10
        private const val ERROR_CODE_AUTH_INVALID_CREDENTIALS = 100
        const val ERROR_CODE_FACEBOOK_USER_EXISTS = 1337
        const val ERROR_CODE_FACEBOOK_USER_DOES_NOT_EXISTS = 4
        const val ERROR_CODE_FACEBOOK_USER_EXITS_IN_LIMOR = 3
    }
}
