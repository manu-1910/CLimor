package com.limor.app.playlists.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.databinding.LayoutSelectPlaylistBinding
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable

class SelectPlaylistAdapter(
    private val onPlaylistSelected: (playlistId: Int, selected: Boolean) -> Unit
) : ListAdapter<PlaylistUIModel, RecyclerView.ViewHolder>(
    PlaylistsDiffCallback()
) {

    override fun getItemCount(): Int {
        val superCount = super.getItemCount()
        if (superCount == 0) {
            return 0
        }

        return superCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutSelectPlaylistBinding.inflate(inflater, parent, false)
        return ViewHolderSelectPlaylist(binding, onPlaylistSelected = onPlaylistSelected)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolderSelectPlaylist).bind(getItem(position))
    }

}

class ViewHolderSelectPlaylist(
    val binding: LayoutSelectPlaylistBinding,
    val onPlaylistSelected: (playlistId: Int, selected: Boolean) -> Unit
) : ViewHolderBindable<PlaylistUIModel>(binding) {
    private var playlistModel: PlaylistUIModel? = null

    override fun bind(item: PlaylistUIModel) {
        playlistModel = item
        initialise()
    }

    private fun initialise() {
        val playlist = playlistModel ?: return
        binding.playlistNameTextView.text = playlist.title
        if(playlist.isAdded){
            binding.selectContactCheckbox.setImageDrawable(binding.root.context.getDrawable(R.drawable.ic_selected_checkbox))
        } else{
            binding.selectContactCheckbox.setImageDrawable(binding.root.context.getDrawable(R.drawable.ic_unselected_checkbox))
        }
        //onPlaylistSelected(playlist.id, playlist.isAdded)
        binding.root.setOnClickListener {
            if(playlist.isAdded){
                binding.selectContactCheckbox.setImageDrawable(binding.root.context.getDrawable(R.drawable.ic_unselected_checkbox))
            } else{
                binding.selectContactCheckbox.setImageDrawable(binding.root.context.getDrawable(R.drawable.ic_selected_checkbox))
            }
            playlist.isAdded = !playlist.isAdded
            onPlaylistSelected(playlist.id, playlist.isAdded)
        }
    }

}