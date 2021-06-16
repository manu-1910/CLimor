package com.limor.app.scenes.main.fragments.discover2.items

class HeaderItem(
    val name: String,
    val action: HeaderAction? = null
) {
    data class HeaderAction(val name: String, val onActionClick: () -> Unit)

}