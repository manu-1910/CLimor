package com.limor.app.scenes.patron.manage.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentCastEarningsBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.main_new.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.patron.manage.adapters.CastBuyersAdapter
import com.limor.app.scenes.patron.manage.adapters.CastEarningsAdapter
import com.limor.app.scenes.patron.manage.viewmodels.ManagePatronViewModel
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.uimodels.CastUIModel
import kotlinx.android.synthetic.main.fragment_home_new.*
import javax.inject.Inject

class FragmentCastEarnings : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: ManagePatronViewModel by activityViewModels { viewModelFactory }
    private lateinit var binding: FragmentCastEarningsBinding

    private var castBuyersAdapter: CastBuyersAdapter? = null
    private var offset = 0
    private val castEarningsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCastEarningsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseViews()
        setClickListeners()
        setUpRecyclerView()
        subscribeViewModels()
        loadBuyers()
    }

    private fun initialiseViews() {
        binding.toolbar.title.text = resources.getString(R.string.cast_earnings)
        binding.toolbar.btnNotification.visibility = View.GONE
        binding.castNameTextview.text = "Cast Name"
        binding.castDurationTextview.text = "5m 30s"
    }

    private fun setClickListeners() {
        binding.toolbar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.castPlayButton.setOnClickListener {
            openPlayer(17112)
        }
    }

    private fun setUpRecyclerView() {
        castBuyersAdapter = CastBuyersAdapter(
            onLoadMore = {
                offset = castEarningsList.size
                castBuyersAdapter?.isLoading = true
                loadBuyers()
            })
        val layoutManager = LinearLayoutManager(requireContext())
        binding.castBuyersRecyclerView.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        binding.castBuyersRecyclerView.addItemDecoration(MarginItemDecoration(itemMargin))
        binding.castBuyersRecyclerView.adapter = castBuyersAdapter
    }

    private fun loadBuyers() {
        model.loadCastBuyers(
            offset = offset,
            limit = 10
        )
    }

    private fun subscribeViewModels() {
        model.buyersData.observe(viewLifecycleOwner) {
            onLoadCastEarnings(it)
        }
    }

    private fun onLoadCastEarnings(earnings: List<String>) {
        castEarningsList.addAll(earnings)

        val all = mutableListOf<String>()
        all.addAll(castEarningsList)

        val recyclerViewState = binding.castBuyersRecyclerView.layoutManager?.onSaveInstanceState()
        castBuyersAdapter?.apply {
            loadMore =
                earnings.size >= 10 &&
                        earnings.size >= 10
            submitList(all)
            isLoading = false
        }
        binding.castBuyersRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun openPlayer(/*cast: CastUIModel*/id: Int) {
        (activity as? PlayerViewManager)?.showPlayer(
            PlayerViewManager.PlayerArgs(
                PlayerViewManager.PlayerType.EXTENDED,
                id
            )
        )
    }

}