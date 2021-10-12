package com.limor.app.dm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentChatSessionsBinding
import javax.inject.Inject

class ChatSessionsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var _binding: FragmentChatSessionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatSessionsBinding.inflate(inflater, container, false)
        setViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModels()
    }

    private fun setViews() {

    }



    private fun subscribeToViewModels() {

    }
}