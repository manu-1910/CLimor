package com.limor.app.scenes.auth_new

import android.text.format.DateFormat
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import timber.log.Timber
import java.util.*

abstract class DobPicker {
    abstract fun onDatePicked(dateMills: Long)

    fun startMaterialPicker(fragmentManager: FragmentManager) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))

        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.JANUARY
        calendar[Calendar.YEAR] = 1900
        val jan1990 = calendar.timeInMillis

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
//                .setStart(jan1990)
//                .setEnd(today)
        val picker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setCalendarConstraints(constraintsBuilder.build())
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        picker.addOnPositiveButtonClickListener {
            Timber.d("Picker positive $it")
//            val dateParsed = parseDate(it)
            onDatePicked(it)
        }
        picker.addOnNegativeButtonClickListener {
            Timber.d("Picker negative")
        }
        picker.addOnCancelListener {
            Timber.d("Picker onCancel")
        }
        picker.addOnDismissListener {
            Timber.d("Picker onDismiss")
        }
        picker.show(fragmentManager, "DobPicker")
    }

    companion object {
        fun parseDate(mills: Long) : String{
            return DateFormat.format("dd MMM, yyyy ", Date(mills)).toString()
        }
    }
}