package com.limor.app.playlists

import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.databinding.ItemDeletePlaylistBinding
import com.limor.app.extensions.loadImage
import com.limor.app.extensions.precisePx
import com.limor.app.extensions.px
import com.limor.app.uimodels.CastUIModel
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.wrapContent

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

        val menuBinding = ItemDeletePlaylistBinding.inflate(holder.details.context.layoutInflater, null, false)
        menuBinding.textDelete.text = holder.details.context.getText(R.string.label_remove_from_playlist)
        val popupWindow = PopupWindow(
            menuBinding.root,
            wrapContent,
            48.px,
            true
        ).apply {
            contentView.setOnClickListener { dismiss() }
            elevation = 10.precisePx
            setBackgroundDrawable(AppCompatResources.getDrawable(holder.details.context, R.drawable.popup_menu_background))
        }

        menuBinding.root.setOnClickListener {
            removeFromPlaylist()
            popupWindow.dismiss()
        }

        holder.optionsIV.setOnClickListener {
            popupWindow.showAsDropDown(
                it,
                (-8).px,
                (-2).px,
                Gravity.BOTTOM or Gravity.RIGHT,
            )
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