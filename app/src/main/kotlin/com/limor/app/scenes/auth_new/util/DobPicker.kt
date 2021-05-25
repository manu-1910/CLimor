package com.limor.app.scenes.auth_new.util

import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.limor.app.R
import timber.log.Timber

abstract class DobPicker {
    abstract fun onDatePicked(dateMills: Long)

    fun startMaterialPicker(fragmentManager: FragmentManager, dateMills: Long) {
        val currentDate = if (dateMills != 0L) dateMills else MaterialDatePicker.todayInUtcMilliseconds()
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .setOpenAt(currentDate)
        val picker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_date)
                .setCalendarConstraints(constraintsBuilder.build())
                .setSelection(currentDate)
                .build()

        picker.addOnPositiveButtonClickListener {
            Timber.d("Picker positive $it")
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
}