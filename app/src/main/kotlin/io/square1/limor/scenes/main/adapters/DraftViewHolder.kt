package io.square1.limor.scenes.main.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.square1.limor.R
import io.square1.limor.uimodels.UIDraft

import org.jetbrains.anko.sdk23.listeners.onClick


class DraftViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.fragment_drafts_item, parent, false)) {

    private var tvDraftTitle: TextView? = null
    private var tvDraftCreated: TextView? = null

    init {
        tvDraftTitle = itemView.findViewById(R.id.tvDraftTitle)
        tvDraftCreated = itemView.findViewById(R.id.tvDraftCreated)
    }

    fun bind(
        uiDraft: UIDraft,
        secondaryInformationListener: DraftAdapter.OnSecondaryInformationListenerClickListener?,
        position: Int
    ) {

        //CENTRE TITLE
        if (!uiDraft.title.isNullOrEmpty())
            tvDraftTitle?.text = uiDraft.title
        else
            tvDraftTitle?.text = itemView.context.getString(R.string.draft_title)

        //CENTRE CREATED
        tvDraftCreated?.text = uiDraft.time.toString()


    }

}