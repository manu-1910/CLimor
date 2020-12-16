package com.limor.app.scenes.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.limor.app.R
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.fragments.podcast.PodcastDetailsActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.utils.Commons
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private val PREFS_NAME = "limorv2pref"
        private val PUSH_NEW_KEY = "pushnewtoken"
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        val notification = remoteMessage!!.notification
        val data = remoteMessage.data
        try {
            val pushNote = JSONObject(data["aps"])
            val message = pushNote.getString("alert")
            getNotificationIntent(pushNote)?.let { sendNotification(message, it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        sendRegistrationToServer(token)
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        //Initialize Shared Preferences to store device firebase token
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Save the instance ID inside shared preferences
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(PUSH_NEW_KEY, token)
        editor.apply()
        Timber.e("FIREBASE_ID: %s", token)
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    open fun sendNotification(message: String, intent: Intent) {
        val conversationId =
            intent.getIntExtra(UtilsNotificationManager.NOTIFICATION_TYPE_MESSAGE_SENT, 0)
//        if (conversationId > 0) {
//            if (ChatThreadFragment_.isMessageThreadOpen && ChatThreadFragment_.conversationId === conversationId) {
//                applicationContext.sendBroadcast(Intent(Constants.BROADCAST_UPDATE_CONVERSATION))
//                return
//            } else {
//                applicationContext.sendBroadcast(Intent(Constants.BROADCAST_UPDATE_CONVERSATION_ICON))
//            }
//        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val mNotifyBuilder: Notification.Builder
        mNotifyBuilder = Notification.Builder(this)
            .setSmallIcon(R.drawable.notification)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.mipmap.ic_launcher
                )
            )
            .setStyle(Notification.BigTextStyle().bigText(message))
            .setContentTitle(Commons.APP_TITLE)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_MAX)
        val randomNum = Random().nextInt(999 - 1 + 1) + 1
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "channel_01"
            val channel = NotificationChannel(
                channelName,
                "Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
            mNotifyBuilder.setChannelId(channelName)
        }
        notificationManager.notify(randomNum, mNotifyBuilder.build())
    }



    fun getNotificationIntent(pushNote: JSONObject): Intent? {
        val pushNotificationExtra = pushNote.getJSONObject("extra")
        val type = pushNotificationExtra.getString("type")
        val notificationType = getNotificationTypeByValue(type)
        return when(notificationType) {
            NotificationType.NOTIFICATION_TYPE_GENERAL -> {
                getDefaultIntent()
            }
            NotificationType.NOTIFICATION_TYPE_FOLLOW -> {
                getUserProfileIntent(pushNotificationExtra)
            }
            NotificationType.NOTIFICATION_TYPE_MENTION -> {
                getDefaultIntent()
            }
            NotificationType.NOTIFICATION_TYPE_PODCAST_BOOKMARK_SHARE -> {
                getDefaultIntent()
            }
            NotificationType.NOTIFICATION_TYPE_PODCAST_LIKE -> {
                getPodcastDetailsIntent(pushNotificationExtra)
            }
            NotificationType.NOTIFICATION_TYPE_PODCAST_RECAST -> {
                getPodcastDetailsIntent(pushNotificationExtra)
            }
            NotificationType.NOTIFICATION_TYPE_PODCAST_COMMENT -> {
                getPodcastDetailsIntent(pushNotificationExtra)
            }
            NotificationType.NOTIFICATION_TYPE_COMMENT_LIKE -> {
                getPodcastDetailsIntent(pushNotificationExtra)
            }
            NotificationType.NOTIFICATION_TYPE_AD_COMMENT -> {
                getDefaultIntent()
            }
            NotificationType.NOTIFICATION_TYPE_CONVERSATION_REQUEST -> {
                getDefaultIntent()
            }
            NotificationType.NOTIFICATION_TYPE_CONVERSATION_PARTICIPANT -> {
                getDefaultIntent()
            }
            NotificationType.NOTIFICATION_TYPE_MESSAGE_SENT -> {
                getDefaultIntent()
            }
            NotificationType.NOTIFICATION_TYPE_COMMENT_COMMENT -> {
                getPodcastDetailsIntent(pushNotificationExtra)
            }
            NotificationType.NOTIFICATION_TYPE_FACEBOOK_FRIEND -> {
                getDefaultIntent()
            }
            null -> {
                getDefaultIntent()
            }
        }
    }

    private fun getUserProfileIntent(pushNotificationExtra: JSONObject): Intent {
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra("user_id", pushNotificationExtra.getInt("owner_id"))
        return intent
    }

    private fun getPodcastDetailsIntent(pushNotificationExtra: JSONObject): Intent {
        val notifId = pushNotificationExtra.getInt("notification_id")
        val intent = Intent(this, PodcastDetailsActivity::class.java)
//        intent.putExtra("podcast_id", pushNotificationExtra.getInt("owner_id"))
        intent.putExtra("podcast_id", notifId)
        return intent
    }

    private fun getDefaultIntent(): Intent {
        return Intent(this, MainActivity::class.java)
    }

}