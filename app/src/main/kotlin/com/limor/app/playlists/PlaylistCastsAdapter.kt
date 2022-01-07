package com.limor.app.playlists

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.extensions.loadImage
import com.limor.app.uimodels.CastUIModel

class PlaylistCastsAdapter(
    private var casts: List<CastUIModel>,
    private val removeFromPlaylist: () -> Unit
): RecyclerView.Adapter<PlaylistCastsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistCastsAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.layout_playlist_cast, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlaylistCastsAdapter.ViewHolder, position: Int) {
        casts[position].imageLinks?.medium?.let {
            holder.image.loadImage(it)
        }
        holder.name.text = casts[position].title
        holder.details.text = casts[position].createdAt.toString()
        holder.optionsIV.setOnClickListener {
            val popup = PopupMenu(holder.image.context, holder.optionsIV)
            popup.inflate(R.menu.play_list_menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item?.itemId) {
                        R.id.remove ->{
                            removeFromPlaylist.invoke()
                            true
                        }
                        else -> false
                    }
                }

            })
            popup.show()
        }
    }

    override fun getItemCount(): Int {
        return casts.size
    }

    public fun setData(casts: List<CastUIModel>) {
        this.casts = casts
        notifyDataSetChanged()
    }

    fun clear(){
        this.casts = mutableListOf()
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById(R.id.cast_image) as ImageView
        val name = view.findViewById(R.id.cast_name) as TextView
        val details = view.findViewById<View>(R.id.detail_text_view) as TextView
        val optionsIV = view.findViewById<ImageView>(R.id.options_image_view) as ImageView
    }

}