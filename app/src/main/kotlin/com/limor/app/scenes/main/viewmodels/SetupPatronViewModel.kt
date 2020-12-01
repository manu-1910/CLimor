package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.ViewModel
import javax.inject.Inject

class SetupPatronViewModel @Inject constructor() : ViewModel() {

    var categorySelectedName: String = ""
    var categorySelectedId: Int = 0

    var plus18Activated: Boolean = false
    var earningsVisibleActivated: Boolean = false
    var patronageVisibleActivated: Boolean = false

    fun clearCategory() {
        categorySelectedId = 0
        categorySelectedName = ""
    }


}