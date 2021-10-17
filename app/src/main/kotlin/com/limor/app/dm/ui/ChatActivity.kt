package com.limor.app.dm.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private var shouldScrollToBottom = true;

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
        Toast.makeText(this, getString(R.string.generic_chat_error_message), Toast.LENGTH_LONG).show()
    }

    private fun scrollChatToBottom() {
        val ca = chatAdapter ?: return
        if (ca.itemCount == 0) {
            return
        }
        binding.recyclerChat.post {
            binding.recyclerChat.scrollToPosition(ca.itemCount - 1);
        }
    }

    private fun onChatData(chatData: ChatWithData) {
        println("ZZZZ Got chat data with ${chatData.messages.size} messages")
        chatSession = chatData.sessionWithUser

        if (null == chatAdapter) {
            chatAdapter = ChatAdapter(
                this,
                chatData
            )
            binding.recyclerChat.adapter = chatAdapter
        } else {
            chatAdapter?.apply {
                setChatData(chatData)
                notifyDataSetChanged()
            }
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

    private fun setViews() {
        val linearLayoutManager = object : LinearLayoutManager(baseContext, VERTICAL, false) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                if (shouldScrollToBottom) {
                    scrollChatToBottom()
                    println("scrolling in onLayoutComplete")
                }
                shouldScrollToBottom = false
            }
        }
        binding.recyclerChat.layoutManager = linearLayoutManager

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.editMessageText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                shouldScrollToBottom = true
                scrollChatToBottom()
            }
        }
        binding.editMessageText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val enable = s.isNotEmpty()
                binding.buttonSendMessage.apply {
                    isEnabled = enable
                    isActivated = enable
                }
            }
        })

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

        fun start(context: Context, limorUserId: Int) {
            Intent(context, ChatActivity::class.java).apply {
                putExtra(KEY_LIMOR_USER_ID, limorUserId)
            }.also {
                context.startActivity(it)
            }
        }
    }
}