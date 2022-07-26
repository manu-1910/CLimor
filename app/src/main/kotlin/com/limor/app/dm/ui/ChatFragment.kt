package com.limor.app.dm.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentChatBinding
import com.limor.app.dm.ChatManager
import com.limor.app.dm.ChatSessionWithUser
import com.limor.app.dm.ChatWithData
import com.limor.app.dm.SessionsViewModel
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.viewmodels.PodcastViewModel
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.main_new.fragments.FragmentPodcastPopup
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.uimodels.CastUIModel
import com.limor.app.util.SoundType
import com.limor.app.util.Sounds
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.widget.textChangeEvents
import javax.inject.Inject

@FlowPreview
class ChatFragment : BaseFragment() {

    private var coroutineJob: Job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + coroutineJob)

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val chat: SessionsViewModel by viewModels { viewModelFactory }
    private val podcastViewModel: PodcastViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var chatManager: ChatManager

    private val userId: Int by lazy {
        requireArguments().getInt(
            ChatFragment.KEY_LIMOR_USER_ID,
            -1
        )
    }

    private lateinit var binding: FragmentChatBinding
    private var hasSetHeader = false;

    private var chatAdapter: ChatAdapter? = null
    private var chatSession: ChatSessionWithUser? = null

    private var shouldScrollToBottom = false;
    private var lastMessageId: Int = Int.MIN_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(layoutInflater)

        setViews()

        getSession()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }

    private fun getSession() {
        if (userId == -1) {
            findNavController().navigateUp()
            return
        }

        chatManager.chattingUserId = userId

        scope.launch {
            getChat(userId)
        }
    }

    private suspend fun getChat(limorUserId: Int) {
        val data = chat.getChat(limorUserId)
        if (data == null) {
            reportGenericError()
        } else {
            observeChat(data)
        }
    }

    private fun observeChat(data: LiveData<ChatWithData>) {
        lifecycleScope.launch {
            data.observe(this@ChatFragment) { onChatData(it) }
        }
    }

    private fun reportGenericError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.generic_chat_error_message),
            Toast.LENGTH_LONG
        )
            .show()
    }

    private fun scrollChatToBottom(smooth: Boolean = true) {
        val ca = chatAdapter ?: return
        if (ca.itemCount == 0) {
            return
        }
        binding.recyclerChat.apply {
            val position = ca.itemCount - 1
            if (smooth) smoothScrollToPosition(position) else scrollToPosition(position)
        }
    }

    private fun registerAdapterObserver() {
        chatAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.recyclerChat.scrollToPosition(positionStart)
            }
        })
    }

    private fun onChatData(chatData: ChatWithData) {
        if (BuildConfig.DEBUG) {
            println("ZZZZ Got chat data with ${chatData.messages.size} messages")
        }
        chatSession = chatData.sessionWithUser

        if (null == chatAdapter) {
            chatAdapter = ChatAdapter(requireContext(), chatData, onCastClick = { id ->
                onCastClick(id)
            })

            binding.recyclerChat.adapter = chatAdapter
            scrollChatToBottom(false)
            registerAdapterObserver()

            binding.editMessageText.apply {
                val text = chatData.sessionWithUser.session.draftContent
                setText(text)
                setSelection(text.length)
            }

        } else {
            chatAdapter?.setChatData(chatData)
        }

        if (!hasSetHeader) {
            hasSetHeader = true;
            setHeader(chatData)
        }

        markAsRead(chatData)
        playSound(chatData)
    }

    private fun playSound(chatData: ChatWithData) {
        chatData.messages.lastOrNull()?.let { message ->
            if (Int.MIN_VALUE != lastMessageId && message.id != lastMessageId) {
                Sounds.playSound(requireContext(), SoundType.MESSAGE)
            }
            lastMessageId = message.id
        }
    }

    private fun markAsRead(chatData: ChatWithData) {
        if (BuildConfig.DEBUG) {
            println("Messages: ${chatData.messages}")
        }
        val lastChatMessage = chatData.messages.lastOrNull() ?: return

        if (chatData.sessionWithUser.session.lastReadMessageId == lastChatMessage.id) {
            return
        }

        // update in-memory object just in case of delayed synchronization
        chatData.sessionWithUser.session.apply {
            lastReadMessageId = lastChatMessage.id
            unreadCount = 0
        }

        chatData.sessionWithUser.session.copy(
            lastReadMessageId = lastChatMessage.id,
            unreadCount = 0
        ).also {
            chat.updateSession(it)
        }

    }

    private fun setHeader(chatData: ChatWithData) {
        chatData.sessionWithUser.user.let {
            binding.textTitle.text = it.limorDisplayName
            binding.profile.loadCircleImage(it.limorProfileUrl)
        }
    }

    private fun enableSendButton(text: String) {
        val enable = text.isNotEmpty()
        binding.buttonSendMessage.apply {
            isEnabled = enable
            isActivated = enable
        }
    }

    private fun saveDraft(text: String) {
        val session = chatSession?.session ?: return
        if (session.draftContent.equals(text)) {
            return
        }
        if (BuildConfig.DEBUG) {
            println("Will set draft to $text")
        }
        chat.setDraft(session, text)
    }

    private fun listenToEditTextChanges() {
        binding.editMessageText
            .textChangeEvents()
            .onEach { event -> enableSendButton(event.text.toString()) }
            .debounce(draftDebounceTimeMillis)
            .onEach { event -> saveDraft(event.text.toString()) }
            .launchIn(lifecycleScope)
    }

    private fun setViews() {
        val linearLayoutManager = object : LinearLayoutManager(requireContext(), VERTICAL, false) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
            }
        }
        linearLayoutManager.stackFromEnd = true
        binding.recyclerChat.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    scrollChatToBottom()
                }
            }
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        listenToEditTextChanges()

        binding.buttonSendMessage.setOnClickListener {
            val session = chatSession ?: return@setOnClickListener

            val messageText = binding.editMessageText.text.toString()
            binding.editMessageText.setText("")

            chat.addMyMessage(session, messageText)
        }
    }

    private fun onCastClick(id: Int) {
        podcastViewModel.loadCast(id)
        podcastViewModel.cast.observe(viewLifecycleOwner, { cast ->
            if (cast.patronDetails?.purchased == false && cast.owner?.id != PrefsHandler.getCurrentUserId(
                    requireContext()
                )
            ) {
                val dialog = FragmentPodcastPopup.newInstance(cast.id)
                dialog.show(parentFragmentManager, FragmentPodcastPopup.TAG)
            } else {
                openPlayer(cast)
            }
            podcastViewModel.cast.removeObservers(viewLifecycleOwner)
        })
    }

    private fun openPlayer(cast: CastUIModel) {
        (activity as? PlayerViewManager)?.showPlayer(
            PlayerViewManager.PlayerArgs(
                PlayerViewManager.PlayerType.EXTENDED,
                cast.id
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob.cancel()
        chatManager.clearChattingUserId()
    }

    companion object {
        const val KEY_LIMOR_USER_ID = "KEY_LIMOR_USER_ID"

        const val EXTRA_CHAT_ACTION = "EXTRA_CHAT_ACTION"
        const val EXTRA_PODCAST_ID = "EXTRA_PODCAST_ID"
        const val ACTION_OPEN_PODCAST = "ACTION_OPEN_PODCAST"

        const val draftDebounceTimeMillis = 500L

        /*fun getStartIntent(context: Context, limorUserId: Int) =
            Intent(context, ChatActivity::class.java).apply {
                putExtra(KEY_LIMOR_USER_ID, limorUserId)
        }*/

    }
}