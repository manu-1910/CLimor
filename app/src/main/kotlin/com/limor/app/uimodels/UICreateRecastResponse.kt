package com.limor.app.uimodels

data class UICreateRecastResponse (
    val podcast_id: Int,
    val count: Int,
    val recasted: Boolean,
    val created: Boolean
)