package com.limor.app.extensions

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import com.limor.app.R
import com.limor.app.common.SingleLiveEvent
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


/**
 * Throttles click events by emiting the frist click only within a default time frame of 1500
 * milliseconds.
 */
@SuppressLint("CheckResult")
fun View.throttledClick(throttleDurationMillis: Long = 1500, onClick: (view: View) -> Unit) {
    clicks()
        .throttleFirst(throttleDurationMillis, TimeUnit.MILLISECONDS)
        .subscribe {
            onClick(this)
        }
}

fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    Snackbar.make(this, snackbarText, timeLength).show()
}

fun View.showLongToast(context: Context, toastText: String) {
    Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
}

fun View.showShortToast(context: Context, toastText: String) {
    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
}

fun View.setupSnackbar(
    lifecycleOwner: LifecycleOwner,
    snackbarMessageLiveEvent: SingleLiveEvent<Int>,
    timeLength: Int
) {
    snackbarMessageLiveEvent.observe(lifecycleOwner, Observer { resource ->
        resource?.let { showSnackbar(context.getString(it), timeLength) }
    })
}

fun View.setupLongToast(
    lifecycleOwner: LifecycleOwner,
    toastMessageLiveEvent: SingleLiveEvent<Int>
) {
    toastMessageLiveEvent.observe(lifecycleOwner, Observer { resource ->
        resource?.let { showLongToast(context, context.getString(it)) }
    })
}

fun View.setupShortToast(
    lifecycleOwner: LifecycleOwner,
    toastMessageLiveEvent: SingleLiveEvent<Int>
) {
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

val Int.precisePx: Float
    get() = this * Resources.getSystem().displayMetrics.density

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

fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeInVisible() {
    visibility = View.INVISIBLE
}

fun View.makeGone() {
    visibility = View.GONE
}

fun View.ensureGone() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

val View.viewScope: CoroutineScope
    get() {
        val storedScope = getTag(R.string.view_coroutine_scope) as? CoroutineScope
        if (storedScope != null && storedScope.isActive) return storedScope

        val newScope = ViewCoroutineScope()
        addOnAttachStateChangeListener(newScope)
        setTag(R.string.view_coroutine_scope, newScope)

        return newScope
    }

inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
    getInt(index, -1).let {
        if (it >= 0) enumValues<T>()[it] else default
    }

private class ViewCoroutineScope : CoroutineScope, View.OnAttachStateChangeListener {
    override val coroutineContext = SupervisorJob() + Dispatchers.Main

    override fun onViewAttachedToWindow(view: View) = Unit

    override fun onViewDetachedFromWindow(view: View) {
        coroutineContext.cancel()
        view.setTag(R.string.view_coroutine_scope, null)
    }
}

fun ViewGroup.allChildren(onView: (View) -> Unit) {
    this.children.forEach {
        it.apply {
            onView(it)
            (this as? ViewGroup)?.apply { allChildren(onView) }
        }
    }
}

/**
 * Makes a view .VISIBLE if the condition is true else sets the visibility to .GONE
 */
fun View.visibleIf(condition: Boolean) {
    this.visibility = if (condition) View.VISIBLE else View.GONE
}