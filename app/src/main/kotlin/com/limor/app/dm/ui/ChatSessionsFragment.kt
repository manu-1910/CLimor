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
import com.limor.app.dm.ChatSessionWithUser
import com.limor.app.dm.ChatTarget
import com.limor.app.dm.SessionsViewModel
import com.limor.app.extensions.hideKeyboard
import org.jetbrains.anko.sdk23.listeners.onFocusChange
import javax.inject.Inject

class ChatSessionsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val chat: SessionsViewModel by viewModels { viewModelFactory }

    private var _binding: FragmentChatSessionsBinding? = null
    private val binding get() = _binding!!
    private var isSearching = false

    private lateinit var targetsAdapter: TargetsAdapter
    private lateinit var sessionsAdapter: SessionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatSessionsBinding.inflate(inflater, container, false)

        createAdapters()
        setViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModels()
    }

    private fun createAdapters() {
        targetsAdapter = TargetsAdapter(
            context = requireContext(),
            targets = listOf(),
            onTap = { target -> onTargetTap(target) }
        )

        sessionsAdapter = SessionsAdapter(
            context = requireContext(),
            sessions = listOf(),
            onTap = { session -> onSessionTap(session) }
        )
    }

    private fun onTargetTap(target: ChatTarget) {
        // start chat with user

    }

    private fun onSessionTap(session: ChatSessionWithUser) {
        //
    }

    private fun setViews() {
        binding.editSearch.onFocusChange { v, hasFocus ->
            if (hasFocus) {
                isSearching = true
                ensureStateVisibility()
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
                chat.searchFollowers(s.toString())
            }
        })

        binding.recyclerTargets.adapter = targetsAdapter

        binding.searchClear.setOnClickListener {
            isSearching = false
            ensureStateVisibility()

            binding.editSearch.apply {
                setTargets(listOf())
                setText("")
                clearFocus()
                hideKeyboard()
            }
        }
    }

    private fun ensureVisibility(view: View, targetVisibility: Int) {
        if (view.visibility != targetVisibility) {
            view.visibility = targetVisibility
        }
    }

    private fun ensureStateVisibility() {
        val visibleWhenSearching = if (isSearching) View.VISIBLE else View.GONE
        val goneWhenSearching = if (isSearching) View.GONE else View.VISIBLE

        ensureVisibility(binding.recyclerTargets, visibleWhenSearching)
        ensureVisibility(binding.recyclerSessions, goneWhenSearching)

        ensureVisibility(binding.searchIcon, goneWhenSearching)
        ensureVisibility(binding.searchClear, visibleWhenSearching)

    }

    private fun setTargets(targets: List<ChatTarget>) {
        ensureStateVisibility()
        targetsAdapter.apply {
            setChatTargets(targets)
            notifyDataSetChanged()
        }
    }

    private fun setSessions(sessions: List<ChatSessionWithUser>) {
        ensureStateVisibility()
        sessionsAdapter.apply {
            setSessions(sessions)
            notifyDataSetChanged()
        }
        binding.layoutPlaceholder.visibility = if (isSearching || sessions.isNotEmpty()) View.GONE else View.VISIBLE
    }

    private fun subscribeToViewModels() {
        chat.chatTargets.observe(viewLifecycleOwner) {
            if (isSearching) {
                setTargets(it)
            }
        }

        chat.sessions.observe(viewLifecycleOwner) {
            setSessions(it)
        }
    }
}