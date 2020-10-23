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
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import java.io.File

const val TAG = "DRAFT_PLAYER"

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
    private var currentPlayingItemPosition: Int = -1
    private var currentClickedItemPosition: Int = -1
    var mediaPlayer = MediaPlayer()


    private var currentSeekbarPlaying: SeekBar? = null
    private var currentButtonPlaying: ImageButton? = null
    private var currentTvPassPlaying: TextView? = null

    private val seekUpdater: Runnable
    private val seekHandler: Handler = Handler()


    init {


        seekUpdater = object : Runnable {
            override fun run() {
                seekHandler.postDelayed(this, 150)
                mediaPlayer.let {
                    if(it.isPlaying) {
                        val currentPosition = it.currentPosition
//                        Timber.tag(TAG).d("We are updating the playing state. The current status is pos[$currentPosition]")
                        currentSeekbarPlaying?.let {seekBar ->
//                            Timber.tag(TAG).d("We are updating the seekbar $seekBar")
                            seekBar.progress = currentPosition
                        }
                        currentTvPassPlaying?.let { tvPass ->
//                            Timber.tag(TAG).d("We are updating the tv $tvPass")
                            tvPass.text = CommonsKt.calculateDurationMediaPlayer(
                                currentPosition
                            )
                        }
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
        val currentDraft = list[position]

        // we will just show the player layout of the last item clicked
        if(currentClickedItemPosition == position){
            holder.playerLayout.visibility = View.VISIBLE
        }else{
            holder.playerLayout.visibility = View.GONE
        }

        // we have to make sure that the references to the currentViews playing are always updated
        if(position == currentPlayingItemPosition) {
            currentSeekbarPlaying = holder.seekBar
            currentTvPassPlaying = holder.tvTimePass
            currentButtonPlaying = holder.btnPlay
        }

        // we set title and description
        holder.tvDraftTitle.text = currentDraft.title
        if (!currentDraft.date.isNullOrEmpty()){
            holder.tvDraftDescription.text = currentDraft.date
        }

        // itemClick listener
        holder.itemView.setOnClickListener {
            if(currentClickedItemPosition != position) {
                currentClickedItemPosition = position
                listener.onItemClick(currentDraft)
                notifyDataSetChanged()
            }
        }

        // edit mode
        if (currentDraft.isEditMode!!) {
            holder.ivDraftDelete.setImageResource(R.drawable.delete_symbol)
            holder.ivDraftDelete.onClick { deleteListener.onDeleteItemClick(position) }
        } else {
            holder.ivDraftDelete.setImageResource(android.R.color.transparent)
        }

        // we have to calculate the duration of every item
        var currentDurationInMillis = 0
        val uri: Uri = Uri.parse(list[position].filePath)
        uri.path?.let {
            val f = File(it)
            if(f.exists()) {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(context, uri)
                val durationStr =
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                currentDurationInMillis = durationStr.toInt()
            }
        }


        // seekBar
        holder.seekBar.max = currentDurationInMillis
        if(position == currentPlayingItemPosition) {
            holder.seekBar.progress = mediaPlayer.currentPosition
        } else {
            holder.seekBar.progress = 0
        }
        holder.seekBar.tag = position
        holder.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // if we click on the current playing item seekbar, then we'll seek the audio position
                if (fromUser && currentPlayingItemPosition == position) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // textViews
        holder.tvTimePass.text = "00:00"
        holder.tvTimeDuration.text = CommonsKt.calculateDurationMediaPlayer(currentDurationInMillis)


        // Play, rewind and forward buttons style
        if(position == currentPlayingItemPosition && mediaPlayer.isPlaying) {
            holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause))
            enableRewAndFwdButtons(holder, true)

        } else {
            holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
            enableRewAndFwdButtons(holder, false)
        }

        // play, rewind and forward button listeners
        holder.btnPlay.setOnClickListener {
            if(currentPlayingItemPosition == position) {
                onCurrentPlayingDraftPlayClicked(holder, position)
            } else {
                onOtherDraftPlayClicked(holder, position)
            }
        }

        // Forward button
        holder.btnFfwd.onClick {
            onForwardClicked(holder, position)
        }

        // Rewind button
        holder.btnRew.onClick {
            onRewindClicked(holder, position)
        }


        // More button -> show options menu
        holder.btnMore.onClick {
            showMorePopupMenu(holder, position)
        }


        // Go to Edit button
        holder.tvEditItem.onClick {
            stopMediaPlayer()

            currentDraft.length = currentDurationInMillis.toLong()
            editListener.onEditItemClick(currentDraft)
        }

    }

    private fun enableRewAndFwdButtons(holder: ViewHolder, enabled : Boolean) {
        var alphaValue = 0.6f
        if(enabled)
            alphaValue = 1f
        holder.btnRew.alpha = alphaValue
        holder.btnFfwd.alpha = alphaValue
        holder.btnFfwd.isEnabled = enabled
        holder.btnRew.isEnabled = enabled
    }

    private fun onRewindClicked(
        holder: ViewHolder,
        position: Int
    ) {
        if (currentPlayingItemPosition == position) {
            try {
                mediaPlayer.seekTo(mediaPlayer.currentPosition - 30000)
                holder.seekBar.progress = mediaPlayer.currentPosition - 30000
            } catch (e: Exception) {
                Timber.d("mediaPlayer.seekTo rewind overflow")
            }
        }
    }

    private fun onForwardClicked(holder :ViewHolder, position: Int) {
        if (currentPlayingItemPosition == position) {
            try {
                val nextPosition = mediaPlayer.currentPosition + 30000
                if (nextPosition < mediaPlayer.duration) {
                    mediaPlayer.seekTo(nextPosition)
                    holder.seekBar.progress = nextPosition
                } else if (mediaPlayer.currentPosition < mediaPlayer.duration) {
                    mediaPlayer.seekTo(mediaPlayer.duration)
                    holder.seekBar.progress = mediaPlayer.duration
                }
            } catch (e: Exception) {
                Timber.d("mediaPlayer.seekTo forward overflow")
            }
        }
    }

    private fun showMorePopupMenu(
        holder: ViewHolder,
        position: Int
    ) {
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

    private fun onOtherDraftPlayClicked(
        holder: ViewHolder,
        position: Int
    ) {
        val currentDraft = list[position]
        var shouldContinue = false
        currentDraft.filePath?.let {path ->
            val f = File(path)
            if(f.exists())
                shouldContinue = true
        }

        // if the file doesn't exist, we shouldn't continue because we won't be able to play it
        if(!shouldContinue) {
            context.alert(context.getString(R.string.error_accessing_draft_file)) {
                okButton {  }
            }.show()
        } else {
            // if it's playing or not, then we have to stop the current player draft playing and setup this new draft player
            currentPlayingItemPosition = position

            // we have to change the previous button image because it's playing state has just changed
//            currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))

            // and set the new "current button" and current seekbar
            currentButtonPlaying = holder.btnPlay
            currentSeekbarPlaying = holder.seekBar
            currentTvPassPlaying = holder.tvTimePass



            if(mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                mediaPlayer.stop()
                mediaPlayer.release()
            }

            currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause))
            enableRewAndFwdButtons(holder, true)

            mediaPlayer = MediaPlayer()
            mediaPlayer.setOnCompletionListener {
                onCompletionListener(holder)
            }
            mediaPlayer.setDataSource(currentDraft.filePath)
            mediaPlayer.prepare()
            mediaPlayer.setOnPreparedListener {
                it.start()
                currentSeekbarPlaying?.progress?.let {newProgress ->
                    mediaPlayer.seekTo(newProgress)
                }

                seekHandler.post(seekUpdater)
            }
        }
    }

    private fun onCompletionListener(holder: ViewHolder) {
        currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
        enableRewAndFwdButtons(holder, false)
        mediaPlayer.pause()
    }

    private fun onCurrentPlayingDraftPlayClicked(
        holder: ViewHolder,
        position: Int
    ) {
        currentButtonPlaying = holder.btnPlay
        currentSeekbarPlaying = holder.seekBar
        currentTvPassPlaying = holder.tvTimePass

        // if it is playing, we just have to pause and change buttons images
        if(mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
            enableRewAndFwdButtons(holder, false)
            // if it's not playing, we just have to play it and change buttons images
        } else {
            mediaPlayer.start()
            currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause))
            enableRewAndFwdButtons(holder, true)
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