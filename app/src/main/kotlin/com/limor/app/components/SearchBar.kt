package com.limor.app.components

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.extensions.makeInVisible
import com.limor.app.extensions.makeVisible
import com.limor.app.extensions.viewScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.anko.sdk23.listeners.onEditorAction
import reactivecircus.flowbinding.android.widget.textChangeEvents

class SearchBar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.search_bar, this)
    }

    private val editText = findViewById<AppCompatEditText>(R.id.search_text)
    private val searchIcon = findViewById<ImageView>(R.id.search_icon)

    private val closeIcon = findViewById<ImageView>(R.id.close_icon)
    private var debounceTime = 500L
    private var onQueryTextChange: ((newText: String) -> Unit)? = null
    private var onQueryTextSubmit: ((query: String) -> Unit)? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SearchBar).apply {
            getString(R.styleable.SearchBar_hintText)?.let { editText.hint = it }
        }.recycle()

        initTextListener()
        initSearchActionListener()
    }

    fun setOnQueryTextListener(
        onQueryTextChange: ((newText: String) -> Unit)? = null,
        onQueryTextSubmit: ((query: String) -> Unit)? = null
    ) {
        this.onQueryTextChange = onQueryTextChange
        this.onQueryTextSubmit = onQueryTextSubmit
    }

    private fun initTextListener() {
        editText.textChangeEvents()
            .skipInitialValue()
            .onEach { event ->
                if (event.start == 0 && event.count == 0) {
                    closeIcon.makeInVisible()
                    searchIcon.makeVisible()
                } else {
                    searchIcon.makeInVisible()
                    closeIcon.makeVisible()
                }
            }
            .debounce(debounceTime)
            .onEach {
                onQueryTextChange?.invoke(it.text.toString())
            }.launchIn(viewScope)
    }

    private fun initSearchActionListener() {
        editText.onEditorAction { v, actionId, event ->
            return@onEditorAction when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    onSearchActionClick()
                    true
                }
                else -> false
            }
        }
    }

    private fun onSearchActionClick() {
        editText.clearFocus()
        hideKeyboard()
        onQueryTextSubmit?.invoke(editText.text.toString())
    }

    fun setDebounceTime(millis: Long) {
        debounceTime = millis
    }
}