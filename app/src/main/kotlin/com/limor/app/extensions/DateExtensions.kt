package com.limor.app.extensions

import java.time.*
import java.time.format.DateTimeParseException
import java.util.*

fun Long.epochSecondToLocalDate(): LocalDate {
    return Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun Long.epochMilliToLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun Long.epochSecondToLocalDateTime(): LocalDateTime {
    return Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

fun Long.epochMilliToLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun String.toLocalDate(): LocalDate {
    return LocalDate.parse(this)
}

fun String.toLocalDateTime(): LocalDateTime {
    val iso = try {
        // The date-times in the backend response are now guaranteed to be in the form of:
        // 2018-08-01T08:23:22.754Z, i.e. an ISO UTC instant
        OffsetDateTime
            .ofInstant(Instant.parse(this), ZoneId.systemDefault())
            .toLocalDateTime()
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
        null
    }
    // If parsing the date-time failed we resort to the default app parsing (the one used
    // historically in the app)
    return iso ?: try {
        ZonedDateTime.parse(this).toLocalDateTime()
    } catch (ex: DateTimeParseException) {
        ex.printStackTrace()
        LocalDateTime.of(2000, 1, 1, 1, 1)
    }
}

fun Calendar.isToday() : Boolean {
    val today = Calendar.getInstance()
    return today[Calendar.YEAR] == get(Calendar.YEAR) && today[Calendar.DAY_OF_YEAR] == get(Calendar.DAY_OF_YEAR)
}