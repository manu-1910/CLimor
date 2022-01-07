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
import com.limor.app.GetPodcastByIdQuery
import com.limor.app.R
import com.limor.app.databinding.DialogGenericAlertBinding
import com.limor.app.databinding.DialogTextInputAlertBinding
import com.limor.app.extensions.px
import org.jetbrains.anko.sdk23.listeners.onClick

class LimorTextInputDialog(private val layoutInflater: LayoutInflater) {

    var dismissOnAnyClick = true

    private val dialogView: DialogTextInputAlertBinding =
        DialogTextInputAlertBinding.inflate(layoutInflater)

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

    private fun getFiller(): View {
        return View(layoutInflater.context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 48.px).apply {
                weight = 0.44f
            }
        }
    }

    fun setTitle(title: String) {
        dialogView.textTitle.text = title
    }

    fun setTitle(titleResId: Int) {
        dialogView.textTitle.setText(titleResId)
    }

    fun setHint(hintResId: Int){
        dialogView.inputCastTitle.setHint(hintResId)
    }

    fun setTitle(spannable: Spannable) {
        dialogView.textTitle.setText(spannable, TextView.BufferType.SPANNABLE)
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

    private fun adjustUI() {
        if (dialogView.buttons.childCount == 1) {
            dialogView.buttons.addView(getFiller(), 0)
            dialogView.buttons.addView(getFiller(), 2)
        }
    }

    fun show() {
        adjustUI()
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