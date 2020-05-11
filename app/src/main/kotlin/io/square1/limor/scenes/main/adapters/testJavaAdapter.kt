package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.R
import io.square1.limor.uimodels.UIDraft
import org.jetbrains.anko.sdk23.listeners.onClick
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class testJavaAdapter(
    var context: Context,
    list: ArrayList<UIDraft>,
    private val listener: testJavaAdapter.OnItemClickListener,
    private val deleteListener: testJavaAdapter.OnDeleteItemClickListener
) : RecyclerView.Adapter<testJavaAdapter.ViewHolder>() {
    var inflator: LayoutInflater
    var list: ArrayList<UIDraft> = ArrayList()
    var seekHandler = Handler()
    var run: Runnable? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = inflator.inflate(R.layout.fragment_drafts_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val modelList = list[position]

        holder.tvDraftTitle.text = modelList.title
        holder.tvDraftDescription.text = modelList.caption
        holder.itemView.setOnClickListener { listener.onItemClick(modelList) }
        if (modelList.isEditMode!!) {
            holder.ivDraftDelete.setImageResource(R.drawable.delete_symbol)
            holder.ivDraftDelete.onClick { deleteListener.onDeleteItemClick(position) }
        } else {
            holder.ivDraftDelete.setImageResource(android.R.color.transparent)
        }

        holder.tvAudioName.text = modelList!!.title

        // Initializing MediaPlayer
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaPlayer.setDataSource(modelList.filePath)
            mediaPlayer.prepare() // might take long for buffering.
        } catch (e: IOException) {
            e.printStackTrace()
        }
        holder.seekBar.max = mediaPlayer.duration
        holder.seekBar.tag = position
        //run.run();
        holder.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        holder.tvAudioLength.text = "0:00/" + calculateDuration(mediaPlayer.duration)
        holder.btnPlay.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause))
                run = Runnable {
                    // Updateing SeekBar every 100 miliseconds
                    holder.seekBar.progress = mediaPlayer.currentPosition
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
                            holder.tvAudioLength.text = "0:$seconds/" + calculateDuration(mediaPlayer.duration)
                        } else {
                            if (seconds >= 60) {
                                val sec = seconds - minutes * 60
                                holder.tvAudioLength.text = "$minutes:$sec/" + calculateDuration(mediaPlayer.duration)
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
                            holder.tvAudioLength.text = "0:$seconds"
                        } else {
                            if (seconds >= 60) {
                                val sec = seconds - minutes * 60
                                holder.tvAudioLength.text = "$minutes:$sec"
                            }
                        }
                    }
                }
                run!!.run()
            } else {
                mediaPlayer.pause()
                holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var btnPlay: ImageView
        var tvAudioLength: TextView
        var tvAudioName: TextView
        var seekBar: SeekBar
        var tvDraftTitle: TextView
        var tvDraftDescription: TextView
        var ivDraftDelete: ImageView
        var itemPlayer: RelativeLayout

        init {
            // mTvAudioLength =(TextView) itemView.findViewById(R.id.tv_audio_lenght);
            // mSeekBar = (SeekBar) itemView.findViewById(R.id.seekBar);
            btnPlay = itemView.findViewById<View>(R.id.ivPlayPause) as ImageView
            tvAudioLength = itemView.findViewById<View>(R.id.tvIndex) as TextView
            tvAudioName = itemView.findViewById<View>(R.id.tvDraftTitle) as TextView
            seekBar = itemView.findViewById<View>(R.id.sbProgress) as SeekBar

            tvDraftTitle = itemView.findViewById(R.id.tvDraftTitle) as TextView
            tvDraftDescription = itemView.findViewById(R.id.tvDraftDescription) as TextView
            ivDraftDelete = itemView.findViewById(R.id.ivDraftDelete) as ImageView
            itemPlayer = itemView.findViewById(R.id.itemPlayer) as RelativeLayout
        }
    }

    private fun calculateDuration(duration: Int): String {
        var finalDuration = ""
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong())
        if (minutes == 0L) {
            finalDuration = "0:$seconds"
        } else {
            if (seconds >= 60) {
                val sec = seconds - minutes * 60
                finalDuration = "$minutes:$sec"
            }
        }
        return finalDuration
    }

    init {
        this.list = list
        inflator = LayoutInflater.from(context)
    }


    interface OnItemClickListener {
        fun onItemClick(item: UIDraft)
    }

    interface OnDeleteItemClickListener {
        fun onDeleteItemClick(position: Int)
    }


}