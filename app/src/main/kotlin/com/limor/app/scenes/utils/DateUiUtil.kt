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

    fun getTimeAgoText(
        pastDate: LocalDateTime,
        context: Context
    ): String {
        val date = pastDate.minusSeconds(pastDate.second.toLong()).minusMinutes(pastDate.minute.toLong()).minusHours(pastDate.hour.toLong())
        var now = LocalDateTime.now()
        val days = ChronoUnit.DAYS.between(now, date).absoluteValue
        val weeks = ChronoUnit.WEEKS.between(now, date).absoluteValue
        val months = ChronoUnit.MONTHS.between(now, date).absoluteValue
        val years = ChronoUnit.YEARS.between(now, date).absoluteValue
        return when{
            days == 0L -> context.getString(R.string.today)
            days == 1L -> context.getString(R.string.yesterday)
            days < 7L -> context.getString(R.string.days_ago, days)
            weeks == 1L -> context.getString(R.string.week_ago)
            (weeks <= 4L && months == 0L) -> context.getString(R.string.weeks_ago, weeks)
            months == 1L -> context.getString(R.string.month_ago)
            (months < 12L && years == 0L) -> context.getString(R.string.months_ago, months)
            years == 1L -> context.getString(R.string.year_ago)
            years > 1L -> context.getString(R.string.years_ago, years)
            else -> ""
        }
    }

}