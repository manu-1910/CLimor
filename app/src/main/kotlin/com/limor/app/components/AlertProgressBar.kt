package com.limor.app.components

import android.app.AlertDialog
import android.content.Context
import android.widget.ProgressBar
import com.limor.app.R
import org.jetbrains.anko.layoutInflater

class AlertProgressBar(val context: Context) {

    private val dialog: AlertDialog
    private val progressBar: ProgressBar

    init {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = context.layoutInflater
        dialogBuilder.setTitle(context.getString(R.string.publishing_cast_dialog_title))
        val dialogLayout = inflater.inflate(R.layout.component_progress_bar, null)
        progressBar = dialogLayout.findViewById(R.id.progressBar)


        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setCancelable(false)
        dialog = dialogBuilder.show()
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