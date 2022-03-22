package com.limor.app.scenes.main_new.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import com.limor.app.databinding.FragmentDiscoverSuggestedPeopleBinding
import com.limor.app.scenes.main.fragments.discover.suggestedpeople.DiscoverSuggestedPeopleViewModel
import com.limor.app.scenes.main.fragments.discover.suggestedpeople.list.SuggestedPersonBigItem
import com.xwray.groupie.GroupieAdapter
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class FeedSuggestedPeople: DialogFragment() {
    private var _binding: FragmentDiscoverSuggestedPeopleBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: DiscoverSuggestedPeopleViewModel by viewModels { viewModelFactory }

    private val suggestedPeopleAdapter = GroupieAdapter()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverSuggestedPeopleBinding.inflate(inflater, container, false)
        initViews()
        subscribeForEvents()
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun initViews() {
        binding.toolbar.title.setText(R.string.suggested_people)
        binding.toolbar.btnBack.setOnClickListener {
            dismiss()
        }
        binding.list.adapter = suggestedPeopleAdapter
        suggestedPeopleAdapter.setOnItemClickListener { item, view ->
            val person = (item as SuggestedPersonBigItem).person
            // TODO
        }

        binding.toolbar.btnNotification.visibility = View.GONE
    }

    private fun subscribeForEvents() {
        viewModel.suggestedPeople.observe(viewLifecycleOwner) { suggestedPeople ->
            suggestedPeopleAdapter.update(
                suggestedPeople.map {
                    SuggestedPersonBigItem(person = it, onFollowClick = { person, follow ->
                        viewModel.onFollowClick(person, follow)
                    })
                }
            )
        }
    }
}