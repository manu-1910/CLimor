package com.limor.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.SendData
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.usecases.AddCommentUseCase
import com.limor.app.util.SoundType
import com.limor.app.util.Sounds
import dagger.android.AndroidInjection
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


data class VoiceUploadProgress(var progress: Int, var id: String)
data class VoiceUploadCompletion(var success: Boolean)

class VoiceCommentUploadService: Service() {

    private var coroutineJob: Job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + coroutineJob)

    @Inject
    lateinit var addCommentUseCase: AddCommentUseCase;

    private var started = false

    companion object {

        fun upload(context: Context, bundle: Bundle) {
            context.startService(
                Intent(
                    context.applicationContext,
                    VoiceCommentUploadService::class.java
                ).apply {
                    action = ACTION_START_UPLOAD_SERVICE
                    putExtras(bundle)
                })
        }

        const val ACTION_START_UPLOAD_SERVICE = "LIMOR_ACTION_START_UPLOAD_SERVICE"
        const val ACTION_STOP_UPLOAD_SERVICE = "LIMOR_ACTION_STOP_UPLOAD_SERVICE"

        private const val UPLOAD_CHANNEL_ID = "com.limor.app.voice_upload_channel"
        private const val UPLOAD_NOTIFICATION_ID = 112

        fun fromData(inputStatus: SendData, podcastId: Int, ownerId: Int, ownerType: String): Bundle =
            bundleOf(
                KEY_PODCAST_ID to podcastId,
                KEY_CONTENT to inputStatus.text,
                KEY_OWNER_ID to ownerId,
                KEY_OWNER_TYPE to ownerType,
                KEY_DURATION to inputStatus.duration,
                KEY_LOCAL_AUDIO_FILE to inputStatus.filePath
            )

        const val KEY_PODCAST_ID = "KEY_PODCAST_ID"
        const val KEY_CONTENT = "KEY_CONTENT"
        const val KEY_OWNER_ID = "KEY_OWNER_ID"
        const val KEY_OWNER_TYPE = "KEY_OWNER_TYPE"
        const val KEY_DURATION = "KEY_DURATION"
        const val KEY_LOCAL_AUDIO_FILE = "KEY_LOCAL_AUDIO_FILE"
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_UPLOAD_SERVICE -> {
                if (!started) {
                    startInForeground()
                }
                playSoundAndUpload(intent)
            }
            ACTION_STOP_UPLOAD_SERVICE -> stopForegroundService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun playSoundAndUpload(intent: Intent) {
        Sounds.playSound(this, SoundType.COMMENT) {
            uploadVoiceCommentAudio(intent)
        }
    }

    private fun uploadVoiceCommentAudio(intent: Intent) {
        scope.launch {
            var newCommentID: Int? = null

            uploadAudio(intent)?.let {
                newCommentID = addComment(it, intent)
            }

            stopForegroundService()

            // For now a very simple boolean result
            EventBus.getDefault().post(VoiceUploadCompletion(
                newCommentID != null
            ))
        }
    }

    private suspend fun uploadAudio(inputData: Intent): String? = suspendCoroutine { cont ->
        val audioFilePath = inputData.getStringExtra(KEY_LOCAL_AUDIO_FILE)
        if (audioFilePath == null) {
            cont.resume(null)
            return@suspendCoroutine
        }

        Commons.getInstance().uploadAudio(
            applicationContext,
            File(audioFilePath),
            Constants.AUDIO_TYPE_COMMENT,
            object : Commons.AudioUploadCallback {
                override fun onSuccess(audioUrl: String?) {
                    if (BuildConfig.DEBUG) {
                        println("Audio upload to AWS to $audioUrl")
                    }
                    cont.resume(audioUrl)
                }

                override fun onProgressChanged(
                    id: Int,
                    bytesCurrent: Long,
                    bytesTotal: Long
                ) {
                    val progress = ((bytesCurrent.toDouble() / bytesTotal.toDouble()) * 100).toInt()
                    EventBus.getDefault().post(VoiceUploadProgress(
                        progress = progress,
                        id = audioFilePath
                    ))
                }

                override fun onError(error: String?) {
                    EventBus.getDefault().post(VoiceUploadCompletion(false))
                }
            })
    }

    private suspend fun addComment(remoteAudioFileURL: String, inputData: Intent): Int? {
        val podcastId = inputData.getIntExtra(KEY_PODCAST_ID, 0)
        val content = inputData.getStringExtra(KEY_CONTENT) ?: ""
        val ownerId = inputData.getIntExtra(KEY_OWNER_ID, 0)
        val ownerType = inputData.getStringExtra(KEY_OWNER_TYPE) ?: CommentUIModel.OWNER_TYPE_COMMENT
        val duration = inputData.getIntExtra(KEY_DURATION, 0)

        val result = addCommentUseCase.execute(
            podcastId,
            content,
            ownerId,
            ownerType,
            remoteAudioFileURL,
            duration
        )

        return result.getOrNull()
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException()
    }

    private fun startInForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(UPLOAD_CHANNEL_ID, "Uploading");
        }

        val pendingIntent = Intent(applicationContext, MainActivityNew::class.java).let { ni ->
            PendingIntent.getActivity(applicationContext, 0, ni, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat.Builder(this, UPLOAD_CHANNEL_ID).apply {
            setContentTitle(getString(R.string.uploading_notification_title))
            setContentText(getString(R.string.uploading_notification_text))
            setTicker(getString(R.string.uploading_notification_text))
            setContentIntent(pendingIntent)
            setSmallIcon(R.drawable.limor_circle)
            setLargeIcon(
                BitmapFactory.decodeResource(resources, R.drawable.notification)
            )
            setOngoing(true)
            priority = NotificationManager.IMPORTANCE_MAX
        }

        startForeground(UPLOAD_NOTIFICATION_ID, notification.build())

        started = true
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
        started = false
    }

    override fun onDestroy() {
        coroutineJob.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.getNotificationChannel(channelId)?.let {
            return
        }

        NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            lightColor = getColor(R.color.colorAccent)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
        }.also {
            nm.createNotificationChannel(it)
        }
    }
}