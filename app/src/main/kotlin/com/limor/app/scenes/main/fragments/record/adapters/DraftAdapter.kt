package com.limor.app.scenes.main.fragments.record.adapters

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.uimodels.UIDraft
import kotlinx.android.synthetic.main.fragment_drafts_item.view.*
import org.jetbrains.anko.sdk23.listeners.onClick


class DraftAdapter(
        var context: Context,
        var list: ArrayList<UIDraft>,
        private val listener: OnItemClickListener,
        val deleteListener: OnDeleteItemClickListener,
        val duplicateListener: OnDuplicateItemClickListener,
        val changeNameListener: OnChangeNameClickListener,
        val resumeListener: OnResumeItemClickListener
) : RecyclerView.Adapter<DraftAdapter.ViewHolder>() {

    var inflator: LayoutInflater = LayoutInflater.from(context)

    private var currentPlayingItemPosition: Int = -1
    private var currentClickedItemPosition: Int = -1
    var mediaPlayer = MediaPlayer()

    private var lastSelectedDraftPosition = DRAFT_NOT_SELECTED




    companion object {
        const val TAG = "DRAFT_PLAYER"
        const val DRAFT_NOT_SELECTED = -1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflator.inflate(R.layout.fragment_drafts_item, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDraft = list[position]


        if (lastSelectedDraftPosition == position) {
            holder.itemView.llDraftItem.setBackgroundColor(ContextCompat.getColor(context, R.color.brandPrimary500))
        } else holder.itemView.llDraftItem.setBackgroundColor(ContextCompat.getColor(context, R.color.brandSecondary400))


        // we set title and description
        holder.tvDraftTitle.text = currentDraft.title
        if (!currentDraft.date.isNullOrEmpty()) {
            holder.tvDraftDescription.text = currentDraft.date
        }

        // itemClick listener
        holder.itemView.setOnClickListener {
            val copyOfLastSelectedDraftPosition = lastSelectedDraftPosition
            lastSelectedDraftPosition = position
            if (copyOfLastSelectedDraftPosition != lastSelectedDraftPosition) {
                notifyItemChanged(copyOfLastSelectedDraftPosition)
                notifyItemChanged(lastSelectedDraftPosition)
            } else {
                lastSelectedDraftPosition = DRAFT_NOT_SELECTED
                notifyItemChanged(copyOfLastSelectedDraftPosition)
            }
            listener.onItemClick(currentDraft, position)
        }

        // edit mode
        if (currentDraft.isEditMode!!) {
            holder.ivDraftDelete.setImageResource(R.drawable.delete_symbol)
            holder.ivDraftDelete.onClick {
                deleteClicked(position)
            }
        } else {
            holder.ivDraftDelete.setImageResource(android.R.color.transparent)
        }

        // Play, rewind and forward buttons style
        if (position == currentPlayingItemPosition && mediaPlayer.isPlaying) {
           // holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause))
//            enableRewAndFwdButtons(holder.btnRew, holder.btnFfwd, true)
        } else if (position == currentPlayingItemPosition && !mediaPlayer.isPlaying && mediaPlayer.currentPosition > 0) {
           // holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
//            enableRewAndFwdButtons(holder.btnRew, holder.btnFfwd, true)
        } else {
           // holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
//            enableRewAndFwdButtons(holder.btnRew, holder.btnFfwd, false)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    fun getLastSelectedDraftPosition() = lastSelectedDraftPosition

    private fun stopMediaPlayer() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
        } catch (e: Exception) {
            println("Exception stopping media player inside DraftAdapter")
        }
    }

    private fun deleteClicked(position: Int) {
        stopMediaPlayer()
        if (position == currentPlayingItemPosition) {
            currentPlayingItemPosition = -1
        }
        if (position == currentClickedItemPosition) {
            currentClickedItemPosition = -1
        }
        deleteListener.onDeleteItemClick(position)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDraftTitle: TextView = itemView.findViewById(R.id.tvDraftTitle) as TextView
        var tvDraftDescription: TextView = itemView.findViewById(R.id.tvDraftDescription) as TextView
        var ivDraftDelete: ImageView = itemView.findViewById(R.id.ivDraftDelete) as ImageView
    }


    interface OnItemClickListener {
        fun onItemClick(item: UIDraft, position: Int)
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


}