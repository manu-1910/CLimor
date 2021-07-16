package com.limor.app.scenes.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.limor.app.R
import kotlinx.android.synthetic.main.item_input_with_audio.view.*


class TextAndVoiceInput @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        initView()
        readAttributes(attrs)
    }

    private fun initView() {
        inflate(context, R.layout.item_input_with_audio, this)
        btnPodcastStartVoiceComment.setOnClickListener {
            val visualizerView = findViewById<VisualizerView>(R.id.visualizer)

            val recordingSampler = RecordingSampler()
            //recordingSampler.setVolumeListener(this) // for custom implements

            recordingSampler.setSamplingInterval(100) // voice sampling interval

            recordingSampler.link(visualizerView) // link to visualizer


            recordingSampler.startRecording()
        }
    }


    private fun readAttributes(attrs: AttributeSet?) {
//        val typedArray =
//            context.obtainStyledAttributes(attrs, R.styleable.CustomBtn, 0, 0)
//        state = typedArray.getInt(R.styleable.CustomBtn_cbtn_state, 0)
//        val text = typedArray.getString(R.styleable.CustomBtn_cbtn_name) ?: ""
//        changeState(state)
//        setTextBtn(text)
    }

}