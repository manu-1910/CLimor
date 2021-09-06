package com.limor.app.scenes.utils

import android.app.Activity
import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import com.limor.app.R
import com.limor.app.databinding.ItemVoiceBioBinding
import com.limor.app.extensions.px
import com.limor.app.extensions.viewScope
import com.limor.app.scenes.utils.voicebio.VoiceBioContract
import com.limor.app.scenes.utils.voicebio.VoiceBioPresenter
import com.limor.app.util.hasRecordPermissions
import com.limor.app.util.requestRecordPermissions
import java.io.File
import java.util.*

import kotlinx.coroutines.CoroutineScope

sealed class VoiceBioEvent {
    data class NewVoiceBio(val path: String?, val durationSeconds: Double?): VoiceBioEvent()
}

class VoiceBio @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), VoiceBioContract.ViewModel {

    var voiceBioAudioURL: String? = null
        set(value) {
            field = value
            presenter.setAudioURL(value)
        }

    var voiceBioFilePath: String? = null

    private val binding: ItemVoiceBioBinding
    private val presenter = VoiceBioPresenter(this)
    private var eventListener: ((VoiceBioEvent) -> Unit)? = null

    init {
        val view = inflate(context, R.layout.item_voice_bio, null)

        binding = ItemVoiceBioBinding.bind(view)
        binding.presenter = presenter

        addView(view)
        initViews()
    }

    fun onVoiceBioEvents(listener: (VoiceBioEvent) -> Unit) {
        this.eventListener = listener
    }

    private fun initViews() {
        binding.layoutPlayer.clipToOutline = true
        binding.layoutPlayer.clipChildren = true
        binding.layoutPlayer.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setRoundRect(0, 0, view!!.width, view.height, 24.px.toFloat())
            }
        }
    }

    override fun getNextAudioFilePath(): String {
        val fileName = "${Date().time}.m4a"
        val file = File(context.filesDir, fileName)
        return file.absolutePath
    }

    override fun getAudioURL() = voiceBioAudioURL

    override fun setAudioInfo(path: String?, durationSeconds: Double?) {
        voiceBioFilePath = path
        eventListener?.invoke(VoiceBioEvent.NewVoiceBio(path, durationSeconds))
    }

    override fun addAmp(amp: Int, tickDuration: Int) {
        binding.visualizer.addAmp(amp, tickDuration)
    }

    override fun resetVisualization() {
        binding.visualizer.clear()
    }

    override fun ensurePermissions(): Boolean {
        if (hasRecordPermissions(context)) {
            return true
        }
        // TODO maybe communicate out we need permissions?
        (context as? Activity)?.let {
            requestRecordPermissions(it)
        }
        return false
    }

    override fun getScope(): CoroutineScope = viewScope
}