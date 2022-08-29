package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.databinding.FragmentCountryCodeBinding
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.view.CountrySection
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

class FragmentCountryCode : DialogFragment() {

    private val model: AuthViewModelNew by activityViewModels()

    private var sectionedAdapter: SectionedRecyclerViewAdapter? = null

    private lateinit var binding: FragmentCountryCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        model.loadCountriesList(requireActivity().assets)
        binding = FragmentCountryCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
    }

    private fun subscribeToViewModel(){
        model.countriesLiveData.observe(viewLifecycleOwner){
            val groupedCountries: Map<String, List<Country>> = it.groupBy { c -> c.name.first().uppercase() }
            initialiseAdapter(groupedCountries)
        }
    }

    private fun initialiseAdapter(groupedCountries: Map<String, List<Country>>){
        sectionedAdapter = SectionedRecyclerViewAdapter()

        for ((key, value) in groupedCountries.entries) {
            if (value.isNotEmpty()) {
                sectionedAdapter!!.addSection(CountrySection(key, value) { country ->
                    model.setCountrySelected(country, true)
                    findNavController().navigateUp()
                })
            }
        }

        binding.countriesRV.layoutManager = LinearLayoutManager(context)
        binding.countriesRV.adapter = sectionedAdapter
    }

}