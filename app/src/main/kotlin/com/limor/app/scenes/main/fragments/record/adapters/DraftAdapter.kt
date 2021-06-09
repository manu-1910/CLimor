package com.limor.app.scenes.main.fragments.record.adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.limor.app.R
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.SpecialCharactersInputFilter
import com.limor.app.uimodels.UIDraft
import com.zerobranch.layout.SwipeLayout
import kotlinx.android.synthetic.main.fragment_drafts_item.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import java.io.File


class DraftAdapter(
    var context: Context,
    var list: ArrayList<UIDraft>,
    private val listener: OnItemClickListener,
    private val deleteListener: OnDeleteItemClickListener,
    val duplicateListener: OnDuplicateItemClickListener,
    private val changeNameListener: OnChangeNameClickListener,
    private val resumeListener: OnResumeItemClickListener,
    private val moreListener: OnMoreItemClickListener
) : RecyclerView.Adapter<DraftAdapter.ViewHolder>() {
    private var lastVisiblePlayerLayout: LinearLayout? = null
    var inflator: LayoutInflater = LayoutInflater.from(context)

    private var currentPlayingItemPosition: Int = -1
    private var currentClickedItemPosition: Int = -1
    var mediaPlayer = MediaPlayer()

    // these variables hold the reference to the views of the item that is currently playing:
    // the play, forward, rewind buttons, the current time textview and the seekbar
    private var currentSeekbarPlaying: SeekBar? = null
    private var currentBtnPlayPlaying: ImageButton? = null
    private var currentBtnFwdPlaying: ImageButton? = null
    private var currentBtnRwdPlaying: ImageButton? = null
    private var currentTvPassPlaying: TextView? = null

    // these variables will be used to update the current status of the playing item while it's playing:
    // seekbar and current time
    private val seekUpdater: Runnable
    private val seekHandler: Handler = Handler()


    init {
        seekUpdater = object : Runnable {
            override fun run() {
                seekHandler.postDelayed(this, 150)
                mediaPlayer.let {
                    if (it.isPlaying) {
                        val currentPosition = it.currentPosition
//                        Timber.tag(TAG).d("We are updating the playing state. The current status is pos[$currentPosition]")
                        currentSeekbarPlaying?.let { seekBar ->
//                            Timber.tag(TAG).d("We are updating the seekbar $seekBar")
                            seekBar.progress = currentPosition
                            updateRewFwdButtons(
                                currentBtnRwdPlaying!!,
                                currentBtnFwdPlaying!!,
                                seekBar
                            )
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


    companion object {
        const val TAG = "DRAFT_PLAYER"
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflator.inflate(R.layout.fragment_drafts_item, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDraft = list[position]

        // we will just show the player layout of the last item clicked
        if (currentClickedItemPosition == position) {
            lastVisiblePlayerLayout = holder.playerLayout
            holder.playerLayout.visibility = View.VISIBLE
        } else {
            holder.playerLayout.visibility = View.GONE
        }

        // we have to make sure that the references to the currentViews playing are always updated
        if (position == currentPlayingItemPosition) {
            // these variables should be updated here, and onOtherDraftPlayClicked and in onCurrentDraftPlayClicked
            currentSeekbarPlaying = holder.seekBar
            currentTvPassPlaying = holder.tvTimePass
            currentBtnPlayPlaying = holder.btnPlay
            currentBtnFwdPlaying = holder.btnFfwd
            currentBtnRwdPlaying = holder.btnRew
        }

        // we set title and description
        holder.tvDraftTitle.text = currentDraft.title
        if (!currentDraft.date.isNullOrEmpty()) {
            holder.tvDraftDescription.text = currentDraft.date
        }

        // itemClick listener
        holder.itemView.llDraftItem.setOnClickListener {
            if (currentClickedItemPosition != position) {
                currentClickedItemPosition = position
                listener.onItemClick(currentDraft)
                lastVisiblePlayerLayout?.visibility = View.GONE
                holder.playerLayout.visibility = View.VISIBLE
                lastVisiblePlayerLayout = holder.playerLayout
            }
        }

        /*// edit mode
        if (currentDraft.isEditMode!!) {
            holder.swipeLayout.isEnabledSwipe = true
            holder.ivDraftDelete.setImageResource(R.drawable.ic_delete_draft)
            /*holder.ivDraftDelete.onClick {
                deleteClicked(position)
            }*/
            holder.ivDraftDelete.visibility = View.VISIBLE
        } else {
            holder.swipeLayout.isEnabledSwipe = false
            holder.ivDraftDelete.visibility = View.INVISIBLE
        }*/

        // we have to calculate the duration of every item
        var currentDurationInMillis = 0
        val uri: Uri = Uri.parse(list[position].filePath)
        uri.path?.let {
            val f = File(it)
            if (f.exists()) {
                val mmr = MediaMetadataRetriever()
                try {
                    mmr.setDataSource(context, uri)
                    val durationStr =
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    currentDurationInMillis = durationStr!!.toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Timber.d("Couldn't read metadata and duration. This could be because the file you're trying to access is corrupted")
                }
            }
        }


        // seekBar
        holder.seekBar.max = currentDurationInMillis
        if (position == currentPlayingItemPosition) {
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
        if (position == currentPlayingItemPosition) {
            holder.tvTimePass.text =
                CommonsKt.calculateDurationMediaPlayer(mediaPlayer.currentPosition)
        } else {
            holder.tvTimePass.text = "00:00"
        }
        holder.tvTimeDuration.text = CommonsKt.calculateDurationMediaPlayer(currentDurationInMillis)


        // Play, rewind and forward buttons style
        if (position == currentPlayingItemPosition && mediaPlayer.isPlaying) {
            holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause))
//            enableRewAndFwdButtons(holder.btnRew, holder.btnFfwd, true)
        } else if (position == currentPlayingItemPosition && !mediaPlayer.isPlaying && mediaPlayer.currentPosition > 0) {
            holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play))
//            enableRewAndFwdButtons(holder.btnRew, holder.btnFfwd, true)
        } else {
            holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play))
//            enableRewAndFwdButtons(holder.btnRew, holder.btnFfwd, false)
        }

        // play, rewind and forward button listeners
        holder.btnPlay.setOnClickListener {
            if (currentPlayingItemPosition == position) {
                onCurrentPlayingDraftPlayClicked(holder)
            } else {
                onOtherDraftPlayClicked(holder, position)
            }
        }
        updateRewFwdButtons(holder.btnRew, holder.btnFfwd, holder.seekBar)
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
            moreListener.onMoreItemClick(position, currentDraft)
        }


        // Go to Edit button
        holder.tvResumeItem.onClick {
            stopMediaPlayer()

            currentDraft.length = currentDurationInMillis.toLong()
            resumeListener.onResumeItemClick(position, currentDraft)
        }

        holder.swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
            override fun onOpen(direction: Int, isContinuous: Boolean) {
                if (direction == SwipeLayout.LEFT) {
                    deleteClicked(position)
                    holder.swipeLayout.close()
                }
            }

            override fun onClose() {

            }
        })


    }

    private fun updateRewFwdButtons(btnRew: ImageButton, btnFfwd: ImageButton, seekBar: SeekBar) {
        btnRew.isEnabled = seekBar.progress > 0
        btnFfwd.isEnabled = seekBar.progress < seekBar.max
    }


    // this method should be called from within listener, like onClickListener, onCompletionListener..
    // if you want to achieve this functionality inside onBind method, call the method below
