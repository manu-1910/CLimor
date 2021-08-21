package com.limor.app.extensions

import java.time.Duration

const val DURATION_READABLE_FORMAT_1 = "%02d:%02d"
const val DURATION_READABLE_FORMAT_2 = "%dm %ds"
const val DURATION_READABLE_FORMAT_3 = "%02d:%02d:%02d"
const val DURATION_READABLE_FORMAT_4 = "%hm %dm %ds"

/**
 * @param format [DURATION_READABLE_FORMAT_1] or [DURATION_READABLE_FORMAT_2]
 * @return readable String value of the provided duration
 *
 * For 85 seconds output will be "01:25" or "1m 25s"
 */
fun Duration.toReadableFormat(format: String): String {
    val remainingMinutes = toMinutes()
    val remainingSeconds = minusMinutes(remainingMinutes).seconds
    return String.format(format, remainingMinutes, remainingSeconds)
}

fun Duration.toReadableStringFormat(format: String): String{
    val remainingHours = toHours()
    val remainingMinutes = minusHours(remainingHours).toMinutes()
    val remainingSeconds = minusHours(remainingHours).minusMinutes(remainingMinutes).seconds
    return when(format){
        DURATION_READABLE_FORMAT_3 ->
            if(remainingHours == 0L) {
                String.format(DURATION_READABLE_FORMAT_1, remainingMinutes, remainingSeconds)
            } else{
                String.format(format, remainingHours, remainingMinutes, remainingSeconds)
            }
        DURATION_READABLE_FORMAT_4 ->
            if(remainingHours == 0L) {
                String.format(DURATION_READABLE_FORMAT_2, remainingMinutes, remainingSeconds)
            } else{
                String.format(format, remainingHours, remainingMinutes, remainingSeconds)
            }
        else ->
            if(remainingHours == 0L) {
                String.format(DURATION_READABLE_FORMAT_1, remainingMinutes, remainingSeconds)
            } else{
                String.format(format, remainingHours, remainingMinutes, remainingSeconds)
            }
    }
}