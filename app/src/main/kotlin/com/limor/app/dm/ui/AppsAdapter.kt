package com.limor.app.dm.ui

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import org.jetbrains.anko.image
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent

import android.content.ComponentName
import android.content.Context
import com.limor.app.uimodels.CastUIModel
import org.greenrobot.eventbus.EventBus


class AppsAdapter(
    private val context: Context,
    private val apps: List<ResolveInfo>,
    private val onTap: (ri: ResolveInfo) -> Unit
) :
    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById(R.id.imageAppIcon) as ImageView
        val name = view.findViewById(R.id.textAppTitle) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_external_share_app, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ri = apps[position]

        context.packageManager.let { pm ->
            holder.name.text = ri.loadLabel(pm)
            holder.image.image = ri.loadIcon(pm)
        }
        holder.itemView.setOnClickListener {
            onTap(ri)
        }
    }

    override fun getItemCount() = apps.size
}