//    private fun enableRewAndFwdButtons(enabled : Boolean) {
//        enableRewAndFwdButtons(currentBtnRwdPlaying, currentBtnFwdPlaying, enabled)
//    }

    // The difference between this one and the upper method, is that this should be called inside onBind method,
    // and the other should be called from inside listeners.
    // The reason behind this is that the references inside a listener can be changed in this situation
    // where we are swapping current listening item with another. And in those changes, we are saving
    // those new references to the buttons in the adequated methods.
//    private fun enableRewAndFwdButtons(btnRwd : ImageButton?, btnFwd: ImageButton?, enabled : Boolean) {
//        var alphaValue = 0.6f
//        if(enabled)
//            alphaValue = 1f
//        btnRwd?.alpha = alphaValue
//        btnFwd?.alpha = alphaValue
//        btnRwd?.isEnabled = enabled
//        btnFwd?.isEnabled = enabled
//    }

    private fun onRewindClicked(
        holder: ViewHolder,
        position: Int
    ) {
        try {
            val nextPosition = holder.seekBar.progress - 5000
            holder.seekBar.progress = nextPosition
            if (currentPlayingItemPosition == position)
                mediaPlayer.seekTo(nextPosition)
            updateRewFwdButtons(holder.btnRew, holder.btnFfwd, holder.seekBar)
        } catch (e: Exception) {
            Timber.d("mediaPlayer.seekTo rewind overflow")
        }
    }

    private fun onForwardClicked(holder: ViewHolder, position: Int) {
        try {
            var nextPosition = holder.seekBar.progress + 5000
            if (nextPosition > holder.seekBar.max)
                nextPosition = holder.seekBar.max
            holder.seekBar.progress = nextPosition
            if (currentPlayingItemPosition == position) {
                mediaPlayer.seekTo(nextPosition)
            }
            updateRewFwdButtons(holder.btnRew, holder.btnFfwd, holder.seekBar)
        } catch (e: Exception) {
            Timber.d("mediaPlayer.seekTo forward overflow")
        }
    }

    private fun onOtherDraftPlayClicked(
        holder: ViewHolder,
        position: Int
    ) {
        val currentDraft = list[position]
        var shouldContinue = false
        currentDraft.filePath?.let { path ->
            val f = File(path)
            if (f.exists())
                shouldContinue = true
        }

        // if the file doesn't exist, we shouldn't continue because we won't be able to play it
        if (!shouldContinue) {
            context.alert(context.getString(R.string.error_accessing_draft_file)) {
                okButton { }
            }.show()
        } else {
            // if it's playing or not, then we have to stop the current player draft playing and setup this new draft player
            currentPlayingItemPosition = position

            // we have to change the previous button image because it's playing state has just changed
//            currentButtonPlaying?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))

            // and set the new "current button" and current seekbar
            // these variables should be updated here, and onBindViewHolder and in onCurrentDraftPlayClicked
            currentBtnPlayPlaying = holder.btnPlay
            currentSeekbarPlaying = holder.seekBar
            currentTvPassPlaying = holder.tvTimePass
            currentBtnFwdPlaying = holder.btnFfwd
            currentBtnRwdPlaying = holder.btnRew



            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                mediaPlayer.stop()
                mediaPlayer.release()
            }

            currentBtnPlayPlaying?.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_pause
                )
            )
