package com.limor.app.scenes.utils

import android.content.Context
import android.text.format.DateUtils
import com.limor.app.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
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

    fun getTimeElapsedFromDateString(dateString: String?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        try {
            val time = sdf.parse(dateString)?.time
            val now = System.currentTimeMillis()
            time?.let {
                return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString()
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }
}