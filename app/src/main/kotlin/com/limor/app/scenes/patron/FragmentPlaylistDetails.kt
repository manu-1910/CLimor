package com.limor.app.scenes.patron

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.R
import com.limor.app.databinding.FragmentPlaylistDetailsBinding
import com.limor.app.di.Injectable
import com.limor.app.extensions.loadImage

import com.limor.app.extensions.toLocalDateTime
import com.limor.app.extensions.visibleIf
import com.limor.app.playlists.PlaylistsViewModel
import com.limor.app.playlists.models.PlaylistCastUIModel

import com.limor.app.playlists.PlaylistCastsAdapter
import com.limor.app.playlists.SaveToPlaylistFragment
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.utils.FragmentCreatePlaylist

import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.PlaylistResult
import kotlinx.android.synthetic.main.fragment_my_earnings.*
import javax.inject.Inject

class FragmentPlaylistDetails : Fragment(), Injectable {

    companion object {
        const val KEY_PLAYLIST = "KEY_PLAYLIST"
        fun newInstance() = FragmentPlaylistDetails()
    }

    enum class SortOrder {
        ASC, DESC
    }

    enum class LayoutMode {
        SEARCH_MODE, NORMAL_MODE
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PlaylistsViewModel by viewModels { viewModelFactory }
    private lateinit var binding: FragmentPlaylistDetailsBinding

    private var playlistAdapter: PlaylistCastsAdapter? = null
    private var searchPlaylistAdapter: PlaylistCastsAdapter? = null
    private var sortOrder: SortOrder = SortOrder.DESC
    private var mode: LayoutMode = LayoutMode.NORMAL_MODE
    private var playlistCasts: List<PlaylistCastUIModel> = mutableListOf()

    private val playlist: PlaylistUIModel by lazy {
        requireArguments().getParcelable(KEY_PLAYLIST)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseViews()
        setAdapter()
        setClickListeners()
        setUpRecyclerView()
        setUpSearchRecyclerView()
        loadCasts()
    }

    private fun initialiseViews() {
        binding.loaderPB.visibility = View.VISIBLE
        binding.title.text = playlist.title
        binding.btnEditPlaylist.setImageDrawable(resources.getDrawable(R.drawable.ic_edit_small))

        binding.btnEditPlaylist.visibleIf(playlist.isCustom)

        loadPlaylistPreviewImage()
    }

    private fun setClickListeners() {
        binding.btnBack.setOnClickListener {
            if (mode == LayoutMode.NORMAL_MODE) {
                findNavController().navigateUp()
            } else {
                hideSearchLayout()
            }
        }
        binding.btnSearch.setOnClickListener {
            showSearchLayout()
        }
        binding.btnEditPlaylist.setOnClickListener {
            editPlaylist()
        }
    }

    private fun editPlaylist() {
        FragmentCreatePlaylist.editPlaylist(playlistId = playlist.id).also {
            it.onResult = { result ->  binding.title.text = result.title }
            it.show(parentFragmentManager, SaveToPlaylistFragment.TAG)
        }
    }

    private fun showSearchLayout() {
        mode = LayoutMode.SEARCH_MODE
        binding.searchBar.hideSearchIcon()
        binding.mainLayout.visibility = View.GONE
        binding.searchLayout.visibility = View.VISIBLE
        binding.btnSearch.visibility = View.GONE
        binding.btnEditPlaylist.visibility = View.GONE
        binding.searchBar.apply {
            setOnQueryTextListener(
                onQueryTextChange = {
                    performSearch(it)
                },
                onQueryTextSubmit = {
                    performSearch(it)
                },
                onQueryTextBlank = {
                    binding.searchBar.hideSearchIcon()
                    searchPlaylistAdapter?.clear()
                }
            )
            // Automatically open keyboard
            requestFocusOnText()
        }
    }

    private fun performSearch(query: String) {
        val results = mutableListOf<PlaylistCastUIModel>()
        playlistCasts.forEach { cast ->
            if (cast.title.contains(query, true) && query.trim() != "") {
                results.add(cast)
            }
        }
        if (results.size == 0) {
            binding.noResultsFound.visibility = View.VISIBLE
            binding.noResultsFoundDesc.visibility = View.VISIBLE
            binding.noResultsFoundDesc.text = getString(R.string.no_results_found_desc, query)
            binding.searchRecyclerView.visibility = View.GONE
        } else {
            binding.noResultsFound.visibility = View.GONE
            binding.noResultsFoundDesc.visibility = View.GONE
            binding.searchRecyclerView.visibility = View.VISIBLE
            searchPlaylistAdapter?.setData(results)
        }
    }

    private fun hideSearchLayout() {
        mode = LayoutMode.NORMAL_MODE
        binding.mainLayout.visibility = View.VISIBLE
        binding.searchLayout.visibility = View.GONE
        binding.btnSearch.visibility = View.VISIBLE
        binding.btnEditPlaylist.visibleIf(playlist.isCustom)
    }

