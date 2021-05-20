package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_new_auth_loading_include.*

abstract class FragmentWithLoading : Fragment() {

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        switchCommonVisibility(isLoading = true)
        btnRetry.setOnClickListener {
            switchCommonVisibility(isLoading = true)
            load()
        }
        load()
    }

    abstract fun load()
    abstract val errorLiveData: LiveData<String>

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
        pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}