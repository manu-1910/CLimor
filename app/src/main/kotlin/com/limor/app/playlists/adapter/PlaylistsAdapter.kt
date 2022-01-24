package com.limor.app.playlists.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.ObjectKey
import com.limor.app.R
import com.limor.app.databinding.*
import com.limor.app.extensions.*
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.uimodels.CastUIModel
import org.jetbrains.anko.layoutInflater

class PlaylistsAdapter(
    private val onPlaylistClick: (playlist: PlaylistUIModel) -> Unit,
    private val onDeleteClick: (playlist: PlaylistUIModel) -> Unit
) : ListAdapter<PlaylistUIModel, RecyclerView.ViewHolder>(
    PlaylistsDiffCallback()
) {

    override fun getItemCount(): Int {
        val superCount = super.getItemCount()
        if (superCount == 0) {
            return 0
        }

        val sectionHeaderCount = 1
        val placeholderCount = 1
        return superCount + sectionHeaderCount + if (superCount == 2) placeholderCount else 0
    }

    private fun getAdjustedPosition(position: Int): Int {
        if (position > 2) {
            // -1 for the section header
            return position - 1
        } else {
            return position
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 2) {
            return ITEM_TYPE_SECTION_HEADER
        } else if (position == 3 && super.getItemCount() == 2) {
            return ITEM_TYPE_NO_CUSTOM_PLACEHOLDER
        }
        return ITEM_TYPE_PLAYLIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        if (viewType == ITEM_TYPE_SECTION_HEADER) {
            return ViewHolderSectionHeader(ItemSectionHeaderBinding.inflate(inflater, parent, false))
        } else if (viewType == ITEM_TYPE_NO_CUSTOM_PLACEHOLDER) {
            return ViewHolderPlaceholder(ItemNoPlaylistPlaceholderBinding.inflate(inflater, parent, false))
        }

        val binding = ItemPlaylistBinding.inflate(inflater, parent, false)
        return ViewHolderPlaylist(binding, onPlaylistClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // The section header and placeholder are static and don't change their state
        if (getItemViewType(position) != ITEM_TYPE_PLAYLIST) {
            return
        }

        (holder as ViewHolderPlaylist).bind(getItem(getAdjustedPosition(position)))
    }

    companion object {
        private const val ITEM_TYPE_PLAYLIST = 1
        private const val ITEM_TYPE_SECTION_HEADER = 2
        private const val ITEM_TYPE_NO_CUSTOM_PLACEHOLDER = 3
    }
}

class PlaylistsDiffCallback : DiffUtil.ItemCallback<PlaylistUIModel>() {
    override fun areItemsTheSame(
        oldItem: PlaylistUIModel,
        newItem: PlaylistUIModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: PlaylistUIModel,
        newItem: PlaylistUIModel
    ): Boolean {
        return oldItem == newItem
    }
}

class ViewHolderPlaylist(
    val binding: ItemPlaylistBinding,
    private val onPlaylistClick: (playlist: PlaylistUIModel) -> Unit,
    private val onDeleteClick: (playlist: PlaylistUIModel) -> Unit
) : ViewHolderBindable<PlaylistUIModel>(binding) {
    private var playlistModel: PlaylistUIModel? = null

    override fun bind(item: PlaylistUIModel) {
        playlistModel = item
        setData()
        setListeners()
    }

    private fun setBackgroundColor(color: Int) {
        Glide.with(binding.imagePlaylist)
            .load(ColorDrawable(color))
            .signature(ObjectKey(color))
            .override(48.px, 48.px)
            .transform(RoundedCorners(8.px))
            .into(binding.imagePlaylist)
    }

    private fun setData() {
        val playlist = playlistModel ?: return
        binding.imageMore.visibleIf(playlist.isCustom)
        binding.textPlaylistTitle.text = playlist.title

        val count = context.resources.getQuantityString(R.plurals.casts_count, playlist.count,  playlist.count)
        binding.textPlaylistPodcastCount.text = count

        val url = playlist.images?.smallUrl

        when {
            playlist.count == 0 -> {
                setBackgroundColor(PlaylistUIModel.defaultColorInt)

            }
            !url.isNullOrEmpty() -> {
                Glide.with(binding.imagePlaylist)
                    .load(url)
                    .signature(ObjectKey(url))
                    .centerCrop()
                    .override(48.px, 48.px)
                    .transform(RoundedCorners(8.px))
                    .into(binding.imagePlaylist)

            }
            !playlist.colorCode.isNullOrEmpty() -> {
                setBackgroundColor(Color.parseColor(playlist.colorCode))
            }
            else -> {
                setBackgroundColor(PlaylistUIModel.defaultColorInt)
            }
        }
    }

    private fun showDeleteWarning() {
        LimorDialog(context.layoutInflater).apply {
            // UI
            setTitle(R.string.delete_playlist_title)
            setMessage(R.string.delete_playlist_message)
            setMessageColor(ContextCompat.getColor(context, R.color.error_stroke_color))
            setIcon(R.drawable.ic_delete_cast)

            // Actions
            addButton(R.string.dialog_yes_button, false) {
                playlistModel?.let { onDeleteClick(it) }
            }
            addButton(R.string.cancel, true)
        }.show()
    }

    private fun setListeners() {
        val menuBinding = ItemDeletePlaylistBinding.inflate(context.layoutInflater, null, false)
        val popupWindow = PopupWindow(
            menuBinding.root,
            164.px,
            48.px,
            true
        ).apply {
            contentView.setOnClickListener { dismiss() }
            elevation = 10.precisePx
            setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.popup_menu_background))
        }

        menuBinding.root.setOnClickListener {
            showDeleteWarning()
            popupWindow.dismiss()
        }

        binding.imageMore.setOnClickListener {
            popupWindow.showAsDropDown(
                it,
                (-8).px,
                (-2).px,
                Gravity.BOTTOM or Gravity.RIGHT,
            )
        }
        itemView.setOnClickListener {
            playlistModel?.let { onPlaylistClick(it) }
        }
    }

}


class ViewHolderSectionHeader(
    val binding: ItemSectionHeaderBinding,
) : ViewHolderBindable<CastUIModel>(binding) {

    override fun bind(item: CastUIModel) {
    }
}

class ViewHolderPlaceholder(
    val binding: ItemNoPlaylistPlaceholderBinding,
) : ViewHolderBindable<CastUIModel>(binding) {

    override fun bind(item: CastUIModel) {
    }
}

