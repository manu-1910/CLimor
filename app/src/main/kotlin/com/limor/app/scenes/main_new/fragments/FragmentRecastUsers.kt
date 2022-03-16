package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentRecastUsersBinding
import com.limor.app.playlists.models.PlaylistCastUIModel
import com.limor.app.scenes.main_new.adapters.RecastUsersAdapter
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.uimodels.UserUIModel
import javax.inject.Inject

class FragmentRecastUsers : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: HomeFeedViewModel by viewModels { viewModelFactory }

    lateinit var binding: FragmentRecastUsersBinding

    private var usersAdapter: RecastUsersAdapter? = null

    private var recastUsers: List<UserUIModel> = mutableListOf()

    private val podcastId: Int by lazy {
        requireArguments().getInt(
            PODCAST_ID_KEY
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecastUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.getRecastedUsers(podcastId)
        initialise()
        createAdapter()
        setUpRecyclerView()
        setOnClicks()
        subscribeToViewModel()
    }

    private fun initialise(){
        binding.toolbar.btnNotification.visibility = View.GONE
        binding.toolbar.title.text = resources.getText(R.string.recasts)
    }

    private fun createAdapter() {
        usersAdapter = RecastUsersAdapter(
            arrayListOf(),
            onFollowClick = { account, follow ->
                model.followUser(account, follow)
            }
        )
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginTwenty).toInt()
        binding.rvUsers.addItemDecoration(MarginItemDecoration(itemMargin))
        binding.rvUsers.adapter = usersAdapter
    }

    private fun setOnClicks() {
        binding.toolbar.btnBack.setOnClickListener {
            it?.findNavController()?.navigateUp()
        }
        binding.searchBar.apply {
            setOnQueryTextListener(
                onQueryTextChange = {
                    performSearch(it)
                },
                onQueryTextSubmit = {
                    performSearch(it)
                },
                onQueryTextBlank = {
                    performSearch("")
                }
            )
            // Automatically open keyboard
            requestFocusOnText()
        }
    }

    private fun subscribeToViewModel() {
        model.getRecastedUsers(podcastId).observe(viewLifecycleOwner, {
            recastUsers = it
            usersAdapter?.setData(it)
        })
    }

    private fun performSearch(query: String) {
        val results = mutableListOf<UserUIModel>()
        recastUsers.forEach { user ->
            if (user.getFullName()?.contains(query, true) == true && query.trim() != "") {
                results.add(user)
            }
        }
        if(query.isEmpty()){
            usersAdapter?.setData(recastUsers)
        } else{
            usersAdapter?.setData(results)
        }
    }

    companion object {
        const val PODCAST_ID_KEY = "PODCAST_ID"
    }

}