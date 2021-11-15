package com.limor.app.scenes.utils

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.limor.app.R
import com.limor.app.databinding.DialogGenericAlertBinding
import com.limor.app.extensions.px
import org.jetbrains.anko.sdk23.listeners.onClick

class LimorDialog(private val layoutInflater: LayoutInflater) {

    var dismissOnAnyClick = true

    private val dialogView: DialogGenericAlertBinding =
        DialogGenericAlertBinding.inflate(layoutInflater)

    private val dialogBuilder = AlertDialog.Builder(layoutInflater.context).apply {
        setView(dialogView.root)
        setCancelable(true)
    }

    val dialog: AlertDialog = dialogBuilder.create()

    private fun getDialogButton(labelResId: Int, mainAction: Boolean = false): Button {
        val resId = if (mainAction) R.layout.dialog_button_main else R.layout.dialog_button_normal
        return (layoutInflater.inflate(resId, null) as Button).apply {
            setText(labelResId)
            layoutParams = LinearLayout.LayoutParams(0, 48.px).apply {
                weight = 1.0f
            }
        }
    }

    fun setTitle(title: String) {
        dialogView.textTitle.text = title
    }

    fun setTitle(titleResId: Int) {
        dialogView.textTitle.setText(titleResId)
    }

    fun setTitle(spannable: Spannable) {
        dialogView.textTitle.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    fun setMessage(spannable: Spannable) {
        dialogView.textMessage.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    fun setIcon(iconResId: Int) {
        dialogView.headerIcon.apply {
            setImageResource(iconResId)
            visibility = View.VISIBLE
        }
    }

    /**
     * Order is important, the order is preserved and buttons are stacked from left to right. This
     * means that the first button is the left-most button.
     */
    fun addButton(labelResId: Int, mainAction: Boolean = false, onClick: (() -> Unit)? = null) {
        getDialogButton(labelResId, mainAction).also {
            dialogView.buttons.addView(it)
        }.onClick {
            if (dismissOnAnyClick) {
                dismiss()
            }
            onClick?.invoke()
        }
    }

    fun show() {
        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)
        dialog.apply {
            window?.setBackgroundDrawable(inset)
            show()
        }
    }

    fun dismiss() {
        dialog.dismiss()
    }
}