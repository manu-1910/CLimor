package com.limor.app.scenes.notifications

import android.content.Context
import android.content.Intent
import com.limor.app.R
import com.limor.app.scenes.main.fragments.podcast.PodcastDetailsActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.uimodels.UINotificationItem
import org.jetbrains.anko.toast

class NotificationManagerHelper {
    companion object {
        fun getIntent(context: Context, type: String) {
            val notificationType = getNotificationTypeByValue(type)
            if (notificationType == null) {
                context.toast(context.getString(R.string.not_valid_notification_error)).show()
            } else {
                when (notificationType) {
                    NotificationType.NOTIFICATION_TYPE_GENERAL -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_FOLLOW -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_MENTION -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_PODCAST_BOOKMARK_SHARE -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_PODCAST_LIKE -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_PODCAST_RECAST -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_PODCAST_COMMENT -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_COMMENT_LIKE -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_AD_COMMENT -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_CONVERSATION_REQUEST -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_CONVERSATION_PARTICIPANT -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_MESSAGE_SENT -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_COMMENT_COMMENT -> {

                    }
                    NotificationType.NOTIFICATION_TYPE_FACEBOOK_FRIEND -> {

                    }
                }
            }
        }

        fun handleClickedNotification(context: Context, item: UINotificationItem) {
            val notificationType = getNotificationTypeByValue(item.notificationType)
            if (notificationType == null) {
                context.toast(context.getString(R.string.not_valid_notification_error)).show()
            } else {
                when (notificationType) {
                    NotificationType.NOTIFICATION_TYPE_GENERAL -> {
                        context.toast("Not implemented yet").show()
                    }
                    NotificationType.NOTIFICATION_TYPE_FOLLOW -> {
                        handleUserNotification(context, item)
                    }
                    NotificationType.NOTIFICATION_TYPE_MENTION -> {
                        context.toast("Not implemented yet").show()
                    }
                    NotificationType.NOTIFICATION_TYPE_PODCAST_BOOKMARK_SHARE -> {
                        context.toast("Not implemented yet").show()
                    }
                    NotificationType.NOTIFICATION_TYPE_PODCAST_LIKE -> {
                        handlePodcastNotification(context, item)
                    }
                    NotificationType.NOTIFICATION_TYPE_PODCAST_RECAST -> {
                        handlePodcastNotification(context, item)
                    }
                    NotificationType.NOTIFICATION_TYPE_PODCAST_COMMENT -> {
                        handlePodcastNotification(context, item)
                    }
                    NotificationType.NOTIFICATION_TYPE_COMMENT_LIKE -> {
                        handlePodcastNotification(context, item)
                    }
                    NotificationType.NOTIFICATION_TYPE_AD_COMMENT -> {
                        context.toast("Not implemented yet").show()
                    }
                    NotificationType.NOTIFICATION_TYPE_CONVERSATION_REQUEST -> {
                        context.toast("Not implemented yet").show()
                    }
                    NotificationType.NOTIFICATION_TYPE_CONVERSATION_PARTICIPANT -> {
                        context.toast("Not implemented yet").show()
                    }
                    NotificationType.NOTIFICATION_TYPE_MESSAGE_SENT -> {
                        context.toast("Not implemented yet").show()
                    }
                    NotificationType.NOTIFICATION_TYPE_COMMENT_COMMENT -> {
                        handlePodcastNotification(context, item)
                    }
                    NotificationType.NOTIFICATION_TYPE_FACEBOOK_FRIEND -> {
                        context.toast("Not implemented yet").show()
                    }
                }
            }
        }

        private fun getProfileIntent() {

        }

        private fun getPodcastIntent() {

        }

        private fun handleUserNotification(context: Context, item: UINotificationItem) {
            val currentUser = item.resources.owner
            currentUser.let {
                val userProfileIntent =
                    Intent(context, UserProfileActivity::class.java)
                userProfileIntent.putExtra("user", it)
                context.startActivity(userProfileIntent)
            }
        }

        private fun handlePodcastNotification(context: Context, item: UINotificationItem) {
            val currentPodcast = item.resources.podcast
            currentPodcast.let {
                val podcastDetailsIntent =
                    Intent(context, PodcastDetailsActivity::class.java)
                podcastDetailsIntent.putExtra("podcast", currentPodcast)
                context.startActivity(podcastDetailsIntent)
            }
        }
    }


}

