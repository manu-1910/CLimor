package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.SuggestedUser
import com.limor.app.scenes.auth_new.view.SuggestedPeopleAdapter
import kotlinx.android.synthetic.main.fragment_new_auth_suggested_people.*

class FragmentSuggested : FragmentWithLoading() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_suggested_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
    }

    override fun load() = model.downloadSuggested()

    override val errorLiveData: LiveData<String>
        get() = model.suggestedLiveDataError

    override fun subscribeToViewModel() {
        super.subscribeToViewModel()
        model.suggestedUsersLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                return@Observer
            switchCommonVisibility()
            buildList(it)
        })

        model.suggestedSelectedLiveData.observe(viewLifecycleOwner, Observer {
            btnNext.text = getString(if (it) R.string.continue_button else R.string.btn_skip)
        })
    }

    private fun buildList(users: List<SuggestedUser>) {
        val layoutManager = object : GridLayoutManager(requireContext(), 3) {
            override fun canScrollVertically(): Boolean {
                return true
            }
        }
        rvSuggestedUsers.layoutManager = layoutManager
        rvSuggestedUsers.adapter = object : SuggestedPeopleAdapter(users) {
            override fun onSuggestedFollowClicked(user: SuggestedUser) {
                model.followSuggestedUser(user)
            }
        }
    }


    private fun setOnClickListeners() {
        btnNext.setOnClickListener {
            model.sendSuggestedPeopleSelectionResult()
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_suggested_people_to_fragment_new_auth_onboarding)
        }

        topAppBar.setNavigationOnClickListener {
            AuthActivityNew.popBackStack(requireActivity())
        }
    }
}