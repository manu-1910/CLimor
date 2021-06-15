package com.limor.app.extensions

import android.animation.LayoutTransition
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.limor.app.common.SingleLiveEvent


fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    Snackbar.make(this, snackbarText, timeLength).show()
}

fun View.showLongToast(context: Context, toastText: String) {
    Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
}

fun View.showShortToast(context: Context, toastText: String) {
    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
}

fun View.setupSnackbar(lifecycleOwner: LifecycleOwner, snackbarMessageLiveEvent: SingleLiveEvent<Int>, timeLength: Int) {
    snackbarMessageLiveEvent.observe(lifecycleOwner, Observer { resource ->
        resource?.let { showSnackbar(context.getString(it), timeLength) }
    })
}
fun View.setupLongToast(lifecycleOwner: LifecycleOwner, toastMessageLiveEvent: SingleLiveEvent<Int>) {
    toastMessageLiveEvent.observe(lifecycleOwner, Observer { resource ->
        resource?.let { showLongToast(context, context.getString(it)) }
    })
}
fun View.setupShortToast(lifecycleOwner: LifecycleOwner, toastMessageLiveEvent: SingleLiveEvent<Int>) {
    toastMessageLiveEvent.observe(lifecycleOwner, Observer { resource ->
        resource?.let { showShortToast(context, context.getString(it)) }
    })
}

fun View.hideKeyboard(): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.showKeyboard() {
    val imm = context
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun ViewGroup.inflate(layoutRes: Int): View =
    LayoutInflater.from(context).inflate(layoutRes, this, false)

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(adapterPosition, itemViewType)
    }
    return this
}

fun ViewGroup.forceLayoutChanges() {
    val layoutTransition = LayoutTransition()
    layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    setLayoutTransition(layoutTransition)
}

fun Fragment.drawSimpleSelectorDialog(
    title: String,
    items: List<String>,
    listener: (DialogInterface, Int) -> Unit
) {
    val dialog = AlertDialog.Builder(context!!)
    dialog.setTitle(title)
    dialog.setItems(items.toTypedArray(), listener)
    dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
        dialogInterface.dismiss()
    }
    dialog.show()
}