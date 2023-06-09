package com.limor.app.playlists

import android.graphics.Color
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
import com.limor.app.playlists.models.PlaylistCastUIModel
import com.limor.app.scenes.utils.CommonsKt
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.layoutInflater
import java.time.Duration

class PlaylistCastsAdapter(
    private var playlistId: Int,
    private var casts: List<PlaylistCastUIModel?>,
    private val removeFromPlaylist: (playlistId: Int, podcastId: Int, positionInList: Int) -> Unit,
    private val onPlayPodcast: (podcast: PlaylistCastUIModel?, podcasts: List<PlaylistCastUIModel?>) -> Unit,
    private val resultType: PlaylistResultType
) : RecyclerView.Adapter<PlaylistCastsAdapter.ViewHolder>() {

    enum class PlaylistResultType{
        SEARCH_RESULT,
        NORMAL_RESULT,
        NOT_A_PLAYLIST
    }

    var playlistResultType: PlaylistResultType = resultType

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistCastsAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.layout_playlist_cast, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlaylistCastsAdapter.ViewHolder, position: Int) {
        val podcast = casts[position]

        if (podcast?.images?.mediumUrl != null) {
            holder.image.loadImage(podcast.images.mediumUrl)
        } else {
            holder.image.setImageResource(R.drawable.ic_transparent_image)
            holder.image.circleBackgroundColor = Color.parseColor(podcast?.colorCode)
        }

        holder.itemView.setOnClickListener {
            onPlayPodcast(podcast, casts)
        }
        podcast?.images?.mediumUrl?.let {
            holder.image.loadImage(it)
        }
        holder.name.text = podcast?.title
        holder.userName.text = podcast?.userName
        holder.duration.text = CommonsKt.getFeedDuration(Duration.ofMillis(podcast?.totalLength?.toLong() ?: 0))

        holder.optionsIV.visibility = if(resultType == PlaylistResultType.SEARCH_RESULT || resultType == PlaylistResultType.NOT_A_PLAYLIST) View.GONE else View.VISIBLE

        val menuBinding =
            ItemDeletePlaylistBinding.inflate(holder.userName.context.layoutInflater, null, false)
        menuBinding.textDelete.text =
            holder.userName.context.getText(R.string.label_remove_from_playlist)
        val popupWindow = PopupWindow(
            menuBinding.root,
            200.px,
            48.px,
            true
        ).apply {
            contentView.setOnClickListener {
                dismiss()
            }
            elevation = 10.precisePx
            setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    holder.userName.context,
                    R.drawable.popup_menu_background
                )
            )
        }

        menuBinding.root.setOnClickListener {
            removeFromPlaylist(playlistId, podcast?.id ?: -1, position)
            popupWindow.dismiss()
        }

        holder.optionsIV.setOnClickListener {
            popupWindow.showAsDropDown(
                it,
                (-8).px,
                (-2).px,
                Gravity.BOTTOM or Gravity.END,
            )
        }
    }

    override fun getItemCount(): Int {
        return casts.size
    }

    public fun setData(casts: List<PlaylistCastUIModel?>) {
        this.casts = casts
        notifyDataSetChanged()
    }

    fun clear() {
        this.casts = mutableListOf()
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById(R.id.cast_image) as CircleImageView
        val name = view.findViewById(R.id.cast_name) as TextView
        val userName = view.findViewById<View>(R.id.user_name_text_view) as TextView
        val duration = view.findViewById<View>(R.id.duration_text_view) as TextView
        val optionsIV = view.findViewById<ImageView>(R.id.options_image_view) as ImageView
    }

}