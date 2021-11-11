package com.limor.app.scenes.patron.manage.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.R
import com.limor.app.databinding.FragmentMyEarningsBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.patron.manage.adapters.CastBuyersAdapter
import com.limor.app.scenes.patron.manage.adapters.CastEarningsAdapter
import com.limor.app.scenes.patron.manage.viewmodels.ManagePatronViewModel
import kotlinx.android.synthetic.main.fragment_home_new.*
import kotlinx.android.synthetic.main.fragment_my_earnings.*
import javax.inject.Inject

class FragmentMyEarnings : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: ManagePatronViewModel by activityViewModels{ viewModelFactory }
    private lateinit var binding : FragmentMyEarningsBinding

    private var castEarningsAdapter: CastEarningsAdapter? = null
    private var offset = 0
    private val castEarningsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyEarningsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseViews()
        setClickListeners()
        setUpRecyclerView()
        setAdapter()
        subscribeViewModels()
        loadEarnings()
    }

    private fun initialiseViews(){
        binding.toolbar.title.text = resources.getString(R.string.my_earnings)

        binding.totalEarningsLayout.amountTextView.text = "$100"
        binding.totalEarningsLayout.earningsLayout.setCardBackgroundColor(resources.getColor(R.color.total_earnings_background))
        binding.totalEarningsLayout.creditCardImageView.setColorFilter(resources.getColor(R.color.waveFormColor))
        binding.totalEarningsLayout.titleImageView.text = getString(R.string.total_earnings)

        binding.currentBalanceLayout.amountTextView.text = "$100"
        binding.currentBalanceLayout.earningsLayout.setCardBackgroundColor(resources.getColor(R.color.total_balance_background))
        binding.currentBalanceLayout.creditCardImageView.setColorFilter(resources.getColor(R.color.approved_green))
        binding.currentBalanceLayout.titleImageView.text = getString(R.string.transaction_fee)

        binding.transactionFeeLayout.amountTextView.text = "$100"
        binding.transactionFeeLayout.earningsLayout.setCardBackgroundColor(resources.getColor(R.color.transaction_fee_background))
        binding.transactionFeeLayout.creditCardImageView.setColorFilter(resources.getColor(R.color.error_stroke_color))
        binding.transactionFeeLayout.titleImageView.text = "Current Balance"

        binding.totalEarningsLayout.amountTextView.text = "$300"
        binding.toolbar.btnNotification.visibility = View.GONE
    }

    private fun setClickListeners(){
        binding.toolbar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setUpRecyclerView(){
        castEarningsAdapter = CastEarningsAdapter (
            onLoadMore = {
                offset = castEarningsList.size
                castEarningsAdapter?.isLoading = true
                loadEarnings()
            },
            onClick = {
                findNavController().navigate(R.id.action_my_earnings_fragment_to_fragment_cast_earnings)
            }
        )
        val layoutManager = LinearLayoutManager(requireContext())
        binding.castEarningsRecyclerView.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        binding.castEarningsRecyclerView.addItemDecoration(MarginItemDecoration(itemMargin))
        binding.castEarningsRecyclerView.adapter = castEarningsAdapter
    }

    private fun loadEarnings() {
        model.loadCastEarnings(
            offset = offset,
            limit = 10
        )
    }

    private fun setAdapter() {
        val items = mutableListOf("Last 2 months", "Last month", "Last week")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_phone_code_country_code, items)
        val editText = etFilter.editText as AutoCompleteTextView
        editText.setAdapter(adapter)
        editText.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                editText.setText(adapter.getItem(position), false)
            }
    }

    private fun subscribeViewModels(){
        model.earningsData.observe(viewLifecycleOwner) {
            onLoadBuyers(it)
        }
    }

    private fun onLoadBuyers(buyers: List<String>){
        castEarningsList.addAll(buyers)

        val all = mutableListOf<String>()
        all.addAll(castEarningsList)


        val recyclerViewState = binding.castEarningsRecyclerView.layoutManager?.onSaveInstanceState()
        castEarningsAdapter?.apply {
            loadMore =
                buyers.size >= 10 &&
                        buyers.size >= 10
            submitList(all)
            isLoading = false
        }
        binding.castEarningsRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

}