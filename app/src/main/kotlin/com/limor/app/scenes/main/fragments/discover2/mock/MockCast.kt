package com.limor.app.scenes.main.fragments.discover2.mock

import androidx.annotation.IntRange
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

data class MockCast(
    val id: String = UUID.randomUUID().toString(),
    val owner: MockPerson,
    val date: LocalDateTime,
    val name: String,
    val duration: Duration,
    @IntRange(from = 0, to = 100)
    val playProgress: Int,
    val imageUrl: String
)