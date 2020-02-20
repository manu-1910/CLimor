package io.square1.limor.common

import android.app.ProgressDialog
import androidx.fragment.app.Fragment
import io.square1.limor.di.Injectable
import java.math.BigInteger
import java.security.SecureRandom

abstract class BaseFragment: Fragment(), Injectable {
    var progressDialog: ProgressDialog? = null

    var fragmentId: String? = null

    init {
        generateId()
    }

    open fun generateId() {
        var hash = ""
        this.javaClass.simpleName.map { hash = hash + BigInteger(1, SecureRandom()).toString() + it.toInt() + it }
        fragmentId = hash
    }

    protected fun trackBackgroudProgress(isRunning: Boolean){
        if (isRunning){
            //showLoading()
        }else {
            //hideLoading()
        }
    }
}