package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_new_auth_loading_include.*
import timber.log.Timber

abstract class FragmentWithLoading : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("OnCreate")
        load()
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchCommonVisibility(isLoading = true)
        subscribeToViewModel()
        btnRetry.setOnClickListener {
            switchCommonVisibility(isLoading = true)
            loadFromUserAction()
        }
    }

    abstract fun load()
    abstract val errorLiveData: LiveData<String>

    // If this gets overridden by subclasses `load()` might not be called
    open fun loadFromUserAction() {
        load()
    }

    @CallSuper
    protected open fun subscribeToViewModel() {
        errorLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer
            tvErrorMessage.text = it
            switchCommonVisibility(hasError = true)
        })
    }

    protected fun switchCommonVisibility(isLoading: Boolean = false, hasError: Boolean = false) {
        val shouldShowError = !isLoading && hasError
        val errorVisibility = if (shouldShowError) View.VISIBLE else View.GONE
        tvErrorMessage.visibility = errorVisibility
        btnRetry.visibility = errorVisibility
        tvEmptyContent.visibility = View.GONE
        pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    protected fun showEmptyContentMessage(@StringRes message: Int) {
        tvEmptyContent.visibility = View.VISIBLE
        tvEmptyContent.setText(message)
    }
}