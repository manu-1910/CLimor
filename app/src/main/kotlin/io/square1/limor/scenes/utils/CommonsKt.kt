package io.square1.limor.scenes.utils

import android.os.Build
import android.text.Editable
import io.square1.limor.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


class CommonsKt{

    companion object{

        val audioFileFormat: String = ".wav"

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
                    finalDuration = String.format("%1$02d:%1$02d", minutes, sec)
                }
            }
            return finalDuration
        }

        fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    }

}
