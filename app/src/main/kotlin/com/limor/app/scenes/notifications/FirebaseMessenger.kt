package com.limor.app.scenes.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.limor.app.R
import com.limor.app.apollo.UserRepository
import com.limor.app.common.Constants
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.util.AppNavigationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.inject.Inject

class FirebaseMessenger : FirebaseMessagingService() {

    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var bigpicture: String? = null
    @Inject
    lateinit var userRepository: UserRepository

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("Device Token--> ${remoteMessage.data} --- ${remoteMessage.notification}")
        val notification = remoteMessage.notification
        val data = remoteMessage.data
        try {
            val dataObject = JSONObject(Gson().toJson(data))
            var message = ""
            var ttl = ""
            notification?.let {
                message = it.body.toString()
                ttl = it.title.toString()
            }

            getNavigationIntent(dataObject)?.let { sendNotification(ttl, message, it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun sendNotification(title: String, message: String, intent: Intent) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_NAME,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(mChannel)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis()
                .toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val mBuilder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_NAME)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.notification
                )
            )
            .setContentTitle(title)
            .setContentText(message)
            .setTicker("Limor")
            .setAutoCancel(true)
            .setSound(uri)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setChannelId(Constants.NOTIFICATION_CHANNEL_NAME)
            .setLights(Color.RED, 800, 800)
        mBuilder.setSmallIcon(R.drawable.notification)
        bigpicture?.let{
            mBuilder.setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(getBitmapfromUrl(it))
                    .setSummaryText(message)
            )
            mBuilder.setContentText(message)
        }
        mBuilder.setContentIntent(contentIntent)
        notificationManager.notify(Random().nextInt(999), mBuilder.build())
    }

    private fun getBitmapfromUrl(bigpicture: String): Bitmap? {
        return try{
            val url = URL(bigpicture)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            null
        }
    }

    private fun getNavigationIntent(dataObject: JSONObject): Intent? {
        return when (dataObject.get("type")) {
            "profile" -> AppNavigationManager.navigateToUserProfileIntent(this, dataObject)
            "cast" -> AppNavigationManager.navigateToExtendedPlayerIntent(this, dataObject)
            else -> AppNavigationManager.navigateToTestProfile(this,26)

        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if(token != PrefsHandler.getCurrentUserDeviceToken(this)){
            //Token has Changed
            scope.launch {
                userRepository.createUserDevice(token)
            }
        }
    }
}