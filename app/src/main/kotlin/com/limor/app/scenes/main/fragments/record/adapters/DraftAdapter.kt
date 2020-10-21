package com.limor.app.scenes.main.fragments.record.adapters

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIDraft
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber


class DraftAdapter(
    var context: Context,
    var list: ArrayList<UIDraft>,
    private val listener: OnItemClickListener,
    private val deleteListener: OnDeleteItemClickListener,
    private val duplicateListener: OnDuplicateItemClickListener,
    private val editListener: OnEditItemClickListener,
    private val navController: NavController
) : RecyclerView.Adapter<DraftAdapter.ViewHolder>() {
    var inflator: LayoutInflater
    //var list: ArrayList<UIDraft> = ArrayList()
    //val mainHandler = Handler(Looper.getMainLooper())
    private var run: Runnable? = null
    private var seekHandler = Handler()
    var playingPlayerPosition: Int = -1
    var clickedPlayerPosition: Int = -1
    var mediaPlayer2 = MediaPlayer()


    private var currentSeekbarPlaying: SeekBar? = null
    private var currentButtonPlaying: ImageButton? = null

    private val updater: Runnable
    private val handler: Handler = Handler()


    init {
        mediaPlayer2.setOnCompletionListener {
            currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
            mediaPlayer2.pause()
        }

        updater = object : Runnable {
            override fun run() {
                handler.postDelayed(this, 150)
                mediaPlayer2.let {
                    if(it.isPlaying) {
                        val currentPosition = it.currentPosition
                        currentSeekbarPlaying?.progress = currentPosition
//                        listener.onProgress(comment, currentPosition)
                    }
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflator.inflate(R.layout.fragment_drafts_item, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modelList = list[position]

        if(clickedPlayerPosition == position){
            holder.playerLayout.visibility = View.VISIBLE
        }else{
            holder.playerLayout.visibility = View.GONE
        }

        holder.tvDraftTitle.text = modelList.title
        if (!modelList.date.isNullOrEmpty()){
            holder.tvDraftDescription.text = modelList.date
        }

        holder.itemView.setOnClickListener {
            clickedPlayerPosition = position
            //If some cast is playing stop it.
//            if(mediaPlayer.isPlaying){
////                mediaPlayer.stop()
////                holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
////            }
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

        holder.tvDraftTitle.text = modelList.title

        val uri: Uri = Uri.parse(list[position].filePath)
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, uri)
        val durationStr =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val currentDurationInMillis = durationStr.toInt()
        holder.seekBar.max = currentDurationInMillis
        holder.seekBar.tag = position

        holder.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && playingPlayerPosition == position) {
                    mediaPlayer2.seekTo(progress)
                }
                if (mediaPlayer2.duration <= progress) {
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        holder.tvTimePass.text = "0:00"
        holder.tvTimeDuration.text = CommonsKt.calculateDurationMediaPlayer(currentDurationInMillis)


        //PLAY BUTTON
        holder.btnPlay.setOnClickListener {

            if(playingPlayerPosition == position) {
                onCurrentPlayingDraftPlayClicked(holder, position)
            } else {
                onOtherDraftPlayClicked(holder, position)
            }


        }


        //Forward button
        holder.btnFfwd.onClick {
            if(playingPlayerPosition == position) {
                try {
                    val nextPosition = mediaPlayer2.currentPosition + 30000
                    if(nextPosition < mediaPlayer2.duration)
                        mediaPlayer2.seekTo(nextPosition)
                    else if(mediaPlayer2.currentPosition < mediaPlayer2.duration)
                        mediaPlayer2.seekTo(mediaPlayer2.duration)
                } catch (e: Exception) {
                    Timber.d("mediaPlayer.seekTo forward overflow")
                }
            }
        }


        //Rew button
        holder.btnRew.onClick {
            if(playingPlayerPosition == position) {
                try {
                    mediaPlayer2.seekTo(mediaPlayer2.currentPosition - 30000)
                } catch (e: Exception) {
                    Timber.d("mediaPlayer.seekTo rewind overflow")
                }
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

            modelList.length = currentDurationInMillis.toLong()
            editListener.onEditItemClick(modelList)
        }

    }

    private fun onOtherDraftPlayClicked(
        holder: ViewHolder,
        position: Int
    ) {
        // if it's playing or not, then we have to stop the current player draft playing and setup this new draft player
        val currentDraft = list[position]
        playingPlayerPosition = position
        // we have to change the previous button image because it's playing state has just changed
        currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))

        // and set the new "current button" and current seekbar
        currentButtonPlaying = holder.btnPlay
        currentSeekbarPlaying = holder.seekBar

        currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause))


        if(mediaPlayer2.isPlaying) {
            mediaPlayer2.pause()
            mediaPlayer2.stop()
            mediaPlayer2.release()
        }

        mediaPlayer2 = MediaPlayer()
        mediaPlayer2.setDataSource(currentDraft.filePath)
        mediaPlayer2.prepare()
        mediaPlayer2.setOnPreparedListener {
            it.start()
            currentSeekbarPlaying?.progress?.let {
                mediaPlayer2.seekTo(it)
            }

            run = Runnable {
                // Updateing SeekBar every 100 miliseconds
                var miliSeconds = 0
                try {
                    miliSeconds = mediaPlayer2.currentPosition
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                holder.seekBar.progress = miliSeconds
                seekHandler.postDelayed(run, 100)
                //For Showing time of audio(inside runnable)

                holder.tvTimeDuration.text = CommonsKt.calculateDurationMediaPlayer(
                    mediaPlayer2.duration
                )
                holder.tvTimePass.text = CommonsKt.calculateDurationMediaPlayer(
                    miliSeconds
                )
            }
            run!!.run()

        }
    }

    private fun onCurrentPlayingDraftPlayClicked(
        holder: ViewHolder,
        position: Int
    ) {
        // if it is playing, we just have to pause and change button image
        if(mediaPlayer2.isPlaying) {
            mediaPlayer2.pause()
            currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))

            // if it's not playing, we just have to play it
        } else {
            mediaPlayer2.start()

            currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause))
            currentButtonPlaying = holder.btnPlay
            currentSeekbarPlaying = holder.seekBar
        }


    }


    override fun getItemCount(): Int {
        return list.size
    }


    private fun stopMediaPlayer(){
        try {
            if(mediaPlayer2.isPlaying){
                mediaPlayer2.stop()
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