//            enableRewAndFwdButtons(true)

            mediaPlayer = MediaPlayer()
            mediaPlayer.setOnCompletionListener {
                onCompletionListener()
            }
            mediaPlayer.setDataSource(currentDraft.filePath)
            try {
                mediaPlayer.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d("The mediaplayer could not be prepared, probably because the file is corrupted")
                context.alert(context.getString(R.string.error_file_corrupted)) {
                    okButton { }
                }
                return
            }
            mediaPlayer.setOnPreparedListener {
                it.start()
                currentSeekbarPlaying?.progress?.let { newProgress ->
                    mediaPlayer.seekTo(newProgress)

                    // this is to restart the audio after you have stopped it at the end of the track
                    if (mediaPlayer.currentPosition == mediaPlayer.duration)
                        mediaPlayer.seekTo(0)
                }

                seekHandler.post(seekUpdater)
            }
        }
    }

    private fun onCompletionListener() {
        currentBtnPlayPlaying?.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_play
            )
        )
        currentSeekbarPlaying?.progress = currentSeekbarPlaying?.max ?: mediaPlayer.duration
//        enableRewAndFwdButtons(false)
        mediaPlayer.pause()
        updateRewFwdButtons(currentBtnRwdPlaying!!, currentBtnFwdPlaying!!, currentSeekbarPlaying!!)
    }

    private fun onCurrentPlayingDraftPlayClicked(
        holder: ViewHolder
    ) {
        // these variables should be updated here, and onOtherDraftPlayClicked and in onBindViewHolder
        currentBtnPlayPlaying = holder.btnPlay
        currentSeekbarPlaying = holder.seekBar
        currentTvPassPlaying = holder.tvTimePass
        currentBtnRwdPlaying = holder.btnRew
        currentBtnFwdPlaying = holder.btnFfwd

        // if it is playing, we just have to pause and change buttons images
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            currentBtnPlayPlaying?.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_play
                )
            )
