package com.limor.app.scenes.main.fragments.discover2.mock

import java.util.UUID

data class MockPerson(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val nickName: String,
    val imageUrl: String
)