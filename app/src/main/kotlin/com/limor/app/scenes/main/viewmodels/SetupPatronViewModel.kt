package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.ViewModel
import javax.inject.Inject

class SetupPatronViewModel @Inject constructor() : ViewModel() {

    var categorySelectedName: String = ""
    var categorySelectedId: Int = 0

    fun clearCategory() {
        categorySelectedId = 0
        categorySelectedName = ""
    }


}