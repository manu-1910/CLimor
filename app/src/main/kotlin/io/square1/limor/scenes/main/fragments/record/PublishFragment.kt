package io.square1.limor.scenes.main.fragments.record

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.viewmodels.DraftViewModel
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.uimodels.UIDraft
import kotlinx.android.synthetic.main.fragment_publish.*
import kotlinx.android.synthetic.main.toolbar_default.btnToolbarRight
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PublishFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    lateinit var recordingItem : UIDraft
    private lateinit var mediaPlayer: MediaPlayer
    private var seekHandler = Handler()
    private var run: Runnable? = null



    companion object {
        val TAG: String = PublishFragment::class.java.simpleName
        fun newInstance() = PublishFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_publish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        recordingItem = UIDraft()
        recordingItem = arguments!!["recordingItem"] as UIDraft

        bindViewModel()
        configureToolbar()
        listeners()
        configureMediaPlayer()
    }



    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(DraftViewModel::class.java)
        }
    }



    private fun configureToolbar() {

        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_publish)

        //Toolbar Left
        btnClose.onClick {
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnToolbarRight.visibility = View.GONE

    }


    private fun listeners(){

        placeHolder.onClick {
            toast("Choose a photo here")
        }

        btnSaveAsDraft.onClick {
            toast("Save as Daft here")
        }

        btnPublish.onClick {
            toast ("Publish here")
        }
    }


    private fun configureMediaPlayer(){

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(recordingItem.filePath)


        sbProgress.max = mediaPlayer.duration
        //sbProgress.tag = position
        //run.run();
        sbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mediaPlayer.seekTo(progress)
                if(mediaPlayer.duration <= progress){
                    ibPlayPause.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.play))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        tvTimePass.text = "0:00"
        tvDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
        ibPlayPause.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                ibPlayPause.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.pause))
                run = Runnable {
                    // Updateing SeekBar every 100 miliseconds
                    sbProgress.progress = mediaPlayer.currentPosition
                    seekHandler.postDelayed(run, 100)
                    //For Showing time of audio(inside runnable)
                    val miliSeconds = mediaPlayer.currentPosition
                    if (miliSeconds != 0) {
                        //if audio is playing, showing current time;
                        val minutes =
                            TimeUnit.MILLISECONDS.toMinutes(miliSeconds.toLong())
                        val seconds =
                            TimeUnit.MILLISECONDS.toSeconds(miliSeconds.toLong())
                        if (minutes == 0L) {
                            tvTimePass.text = "0:$seconds"
                            tvDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                        } else {
                            if (seconds >= 60) {
                                val sec = seconds - minutes * 60
                                tvTimePass.text = "$minutes:$sec"
                                tvDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                            }
                        }
                    } else {
                        //Displaying total time if audio not playing
                        val totalTime = mediaPlayer.duration
                        val minutes =
                            TimeUnit.MILLISECONDS.toMinutes(totalTime.toLong())
                        val seconds =
                            TimeUnit.MILLISECONDS.toSeconds(totalTime.toLong())
                        if (minutes == 0L) {
                            tvTimePass.text = "0:$seconds"
                            tvDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                        } else {
                            if (seconds >= 60) {
                                val sec = seconds - minutes * 60
                                tvTimePass.text = "$minutes:$sec"
                                tvDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                            }
                        }
                    }
                }
                run!!.run()
            } else {
                mediaPlayer.pause()
                ibPlayPause.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.play))
            }
        }


        //Forward button
        ibFfwd.onClick {
            try {
                mediaPlayer.seekTo(30000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        //Rew button
        ibRew.onClick {
            try {
                mediaPlayer.seekTo(-30000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}