//            enableRewAndFwdButtons(false)

            // if it's not playing, we just have to play it and change buttons images
        } else {
            // this is to restart the audio after you have stopped it at the end of the track
            if (mediaPlayer.currentPosition == mediaPlayer.duration) {
                mediaPlayer.seekTo(0)
            }
            mediaPlayer.start()
            currentBtnPlayPlaying?.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_pause
                )
            )
//            enableRewAndFwdButtons(true)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }


    fun stopMediaPlayer() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
        } catch (e: Exception) {
            println("Exception stopping media player inside DraftAdapter")
        }
    }

    fun deleteClicked(position: Int) {
        stopMediaPlayer()
        if (position == currentPlayingItemPosition) {
            currentPlayingItemPosition = -1
        }
        if (position == currentClickedItemPosition) {
            currentClickedItemPosition = -1
        }
        deleteListener.onDeleteItemClick(position)
    }

    fun changeNameClicked(currentDraft: UIDraft, position: Int) {
        showSaveDraftAlert(currentDraft.title) { newName ->
            changeNameListener.onNameChangedClick(currentDraft, position, newName)
        }
    }

    private fun showSaveDraftAlert(
        currentName: String?,
        onPositiveClicked: (title: String) -> Unit
    ) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = context.layoutInflater

        val dialogLayout = inflater.inflate(R.layout.dialog_with_edittext, null)
        val positiveButton = dialogLayout.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelButton)
        val editText = dialogLayout.findViewById<TextInputEditText>(R.id.editText)
        val titleText = dialogLayout.findViewById<TextView>(R.id.textTitle)

        titleText.text = context.getString(R.string.edit_draft_title_dialog_title)
        editText.doOnTextChanged { text, start, before, count ->
            positiveButton.isEnabled = count > 0
        }
        editText.setText(currentName)

        editText.filters = arrayOf(SpecialCharactersInputFilter())

        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setCancelable(false)
        val dialog: AlertDialog = dialogBuilder.create()

        positiveButton.onClick {
            onPositiveClicked(editText.text.toString())
            dialog.dismiss()
        }

        cancelButton.onClick {
            dialog.dismiss()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                positiveButton.isEnabled = !p0.isNullOrEmpty()
            }
        })

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset);
            show()
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var btnPlay: ImageButton = itemView.findViewById<View>(R.id.ibPlayPause) as ImageButton
        var btnRew: ImageButton = itemView.findViewById<View>(R.id.ibRew) as ImageButton
        var btnFfwd: ImageButton = itemView.findViewById<View>(R.id.ibFfwd) as ImageButton
        var tvTimePass: TextView = itemView.findViewById<View>(R.id.tvTimePass) as TextView
        var tvTimeDuration: TextView = itemView.findViewById<View>(R.id.tvDuration) as TextView
        var seekBar: SeekBar = itemView.findViewById<View>(R.id.sbProgress) as SeekBar
        var tvDraftTitle: TextView = itemView.findViewById(R.id.tvDraftTitle) as TextView
        var tvDraftDescription: TextView =
            itemView.findViewById(R.id.tvDraftDescription) as TextView
        var ivDraftDelete: ImageView = itemView.findViewById(R.id.ivDraftDelete) as ImageView
        var playerLayout: LinearLayout = itemView.findViewById(R.id.itemPlayer) as LinearLayout
        var btnMore: ImageButton = itemView.findViewById<View>(R.id.btnMore) as ImageButton
        var tvResumeItem: TextView = itemView.findViewById<View>(R.id.tvResumeRecording) as TextView
        var swipeLayout: SwipeLayout =
            itemView.findViewById<SwipeLayout>(R.id.swipeLayout) as SwipeLayout

    }

    interface OnItemClickListener {
        fun onItemClick(item: UIDraft)
    }

    interface OnChangeNameClickListener {
        fun onNameChangedClick(
            item: UIDraft,
            position: Int,
            newName: String
        )
    }

    interface OnDeleteItemClickListener {
        fun onDeleteItemClick(position: Int)
    }

    interface OnDuplicateItemClickListener {
        fun onDuplicateItemClick(position: Int)
    }

    interface OnResumeItemClickListener {
        fun onResumeItemClick(position: Int, item: UIDraft)
    }

    interface OnMoreItemClickListener {
        fun onMoreItemClick(position: Int, item: UIDraft)
    }


}