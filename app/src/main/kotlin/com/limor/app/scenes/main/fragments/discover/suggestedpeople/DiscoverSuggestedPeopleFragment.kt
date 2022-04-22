package com.limor.app.scenes.main.fragments.discover.suggestedpeople

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentDiscoverSuggestedPeopleBinding
import com.limor.app.scenes.main.fragments.discover.suggestedpeople.list.SuggestedPersonBigItem
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.xwray.groupie.GroupieAdapter
import javax.inject.Inject

class DiscoverSuggestedPeopleFragment : BaseFragment() {

    private var _binding: FragmentDiscoverSuggestedPeopleBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: DiscoverSuggestedPeopleViewModel by viewModels { viewModelFactory }

    private val suggestedPeopleAdapter = GroupieAdapter()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
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

    private fun initViews() {
        binding.toolbar.title.setText(R.string.suggested_people)
        binding.toolbar.btnBack.setOnClickListener {
            it.findNavController().popBackStack()
        }
        binding.list.adapter = suggestedPeopleAdapter
        suggestedPeopleAdapter.setOnItemClickListener { item, view ->
            val person = (item as SuggestedPersonBigItem).person
            val userProfileIntent = Intent(context, UserProfileActivity::class.java)
            userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, person.username)
            userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, person.id)
            context?.startActivity(userProfileIntent)
        }

        binding.toolbar.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.navigation_notifications)
        }
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