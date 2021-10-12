package com.limor.app.dm.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import android.content.Context
import com.limor.app.dm.ChatTarget
import com.limor.app.extensions.loadCircleImage

class TargetsAdapter(
    private val context: Context,
    private var targets: List<ChatTarget>,
    private val onTap: (target: ChatTarget) -> Unit
) :
    RecyclerView.Adapter<TargetsAdapter.ViewHolder>() {

    private var term: String = ""

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById(R.id.person_image) as ImageView
        val name = view.findViewById(R.id.full_name) as TextView
        val userName = view.findViewById(R.id.description) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_chat_search_target, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val target = targets[position]

        holder.name.text = target.limorDisplayName
        holder.userName.text = "@${target.limorUserName}"
        holder.image.loadCircleImage(target.limorProfileUrl)

        holder.itemView.setOnClickListener {
            onTap(target)
        }
    }

    override fun getItemCount() = targets.size

    fun setChatTargets(chatTargets: List<ChatTarget>) {
        // TODO diff util
        targets = chatTargets
    }

    fun setTerm(searchTerm: String) {
        term = searchTerm
    }
}