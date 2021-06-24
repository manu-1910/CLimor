package com.limor.app.scenes.utils

import android.content.Context
import com.limor.app.R
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

object DateUiUtil {
    fun getPastDateDaysTextDescription(
        pastDate: LocalDateTime,
        context: Context
    ): String {
        return when (val daysBetween =
            ChronoUnit.DAYS.between(LocalDateTime.now(), pastDate).absoluteValue
        ) {
            0L -> context.getString(R.string.today)
            1L -> context.getString(R.string.yesterday)
            else -> context.getString(R.string.days_ago, daysBetween)
        }
    }
}