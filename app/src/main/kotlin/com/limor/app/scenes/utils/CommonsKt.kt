package com.limor.app.scenes.utils

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.graphics.Color

import android.os.Build
import android.text.Editable
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.limor.app.App
import com.limor.app.R
import com.limor.app.scenes.authentication.SignActivity
import com.limor.app.uimodels.CastUIModel

import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UserUIModel
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import android.content.res.XmlResourceParser
import android.util.SparseArray
import java.time.LocalDate
import kotlin.collections.HashMap


class CommonsKt {

    companion object {

        //val audioFileFormat: String = ".amr"
        val audioFileFormat: String = ".wav"

        var user: UserUIModel? = null

        fun getDateTimeFormatted(): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                current.format(formatter)
            } else {
                var date = Date()
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
                formatter.format(date)
            }
        }

        fun getFormattedLocalDate(localDate: LocalDate?): String {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
            return formatter.format(localDate)?:""
        }


        fun getDateTimeFormattedFromTimestamp(time: Long): String {
            val cal = Calendar.getInstance(Locale.getDefault())
            cal.timeInMillis = time * 1000
            return DateFormat.format("dd/MM/yyyy HH:mm", cal).toString()
        }

        fun getDateFromString(date: String): Date? {
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return formatter.parse(date)
        }


        @Throws(IOException::class)
        fun copyFile(sourceFile: File?, destFile: File) {
            if (!destFile.parentFile.exists()) destFile.parentFile.mkdirs()
            if (!destFile.exists()) {
                destFile.createNewFile()
            }
            var source: FileChannel? = null
            var destination: FileChannel? = null
            try {
                source = FileInputStream(sourceFile).channel
                destination = FileOutputStream(destFile).channel
                destination.transferFrom(source, 0, source.size())
            } finally {
                source?.close()
                destination?.close()
            }
        }


        fun calculateDurationMediaPlayer(duration: Int): String {
            var finalDuration = ""
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong())
            if (minutes == 0L) {
                //finalDuration = "0:$seconds"
                finalDuration = String.format("00:%02d", seconds)
            } else {
                if (seconds >= 60) {
                    val sec = seconds - minutes * 60
                    //finalDuration = "$minutes:$sec"
                    finalDuration = String.format("%02d:%02d", minutes, sec)
                }
            }
            return finalDuration
        }

        fun calculateDurationMinutesAndSeconds(millis: Long): String {
            // if it's less than one minute
            return if (millis < 60 * 1000) {
                String.format("%ds", TimeUnit.MILLISECONDS.toSeconds(millis))

                // if it's less than one hour
            } else if (millis < 3600 * 1000) {
                String.format(
                    "%dm %ds",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                )

                // if it's more than one hour
            } else {
                String.format(
                    "%dh %dm" /*%ds*/,
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(
                            millis
                        )
                    )//,
                    //TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                )
            }
        }

        fun calculateDurationMinutesAndSeconds(seconds: Double): String {
            val millis = (seconds * 1000).toLong()
            return calculateDurationMinutesAndSeconds(millis)
        }


        fun dpToPx(dp: Float, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics
            ).toInt()
        }

        fun reduceSwipeSensitivity(viewPager: ViewPager2) {
            val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            recyclerViewField.isAccessible = true
            val recyclerView = recyclerViewField.get(viewPager) as RecyclerView

            val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
            touchSlopField.isAccessible = true
            val touchSlop = touchSlopField.get(recyclerView) as Int
            touchSlopField.set(recyclerView, touchSlop * 8)       // "8" was obtained experimentally
        }

        fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
        fun Int.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this.toString())


        fun formatSocialMediaQuantity(quantity: Int): String {
            return when {
                quantity < 10000 -> {
                    quantity.toString()
                }
                quantity in 10000..99999 -> {
                    val newQty = quantity / 1000.toFloat()
                    val output = "%.1fk".format(newQty)
                    output
                }
                quantity in 100000..999999 -> {
                    val newQty = quantity / 1000.toFloat()
                    val output = "%.0fk".format(newQty)
                    output
                }
                quantity in 1000000..99999999 -> {
                    val newQty = quantity / 1000000.toFloat()
                    val output = "%.1fM".format(newQty)
                    output
                }
                quantity in 10000000..999999999 -> {
                    val newQty = quantity / 1000000.toFloat()
                    val output = "%.0fM".format(newQty)
                    output
                }
                else -> {
                    val newQty = quantity / 1000000000.toFloat()
                    val output = "%.1fB".format(newQty)
                    output
                }
            }
        }

        fun File.copyTo(file: File) {
            inputStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        fun setButtonLimorStylePressed(
            button: Button,
            primaryStatus: Boolean,
            textPrimary: Int,
            textSecondary: Int,
        ) {
            if (primaryStatus) {
                button.background = ContextCompat.getDrawable(
                    button.context, R.drawable.bg_round_yellow_ripple
                )
                button.setTextColor(
                    ContextCompat.getColor(
                        button.context,
                        R.color.brandSecondary500
                    )
                )
                button.text = button.context.getString(textPrimary)
            } else {
                button.text = button.context.getString(textSecondary)
                button.background = ContextCompat.getDrawable(
                    button.context,
                    R.drawable.bg_round_transparent_ripple
                )
                button.setTextColor(ContextCompat.getColor(button.context, R.color.brandPrimary500))
            }
        }

        fun setUserItemButtonPressed(
            button: Button,
            primaryStatus: Boolean,
            textPrimary: Int,
            textSecondary: Int,
        ) {
            if (primaryStatus) {
                button.background = ContextCompat.getDrawable(
                    button.context, R.drawable.bg_round_yellow_ripple
                )
                button.setTextColor(ContextCompat.getColor(button.context, R.color.black))
                button.text = button.context.getString(textPrimary)
            } else {
                button.text = button.context.getString(textSecondary)
                button.background = ContextCompat.getDrawable(
                    button.context,
                    R.drawable.bg_round_transparent_ripple
                )
                button.setTextColor(ContextCompat.getColor(button.context, R.color.black))
            }
        }

        fun setButtonFollowerStylePressed(
            button: Button,
            primaryStatus: Boolean,
            textPrimary: Int,
            textSecondary: Int,
        ) {
            if (primaryStatus) {
                button.background = ContextCompat.getDrawable(
                    button.context, R.drawable.bg_round_yellow_ripple
                )
                button.setTextColor(ContextCompat.getColor(button.context, R.color.black))
                button.text = button.context.getString(textPrimary)
            } else {
                button.text = button.context.getString(textSecondary)
                button.background = ContextCompat.getDrawable(
                    button.context,
                    R.drawable.bg_round_bluish_ripple
                )
                button.setTextColor(
                    ContextCompat.getColor(
                        button.context,
                        R.color.notification_divider
                    )
                )
            }
        }


        fun ageToTimestamp(age: Int): Long {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, age)
            val timeMilli = calendar.timeInMillis

            return timeMilli
        }


        fun timestampToAge(age: Long): Int {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = age
            val timeYear = calendar.get(Calendar.YEAR)

            val calendarNow = Calendar.getInstance()
            val currentYear = calendarNow.get(Calendar.YEAR)

            return timeYear - currentYear
        }

        fun handleOnApiError(
            app: App,
            context: Context,
            fragment: Fragment,
            errorResponse: UIErrorResponse,
        ) {
            if (app.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (errorResponse.errorMessage!!.isNotEmpty()) {
                    message.append(errorResponse.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }
                if (errorResponse.code == 10) {  //Session expired
                    fragment.alert(message.toString()) {
                        okButton {
                            val intent = Intent(context, SignActivity::class.java)
                            //intent.putExtra(getString(R.string.otherActivityKey), true)
                            fragment.startActivityForResult(
                                intent,
                                fragment.resources.getInteger(R.integer.REQUEST_CODE_LOGIN_FROM_PUBLISH)
                            )
                        }
                    }.show()
                } else {
                    context.toast(message).show()
//                    fragment.alert(message.toString()) {
//                        okButton { }
//                    }.show()
                }
            } else {
                fragment.alert(context.getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }

        }

        fun getYearsBetweenTwoCalendars(a: Calendar, b: Calendar): Int {
            var diff = b[Calendar.YEAR] - a[Calendar.YEAR]
            if (a[Calendar.MONTH] > b[Calendar.MONTH] ||
                a[Calendar.MONTH] == b[Calendar.MONTH] && a[Calendar.DATE] > b[Calendar.DATE]
            ) {
                diff--
            }
            return diff
        }

        fun calculateAge(birth: Calendar): Int {
            return getYearsBetweenTwoCalendars(birth, Calendar.getInstance())
        }

        fun calculateAge(timestampBirth: Long): Int {
            val calendarBirth = Calendar.getInstance()
            calendarBirth.timeInMillis = timestampBirth
            return getYearsBetweenTwoCalendars(calendarBirth, Calendar.getInstance())
        }

        fun createPopupBalloon(context: Context,s: String): Balloon {
            return Balloon.Builder(context).apply{
                setArrowSize(16)
                setWidth(200)
                setHeight(120)
                setArrowPosition(0.9f)
                setArrowOrientation(ArrowOrientation.TOP)
                setCornerRadius(4f)
                setAlpha(1.0f)
                setPadding(16)
                setMarginRight(4)
                setText(s)
                setTextTypeface(Typeface.DEFAULT_BOLD)
                setElevation(8)
                setTextColorResource(R.color.black)
                setBackgroundColorResource(R.color.white)
                setBalloonAnimation(BalloonAnimation.FADE)
                setLifecycleOwner(lifecycleOwner)
                setOverlayColorResource(R.color.black60)
                isVisibleOverlay = true
                setDismissWhenOverlayClicked(true)
            }.build()
        }

        fun getLocalPriceTiers(context: Context) : HashMap<String,String>{
            val stringArray: Array<String> = context.resources.getStringArray(R.array.cast_tiers)
            val outputArray = HashMap<String,String>()
            for (entry in stringArray) {
                val splitResult = entry.split("\\|".toRegex(), 2).toTypedArray()
                outputArray[splitResult[0]] = splitResult[1]
            }
            return outputArray
        }


        fun getFeedDuration(duration: Duration): String {
            return if (duration.toMinutes() > 0) {
                "${duration.toMinutes()}m ${duration.minusMinutes(duration.toMinutes()).seconds}s"
            } else {
                "${duration.minusMinutes(duration.toMinutes()).seconds}s"
            }
        }

        private fun getTextColorByBackground(it: String): Int {
            return if (it == "#FFC550") R.color.black else R.color.white
        }

        fun handleColorFeed(
            cast: CastUIModel,
            colorFeedText: TextView,
            context: Context,
        ) {
            if (cast.imageLinks?.large == null) {
                cast.colorCode?.let {
                    colorFeedText.setBackgroundColor(Color.parseColor(it))
                    colorFeedText.setTextColor(ContextCompat.getColor(context,
                        getTextColorByBackground(it)))
                    colorFeedText.visibility = View.VISIBLE
                    colorFeedText.text = cast.title
                }
            } else {
                colorFeedText.visibility = View.GONE
            }
        }


    }
}
