package com.limor.app.scenes.main_new.fragments.mentions

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.R
import com.limor.app.databinding.PopupUserMentionBinding
import com.limor.app.extensions.highlight
import com.limor.app.scenes.main_new.adapters.UserMentionAdapter
import com.limor.app.uimodels.UserUIModel
import org.jetbrains.anko.layoutInflater

class UserMentionPopup(private val editText: EditText, private val dataProvider: UserMentionData) :
    TextWatcher {

    interface UserMentionData {
        fun search(text: String)
    }

    private val binding: PopupUserMentionBinding =
        PopupUserMentionBinding.inflate(editText.context.layoutInflater)
    private val popup = PopupWindow(editText.context)

    private lateinit var userMentionAdapter: UserMentionAdapter
    private var users: List<UserUIModel> = listOf()

    var inputHeight: Int = 0
        set(value) {
            val wasDifferent = field != value
            field = value
            if (wasDifferent) {
                adjustPopupPosition()
            }
        }

    init {
        initEditText()
        initRecyclerView()
        initPopup()
    }

    fun setUsers(users: List<UserUIModel>) {
        userMentionAdapter.setData(users)
    }

    private fun initEditText() {
        editText.addTextChangedListener(this)
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(editText.context)
        binding.userMentionRecycler.layoutManager = layoutManager
        binding.userMentionRecycler.itemAnimator = null

        userMentionAdapter = UserMentionAdapter(users,
            object : UserMentionAdapter.OnUserClickListener {
                override fun onUserClicked(user: UserUIModel) {
                    mentionUser(user)
                }
            })
        binding.userMentionRecycler.adapter = userMentionAdapter
        binding.userMentionRecycler.setHasFixedSize(false)
    }

    private fun mentionUser(user: UserUIModel) {
        popup.dismiss()

        val text = editText.text.toString()
        val left = text.substring(0, editText.selectionStart)
        val chunks = left.split(" ")

        chunks.last().let { word ->
            if (word.length > 1 && word.startsWith('@')) {
                var toLeft = chunks.subList(0, chunks.size - 1).joinToString(" ")
                // adds a space to separate the left and right part but only if there's anything
                // to the left. There won't be anything to the left if the user's first text is a
                // mention
                if (toLeft.isNotEmpty()) {
                    toLeft = "$toLeft "
                }
                val middle = "@${user.username}"
                val toRight = text.substring(editText.selectionStart)
                val withUser = "$toLeft$middle "
                val targetText = "$withUser$toRight"

                editText.setText(targetText)
                editText.highlight(userMentionPattern, R.color.waveFormColor)
                editText.setSelection(withUser.length)
            }
        }
    }

    private fun initPopup() {
        popup.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        popup.contentView = binding.root
        popup.width = ViewGroup.LayoutParams.MATCH_PARENT
        popup.height = ViewGroup.LayoutParams.MATCH_PARENT

        val whiteBackground = ColorDrawable(ContextCompat.getColor(editText.context, R.color.white))
        popup.setBackgroundDrawable(whiteBackground)
    }

    private fun adjustPopupPosition() {
        val maxContentHeight: Int =
            popup.getMaxAvailableHeight(
                editText,
                inputHeight,
                false
            )

        val maxContentWidth = editText.resources.displayMetrics.widthPixels
        binding.root.measure(
            MeasureSpec.makeMeasureSpec(maxContentWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(maxContentHeight - inputHeight, MeasureSpec.EXACTLY)
        )

        popup.height = binding.root.measuredHeight
        println("Adjusting popup position to ${popup.height} with $inputHeight")
    }

    private fun search(text: String) {
        dataProvider.search(text)

        if (!popup.isShowing) {
            adjustPopupPosition()
            popup.showAtLocation(editText, Gravity.TOP, 0, 0)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        println("After text changed... -> ")
        val text = s?.toString() ?: ""
        if (text.isEmpty()) {
            popup.dismiss()
            return
        }

        val left = text.substring(0, editText.selectionStart)
        val chunks = left.split(" ")

        chunks.last().let { word ->
            if (word.length > 1 && word.startsWith('@')) {
                search(word.substring(1))
            } else {
                popup.dismiss()
            }
        }
    }

    companion object {
        val userMentionPattern = "@\\w\\S*\\b".toRegex()
    }
}