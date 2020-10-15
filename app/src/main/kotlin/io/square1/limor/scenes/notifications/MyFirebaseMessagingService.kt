package io.square1.limor.scenes.notifications

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
import io.square1.limor.R
import io.square1.limor.scenes.main.MainActivity
import io.square1.limor.scenes.utils.Commons
import org.json.JSONException
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
        } catch (e: JSONException) {
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
        val pushNoteExtra = pushNote.getJSONObject("extra")
        val type = pushNoteExtra.getString("type")
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.putExtra(Commons.NOTIFICATION_TYPE, type)
        if (type == UtilsNotificationManager.NOTIFICATION_TYPE_MESSAGE_SENT) {
            resultIntent.putExtra(
                UtilsNotificationManager.NOTIFICATION_TYPE_MESSAGE_SENT,
                pushNoteExtra.getInt("conversation_id")
            )
        }
        return resultIntent
    }

}