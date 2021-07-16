package com.limor.app.extensions

import java.time.*
import java.time.format.DateTimeParseException

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
    return try {
        ZonedDateTime.parse(this).toLocalDateTime()
    } catch (ex: DateTimeParseException) {
        LocalDateTime.of(2000, 1, 1, 1, 1)
    }
}