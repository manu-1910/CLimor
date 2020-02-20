package io.square1.limor.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import dagger.android.AndroidInjection
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import javax.inject.Inject

abstract class BaseActivity: AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
    }

    protected fun trackBackgroudProgress(isRunning: Boolean){
        if (isRunning){
            //showLoading()
        }else {
            //hideLoading()
        }
    }

    protected open fun showAlert(title: Int, message: Int, okAction: () -> Unit, cancelAction: () -> Unit) {
        alert {
            if (title != 0) this.titleResource = title
            if (message != 0) this.messageResource = message
            okButton {
                okAction()
                it.dismiss()
            }
            noButton {
                cancelAction()
                it.dismiss()
            }
        }.show()
    }
}