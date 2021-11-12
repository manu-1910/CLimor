package com.limor.app.scenes.patron.manage.fragment

import android.R
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.limor.app.databinding.FragmentChangePriceBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.patron.manage.viewmodels.ManagePatronViewModel
import kotlinx.android.synthetic.main.fragment_change_price.*
import javax.inject.Inject

class FragmentChangePrice : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: ManagePatronViewModel by activityViewModels { viewModelFactory }
    private lateinit var binding: FragmentChangePriceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClicks()
        setPrices()
        subscribeViewModel()
        initialiseViews()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    private fun setOnClicks() {
        binding.yesButton.setOnClickListener {
            findNavController().navigate(com.limor.app.R.id.action_dialog_change_price_to_dialog_change_price_confirmation)
        }
        binding.toolbar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setPrices() {
        val editText = priceTIL.editText as AutoCompleteTextView

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(
                requireContext(),
                R.layout.select_dialog_item,
                listOf("2.99", "3.99", "4.99", "5.99")
            )
        editText.setAdapter(adapter)
    }

    private fun subscribeViewModel() {
        model.priceChangeResult.observe(viewLifecycleOwner, {
            Handler().postDelayed(Runnable() {
                if (it == true) {
                    findNavController().navigateUp()
                }
            }, 1000)
        })
    }

    private fun initialiseViews() {
        binding.toolbar.title.text = getString(com.limor.app.R.string.edit_price)
        binding.toolbar.btnNotification.setImageDrawable(resources.getDrawable(R.drawable.ic_menu_info_details))
    }

}