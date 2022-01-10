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
import com.limor.app.extensions.makeGone
import com.limor.app.playlists.PlaylistCastsAdapter
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.scenes.utils.LimorTextInputDialog
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.uimodels.CastUIModel
import kotlinx.android.synthetic.main.fragment_my_earnings.*
import javax.inject.Inject

class FragmentPlaylistDetails : Fragment(), Injectable {

    companion object {
        const val IS_PLAYLIST = "IS_PLAYLIST"
        const val LIST_NAME = "LIST_NAME"
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
    private val model: HomeFeedViewModel by viewModels { viewModelFactory }
    private lateinit var binding: FragmentPlaylistDetailsBinding

    private var playlistAdapter: PlaylistCastsAdapter? = null
    private var searchPlaylistAdapter: PlaylistCastsAdapter? = null
    private var sortOrder: SortOrder = SortOrder.DESC
    private var mode: LayoutMode = LayoutMode.NORMAL_MODE
    private var playList: List<CastUIModel> = mutableListOf()

    private val isPlayList: Boolean by lazy { requireArguments().getBoolean(IS_PLAYLIST, false) }
    private val playListName: String by lazy { requireArguments().getString(LIST_NAME, "") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        subscribeViewModels()
        loadEarnings()
    }

    private fun initialiseViews() {
        binding.loaderPB.visibility = View.VISIBLE
        binding.title.text = playListName
        binding.btnEditPlaylist.setImageDrawable(resources.getDrawable(R.drawable.ic_edit_small))
        if (!isPlayList) {
            binding.btnEditPlaylist.visibility = View.GONE
        }
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
            LimorTextInputDialog(layoutInflater).apply {
                setTitle(R.string.label_rename_playlist)
                setHint(R.string.label_rename_playlist)
                setCharacterMaxLength(50)
                addButton(R.string.cancel, false)
                addButton(R.string.btn_save, true) {
                }
            }.show()
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
        val results = mutableListOf<CastUIModel>()
        playList.forEach { cast ->
            if (cast.title?.contains(query, true) == true && query.trim() != "") {
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
        binding.btnEditPlaylist.visibility = View.VISIBLE
    }

    private fun playPodcast(podcast: CastUIModel, podcasts: List<CastUIModel>) {
        (activity as? PlayerViewManager)?.showPlayer(
            PlayerViewManager.PlayerArgs(
                PlayerViewManager.PlayerType.EXTENDED,
                podcast.id,
                podcasts.map { it.id }
            )
        )
    }

    private fun setUpRecyclerView() {
        playlistAdapter = PlaylistCastsAdapter(
            mutableListOf(),
            onPlayPodcast = { podcast, podcasts ->
                playPodcast(podcast, podcasts)
            },
            removeFromPlaylist = {
                LimorDialog(layoutInflater).apply {
                    setTitle(R.string.label_remove_from_playlist)
                    setMessage(R.string.label_remove_from_playlist_description)
                    setIcon(R.drawable.ic_delete_cast)
                    addButton(R.string.yes_title, false) { dismiss() }
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
            mutableListOf(),
            onPlayPodcast = { podcast, podcasts ->
                playPodcast(podcast, podcasts)
            },
            removeFromPlaylist = {
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
                    addButton(R.string.yes_title, false) { dismiss() }
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

    private fun loadEarnings() {
        model.loadHomeFeed(
            offset = 0,
            limit = 40
        )
    }

    private fun setAdapter() {
        val items = mutableListOf("Newest to Oldest", "Oldest to Newest")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_phone_code_country_code, items)
        val editText = etFilter.editText as AutoCompleteTextView
        editText.setAdapter(adapter)
        editText.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.selectedFilterTextView.text = adapter.getItem(position)
                if (position == 0) {
                    sortOrder = SortOrder.DESC
                } else {
                    sortOrder = SortOrder.ASC
                }
                loadPlaylist(playList)
            }
    }

    private fun subscribeViewModels() {
        model.homeFeedData.observe(viewLifecycleOwner) {
            binding.loaderPB.visibility = View.GONE
            binding.mainLayout.visibility = View.VISIBLE
            mode = LayoutMode.NORMAL_MODE
            val podcasts = it.filter { it.recasted != true }
            playList = podcasts
            binding.castCountTextView.text = getString(R.string.label_cast_count, podcasts.size)
            loadPlaylist(podcasts)
        }
    }

    private fun loadPlaylist(playlist: List<CastUIModel>) {
        val list = if (sortOrder == SortOrder.ASC) {
            playlist.sortedBy { it.createdAt }
        } else {
            playlist.sortedByDescending { it.createdAt }
        }
        if (mode == LayoutMode.NORMAL_MODE) {
            loadPlaylistPreviewImage(list[0])
        }
        playlistAdapter?.setData(list)
        val recyclerViewState =
            binding.castRecyclerView.layoutManager?.onSaveInstanceState()
        binding.castRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun loadPlaylistPreviewImage(playlist: CastUIModel) {
        if (playlist.imageLinks?.large != null) {
            binding.playlistPreviewImage.loadImage(playlist.imageLinks.large)
        } else {
            binding.playlistPreviewImage.setImageResource(R.drawable.ic_transparent_image)
            binding.playlistPreviewImage.setBackgroundColor(Color.parseColor(playlist.colorCode))
        }
    }

}