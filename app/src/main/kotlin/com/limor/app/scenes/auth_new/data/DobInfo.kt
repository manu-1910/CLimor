package com.limor.app.scenes.auth_new.data

import android.text.format.DateFormat
import java.util.*
import java.util.Calendar.*

data class DobInfo(val mills: Long) {

    fun isValid(): Boolean {
        if (mills == 0L) return false
        val userDob = Date(mills)
        val today = Date()
        return getDiffYears(userDob, today) >= VALID_DOB_YEARS
    }

    val formatted: String
        get() = parseDate(mills)

    private fun getDiffYears(first: Date?, last: Date?): Int {
        val a = getCalendar(first)
        val b = getCalendar(last)
        var diff = b[YEAR] - a[YEAR]
        if (a[MONTH] > b[MONTH] ||
            a[MONTH] == b[MONTH] && a[DATE] > b[DATE]
        ) {
            diff--
        }
        return diff
    }

    private fun getCalendar(date: Date?): Calendar {
        val cal = getInstance(Locale.US)
        cal.time = date
        return cal
    }

    companion object {
        const val VALID_DOB_YEARS = 13
        fun Empty(): DobInfo {
            return DobInfo(0)
        }

        fun parseDate(mills: Long): String {
            if(mills == 0L) return ""
            return DateFormat.format("dd MMM, yyyy ", Date(mills)).toString()
        }

        fun parseForUserCreation(mills: Long): String {
            if(mills == 0L) return ""
            return DateFormat.format("YYYY-MM-DD", Date(mills)).toString()
        }
    }
}