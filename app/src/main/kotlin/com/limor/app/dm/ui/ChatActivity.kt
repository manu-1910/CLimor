package com.limor.app.dm.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.databinding.ActivityChatBinding
import com.limor.app.dm.ChatSessionWithUser
import com.limor.app.dm.ChatWithData
import com.limor.app.dm.SessionsViewModel
import com.limor.app.extensions.loadCircleImage
import dagger.android.AndroidInjection
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.widget.textChangeEvents
import javax.inject.Inject

@FlowPreview
class ChatActivity : AppCompatActivity() {

    private var coroutineJob: Job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + coroutineJob)

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val chat: SessionsViewModel by viewModels { viewModelFactory }

    private lateinit var binding: ActivityChatBinding
    private var hasSetHeader = false;

    private var chatAdapter: ChatAdapter? = null
    private var chatSession: ChatSessionWithUser? = null

    private var shouldScrollToBottom = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidInjection.inject(this)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViews()

        getSession()
    }

    private fun getSession() {
        if (!intent.hasExtra(KEY_LIMOR_USER_ID)) {
            finish()
            return
        }

        val limorUserId = intent.extras?.getInt(KEY_LIMOR_USER_ID)
        if (limorUserId == null) {
            finish()
            return
        }

        scope.launch {
            getChat(limorUserId)
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
            data.observe(this@ChatActivity) { onChatData(it) }
        }
    }

    private fun reportGenericError() {
        Toast.makeText(this, getString(R.string.generic_chat_error_message), Toast.LENGTH_LONG)
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
        println("ZZZZ Got chat data with ${chatData.messages.size} messages")
        chatSession = chatData.sessionWithUser

        if (null == chatAdapter) {
            chatAdapter = ChatAdapter(this, chatData)

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
        println("Will set draft to $text")
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
        val linearLayoutManager = object : LinearLayoutManager(baseContext, VERTICAL, false) {
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
            finish()
        }

        listenToEditTextChanges()

        binding.buttonSendMessage.setOnClickListener {
            val session = chatSession ?: return@setOnClickListener

            val messageText = binding.editMessageText.text.toString()
            binding.editMessageText.setText("")

            chat.addMyMessage(session, messageText)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob.cancel()
    }

    companion object {
        const val KEY_LIMOR_USER_ID = "KEY_LIMOR_USER_ID"

        const val draftDebounceTimeMillis = 500L

        fun start(context: Context, limorUserId: Int) {
            Intent(context, ChatActivity::class.java).apply {
                putExtra(KEY_LIMOR_USER_ID, limorUserId)
            }.also {
                context.startActivity(it)
            }
        }
    }
}