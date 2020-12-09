package com.limor.app.components

import android.app.AlertDialog
import android.content.Context
import android.widget.Button
import android.widget.ProgressBar
import com.limor.app.R
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk23.listeners.onClick

class AlertProgressBar(val context: Context, cancelCallback: () -> Unit) {

    private val dialog : AlertDialog
    private val progressBar : ProgressBar

    init {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = context.layoutInflater
        dialogBuilder.setTitle(context.getString(R.string.publishing_cast_dialog_title))
        val dialogLayout = inflater.inflate(R.layout.component_progress_bar, null)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.btnCancel)
        progressBar = dialogLayout.findViewById(R.id.progressBar)


        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setCancelable(false)
        dialog = dialogBuilder.show()

        cancelButton?.onClick {
            dialog.dismiss()
            cancelCallback()
        }
    }

    fun show() {
        dialog.show()
    }

    fun updateProgress(current: Int, max: Int) {
        progressBar.progress = current
        progressBar.max = max
    }

    fun dismiss() {
        dialog.dismiss()
    }

}