    private fun playPodcast(podcast: PlaylistCastUIModel?, podcasts: List<PlaylistCastUIModel?>) {
        if (podcast != null) {
            val result = arrayListOf<PlaylistCastUIModel>()
            podcasts.forEach {
                it?.let {
                    result.add(it)
                }
            }
            (activity as? PlayerViewManager)?.showPlayer(
                PlayerViewManager.PlayerArgs(
                    PlayerViewManager.PlayerType.EXTENDED,
                    podcast.id,
                    result.map { it.id }
                )
            )
        }
    }

    private fun setUpRecyclerView() {
        playlistAdapter = PlaylistCastsAdapter(
            playlist.id,
            mutableListOf(),
            onPlayPodcast = { podcast, podcasts ->
                playPodcast(podcast, podcasts)
            },
            removeFromPlaylist = { playlistId, podcastId, positionInList ->
                LimorDialog(layoutInflater).apply {
                    setTitle(R.string.label_remove_from_playlist)
                    setMessage(R.string.label_remove_from_playlist_description)
                    setIcon(R.drawable.ic_delete_cast)
                    addButton(R.string.yes_title, false) {
                        removeFromPlaylist(playlistId, podcastId, positionInList)
                        dismiss()
                    }
                    addButton(R.string.btn_cancel, true)
                }.show()
            }
        )
        val layoutManager = LinearLayoutManager(requireContext())
        binding.castRecyclerView.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        binding.castRecyclerView.addItemDecoration(MarginItemDecoration(itemMargin))
        binding.castRecyclerView.adapter = playlistAdapter
    }

    private fun setUpSearchRecyclerView() {
        searchPlaylistAdapter = PlaylistCastsAdapter(
            playlist.id,
            mutableListOf(),
            onPlayPodcast = { podcast, podcasts ->
                playPodcast(podcast, podcasts)
            },
            removeFromPlaylist = { playlistId, podcastId, positionInList ->
                LimorDialog(layoutInflater).apply {
                    setTitle(R.string.label_remove_from_playlist)
                    setMessage(R.string.label_remove_from_playlist_description)
                    setMessageColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.error_stroke_color
                        )
                    )
                    setIcon(R.drawable.ic_delete_cast)
                    addButton(R.string.yes_title, false) {
                        removeFromPlaylist(playlistId, podcastId, positionInList)
                        dismiss()
                    }
                    addButton(R.string.btn_cancel, true)
                }.show()
            }
        )
        val layoutManager = LinearLayoutManager(requireContext())
        binding.searchRecyclerView.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        binding.searchRecyclerView.addItemDecoration(MarginItemDecoration(itemMargin))
        binding.searchRecyclerView.adapter = searchPlaylistAdapter
    }

    private fun removeFromPlaylist(playlistId: Int, podcastId: Int, positionInList: Int){
        model.deleteCastInPlaylist(playlistId, podcastId).observe(viewLifecycleOwner, {
            if(it.success){
                loadCasts()
            }
        })
    }

    private fun loadCasts() {
        model.getCastsInPlaylist(playlist.id).observe(viewLifecycleOwner, {
            binding.loaderPB.visibility = View.GONE
            binding.mainLayout.visibility = View.VISIBLE
            mode = LayoutMode.NORMAL_MODE
            playlistCasts = it
            binding.castCountTextView.text = getString(R.string.label_cast_count, it?.size)
            loadPlaylist(it)
        })
    }

    private fun setAdapter() {
        val items = mutableListOf("Newest to Oldest", "Oldest to Newest")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_phone_code_country_code, items)
        val editText = etFilter.editText as AutoCompleteTextView
        editText.setAdapter(adapter)
        editText.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.selectedFilterTextView.text = adapter.getItem(position)
                sortOrder = if (position == 0) {
                    SortOrder.DESC
                } else {
                    SortOrder.ASC
                }
                loadPlaylist(playlistCasts)
            }
    }

    private fun loadPlaylist(playlist: List<PlaylistCastUIModel>) {
        if(playlist.isNotEmpty()){
            val list = if (sortOrder == SortOrder.ASC) {
                playlist.sortedBy { it.addedAt.toLocalDateTime() }
            } else {
                playlist.sortedByDescending { it.addedAt.toLocalDateTime() }
            }
            playlistAdapter?.setData(list)
            val recyclerViewState =
                binding.castRecyclerView.layoutManager?.onSaveInstanceState()
            binding.castRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
        } else{
            binding.castRecyclerView.removeAllViews()
            binding.searchRecyclerView.removeAllViews()
            binding.mainLayout.visibility = View.GONE
            binding.searchLayout.visibility = View.GONE
            playlistCasts = mutableListOf()
        }
    }

    private fun loadPlaylistPreviewImage() {
        val url = playlist.images?.largeUrl?.takeIf { it.isNotEmpty() }

        if (url == null) {
            binding.playlistPreviewImage.setImageResource(R.drawable.ic_transparent_image)

            val hexColor = playlist.colorCode ?: PlaylistUIModel.defaultColorString
            val color = Color.parseColor(hexColor)
            binding.playlistPreviewImage.setBackgroundColor(color)
        } else {
            binding.playlistPreviewImage.loadImage(playlist.images?.largeUrl!!)
        }
    }

}