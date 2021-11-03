package com.limor.app.scenes.patron.manage.fragment

import android.R
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.limor.app.databinding.DialogChangePriceBinding
import com.limor.app.scenes.patron.manage.viewmodels.ManagePatronViewModel
import kotlinx.android.synthetic.main.dialog_change_price.*
import javax.inject.Inject

class DialogChangePrice : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: ManagePatronViewModel by activityViewModels{ viewModelFactory }
    private lateinit var binding : DialogChangePriceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogChangePriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClicks()
        setPrices()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    private fun setOnClicks() {
        binding.cancelButton.setOnClickListener { this.dismiss() }
        binding.yesButton.setOnClickListener {
            findNavController().navigate(com.limor.app.R.id.action_dialog_change_price_to_dialog_change_price_confirmation)
        }
    }

    private fun setPrices(){
        val editText = priceTIL.editText as AutoCompleteTextView

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), R.layout.select_dialog_item, listOf("2.99", "3.99", "4.99", "5.99"))
        editText.setAdapter(adapter)
    }

}