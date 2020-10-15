package io.square1.limor.scenes.main.fragments.record.adapters

import android.content.Context
import android.media.AudioAttributes

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.R
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.uimodels.UIDeleteResponse
import io.square1.limor.uimodels.UIDraft
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import java.util.concurrent.TimeUnit


class DraftAdapter(
    var context: Context,
    var list : ArrayList<UIDraft>,
    private val listener: OnItemClickListener,
    private val deleteListener: OnDeleteItemClickListener,
    private val duplicateListener: OnDuplicateItemClickListener,
    private val editListener: OnEditItemClickListener,
    private val navController: NavController
) : RecyclerView.Adapter<DraftAdapter.ViewHolder>() {
    var inflator: LayoutInflater
    //var list: ArrayList<UIDraft> = ArrayList()
    val mainHandler = Handler(Looper.getMainLooper())
    var playerPosition: Int = -1
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflator.inflate(R.layout.fragment_drafts_item, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modelList = list[position]

        // Initializing MediaPlayer
        mediaPlayer = MediaPlayer()
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
//        try {
////            mediaPlayer.setDataSource(modelList.filePath)
////            mediaPlayer.prepare() // might take long for buffering.
////        } catch (e: Exception) {
////            e.printStackTrace()
////        }

        if(playerPosition == position){
            holder.playerLayout.visibility = View.VISIBLE
        }else{
            holder.playerLayout.visibility = View.GONE
        }

        holder.tvDraftTitle.text = modelList.title
        if (!modelList.date.isNullOrEmpty()){
            holder.tvDraftDescription.text = modelList.date
        }

        holder.itemView.setOnClickListener {
            playerPosition = position
            //If some cast is playing stop it.
            if(mediaPlayer.isPlaying){
                mediaPlayer.stop()
                holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
            }
            notifyDataSetChanged()
            holder.playerLayout.visibility = View.VISIBLE
            listener.onItemClick(modelList)
        }
        if (modelList.isEditMode!!) {
            holder.ivDraftDelete.setImageResource(R.drawable.delete_symbol)
            holder.ivDraftDelete.onClick { deleteListener.onDeleteItemClick(position) }
        } else {
            holder.ivDraftDelete.setImageResource(android.R.color.transparent)
        }

        holder.tvDraftTitle.text = modelList!!.title


        holder.seekBar.max = mediaPlayer.duration
        holder.seekBar.tag = position

        holder.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress)
                }
                if(mediaPlayer.duration <= progress){
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        holder.tvTimePass.text = "0:00"
        holder.tvTimeDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)

        holder.btnPlay.setOnClickListener {

            try {
                mediaPlayer.setDataSource(list[position].filePath)
                mediaPlayer.prepare() // might take long for buffering.
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mediaPlayer.setOnCompletionListener {
                holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
                mediaPlayer.stop()
            }


            mediaPlayer.start()

            holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause))

            mainHandler.post(object : Runnable {
                override fun run() {
                    // Updateing SeekBar every 100 miliseconds
                    holder.seekBar.progress = mediaPlayer.currentPosition
                    //For Showing time of audio(inside runnable)
                    val miliSeconds = mediaPlayer.currentPosition
                    if (miliSeconds != 0) {
                        //if audio is playing, showing current time;
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds.toLong())
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds.toLong())
                        if (minutes == 0L) {
                            holder.tvTimePass.text = "00:" + String.format("%02d", seconds)
                            holder.tvTimeDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                        } else {
                            if (seconds >= 60) {
                                val sec = seconds - minutes * 60
                                holder.tvTimePass.text = String.format("%02d", minutes)+":"+String.format("%02d", seconds)
                                holder.tvTimeDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                            }
                        }
                    } else {
                        //Displaying total time if audio not playing
                        val totalTime = mediaPlayer.duration
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime.toLong())
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime.toLong())
                        if (minutes == 0L) {
                            holder.tvTimePass.text = "00:" + String.format("%02d", seconds)
                            holder.tvTimeDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                        } else {
                            if (seconds >= 60) {
                                val sec = seconds - minutes * 60
                                holder.tvTimePass.text = String.format("%02d", minutes)+":"+String.format("%02d", seconds)
                                holder.tvTimeDuration.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                            }
                        }
                    }

                    mainHandler.postDelayed(this, 100)
                }
            })

        }


        //Forward button
        holder.btnFfwd.onClick {
            try {
                val nextPosition = mediaPlayer.currentPosition + 30000
                if(nextPosition < mediaPlayer.duration)
                    mediaPlayer.seekTo(nextPosition)
                else if(mediaPlayer.currentPosition < mediaPlayer.duration)
                    mediaPlayer.seekTo(mediaPlayer.duration)
            } catch (e: Exception) {
                Timber.d("mediaPlayer.seekTo forward overflow")
            }
        }


        //Rew button
        holder.btnRew.onClick {
            try {
                mediaPlayer.seekTo(mediaPlayer.currentPosition - 30000)
            } catch (e: Exception) {
                Timber.d("mediaPlayer.seekTo rewind overflow")
            }
        }


        //More button -> show options menu
        holder.btnMore.onClick {
            //creating a popup menu
            val popup = PopupMenu(context, holder.btnMore)
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_drafts_iems_adapter)
            //adding click listener
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_duplicate_cast -> {
                        //Toast.makeText(context, "duplicate cast", Toast.LENGTH_SHORT).show()
                        stopMediaPlayer()
                        duplicateListener.onDuplicateItemClick(position)
                        true
                    }
                    R.id.menu_delete_cast -> {
                        //Toast.makeText(context, "delete cast", Toast.LENGTH_SHORT).show()
                        stopMediaPlayer()
                        deleteListener.onDeleteItemClick(position)
                        true
                    }
                    else -> false
                }
            }
            //displaying the popup
            popup.show()
        }


        //Go to Edit button
        holder.tvEditItem.onClick {
            stopMediaPlayer()

            modelList.length = mediaPlayer.duration.toLong()
            editListener.onEditItemClick(modelList)
        }

    }


    override fun getItemCount(): Int {
        return list.size
    }


    private fun stopMediaPlayer(){
        try {
            if(mediaPlayer.isPlaying){
                mediaPlayer.stop()
            }
        } catch (e: Exception) {
            println("Exception stopping media player inside DraftAdapter")
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var btnPlay: ImageButton
        var btnRew: ImageButton
        var btnFfwd: ImageButton
        var tvTimePass: TextView
        var tvTimeDuration: TextView
        var seekBar: SeekBar
        var tvDraftTitle: TextView
        var tvDraftDescription: TextView
        var ivDraftDelete: ImageView
        var playerLayout: LinearLayout
        var btnMore: ImageButton
        var tvEditItem: TextView

        init {
            btnPlay = itemView.findViewById<View>(R.id.ibPlayPause) as ImageButton
            btnRew = itemView.findViewById<View>(R.id.ibRew) as ImageButton
            btnFfwd = itemView.findViewById<View>(R.id.ibFfwd) as ImageButton
            tvTimePass = itemView.findViewById<View>(R.id.tvTimePass) as TextView
            tvTimeDuration = itemView.findViewById<View>(R.id.tvDuration) as TextView
            seekBar = itemView.findViewById<View>(R.id.sbProgress) as SeekBar
            btnMore = itemView.findViewById<View>(R.id.btnMore) as ImageButton
            tvEditItem = itemView.findViewById<View>(R.id.tvEditItem) as TextView
            tvDraftTitle = itemView.findViewById(R.id.tvDraftTitle) as TextView
            tvDraftDescription = itemView.findViewById(R.id.tvDraftDescription) as TextView
            ivDraftDelete = itemView.findViewById(R.id.ivDraftDelete) as ImageView
            playerLayout = itemView.findViewById(R.id.itemPlayer) as LinearLayout
        }

    }

    init {
        this.list = list
        inflator = LayoutInflater.from(context)
    }

    interface OnEditItemClickListener {
        fun onEditItemClick(item: UIDraft)
    }

    interface OnItemClickListener {
        fun onItemClick(item: UIDraft)
    }

    interface OnDeleteItemClickListener {
        fun onDeleteItemClick(position: Int)
    }

    interface OnDuplicateItemClickListener {
        fun onDuplicateItemClick(position: Int)
    }

}