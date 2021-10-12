package com.limor.app.dm.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentChatSessionsBinding
import com.limor.app.dm.ChatTarget
import com.limor.app.dm.SessionsViewModel
import com.limor.app.scenes.auth_new.util.AfterTextWatcher
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel
import org.jetbrains.anko.sdk23.listeners.onFocusChange
import javax.inject.Inject

class ChatSessionsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val sessions: SessionsViewModel by viewModels { viewModelFactory }

    private var _binding: FragmentChatSessionsBinding? = null
    private val binding get() = _binding!!
    private var isSearching = false

    private lateinit var targetsAdapter: TargetsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatSessionsBinding.inflate(inflater, container, false)
        targetsAdapter = TargetsAdapter(
            context = requireContext(),
            targets = listOf(),
            onTap = { target -> onTargetTap(target) }
        )
        setViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModels()
    }

    private fun onTargetTap(target: ChatTarget) {
        // start chat with user

    }

    private fun setViews() {
        binding.editSearch.onFocusChange { v, hasFocus ->
            if (hasFocus) {
                isSearching = true
            }
        }
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val term = s.toString()
                targetsAdapter.setTerm(term)
                sessions.searchFollowers(s.toString())
            }
        })
        binding.recyclerTargets.adapter = targetsAdapter
    }

    private fun ensureRecyclerVisibility() {
        val targetVisibility = if (isSearching) View.VISIBLE else View.GONE
        val sessionVisibility = if (isSearching) View.GONE else View.VISIBLE

        if (binding.recyclerTargets.visibility != targetVisibility) {
            binding.recyclerTargets.visibility = targetVisibility
        }

        if (binding.recyclerSessions.visibility != sessionVisibility) {
            binding.recyclerSessions.visibility = sessionVisibility
        }
    }

    private fun setTargets(targets: List<ChatTarget>) {
        ensureRecyclerVisibility()
        targetsAdapter.apply {
            setChatTargets(targets)
            notifyDataSetChanged()
        }
    }

    private fun subscribeToViewModels() {
        sessions.chatTargets.observe(viewLifecycleOwner) {
            if (isSearching) {
                setTargets(it)
            }
        }
    }
}