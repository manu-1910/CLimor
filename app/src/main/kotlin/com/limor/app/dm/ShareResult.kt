package com.limor.app.dm

data class ShareResult(
    val hasShared: Boolean,
    val newSharesCount: Int,
    val shareUrl: String? = null
)
