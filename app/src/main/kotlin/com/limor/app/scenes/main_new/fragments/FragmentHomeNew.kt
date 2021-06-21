package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.FeedItemsQuery
import com.limor.app.R
import com.limor.app.databinding.FragmentHomeNewBinding
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.main_new.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import kotlinx.android.synthetic.main.fragment_home_new.*

class FragmentHomeNew : FragmentWithLoading() {

    val model: HomeFeedViewModel by viewModels()
    lateinit var binding: FragmentHomeNewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun load() {
        model.loadHomeFeed()
    }

    override val errorLiveData: LiveData<String>
        get() = model.homeFeedErrorLiveData

    override fun subscribeToViewModel() {
        super.subscribeToViewModel()
        model.homeFeedLiveData.observe(viewLifecycleOwner, {
            it?.let{
                switchCommonVisibility(isLoading = false)
                setDataToRecyclerView(it)
            }
        })
    }

    private fun setDataToRecyclerView(list: List<FeedItemsQuery.FeedItem>) {
        val adapter = binding.rvHome.adapter
        if(adapter != null) {
            (adapter as HomeFeedAdapter).addData(list)
        }else {
            setUpRecyclerView(list)
        }
    }

    private fun setUpRecyclerView(list: List<FeedItemsQuery.FeedItem>) {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvHome.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        binding.rvHome.addItemDecoration(MarginItemDecoration(itemMargin))
        rvHome.adapter = HomeFeedAdapter(list.toMutableList())
    }
}