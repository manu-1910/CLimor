package com.limor.app.audio.wav

import android.media.AudioFormat
import android.media.AudioRecord
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class WavHelper {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val NUM_CHANNELS = 2
        private const val BYTES_PER_SAMPLE = 2
        private const val BITS_PER_SAMPLE = BYTES_PER_SAMPLE * 8

        fun combineWaveFile(
            file1: String,
            file2: String,
            outPutFile: String,
            skipFirst: Boolean,
            skipSecond: Boolean
        ): Boolean {
            val aux = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
//            val bufferSize = aux * 3
            val bufferSize = SAMPLE_RATE * NUM_CHANNELS * BYTES_PER_SAMPLE
            val in1: FileInputStream
            val in2: FileInputStream
            val out: FileOutputStream
            val totalAudioLen: Long
            val totalDataLen: Long
            val longSampleRate = SAMPLE_RATE.toLong()
            val sizein1: Long
            val sizein2: Long
            val byteRate = BYTES_PER_SAMPLE * SAMPLE_RATE * NUM_CHANNELS.toLong()
            println("--------- byterate is: $byteRate")

            val data = ByteArray(bufferSize)
            //byte[] data = new byte[4096];
            try {
                in1 = FileInputStream(file1)
                in2 = FileInputStream(file2)
                out = FileOutputStream(outPutFile)
                sizein1 = in1.channel.size()
                sizein2 = in2.channel.size()
                totalAudioLen = sizein1 + sizein2
                totalDataLen = totalAudioLen + 36
                writeWaveFileHeader(
                    out,
                    totalAudioLen,
                    totalDataLen,
                    longSampleRate,
                    NUM_CHANNELS,
                    byteRate,
                    BITS_PER_SAMPLE
                )

                //Skip the blip noise at start of the first audio file
                if (skipFirst) {
                    in1.skip(44)
                    println("Skipping 44 in first file")
                }
                //***********************************************
                while (in1.read(data) != -1) {
                    out.write(data)
                }

                //Skip the blip noise at start of the second audio file
                if (skipSecond) {
                    in2.skip(44)
                    println("Skipping 44 in second file")
                }
                //***********************************************
                while (in2.read(data) != -1) {
                    out.write(data)
                }
                out.close()
                in1.close()
                in2.close()
                out.close()
                out.flush()
                println("Done")
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            return true
        }

        @Throws(IOException::class)
        fun writeWaveFileHeader(
            out: FileOutputStream,
            totalAudioLen: Long,
            totalDataLen: Long,
            longSampleRate: Long,
            channels: Int,
            byteRate: Long,
            RECORDER_BPP: Int
        ) {
            val header = ByteArray(44)
            header[0] = 'R'.toByte()
            header[1] = 'I'.toByte()
            header[2] = 'F'.toByte()
            header[3] = 'F'.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = (totalDataLen shr 8 and 0xff).toByte()
            header[6] = (totalDataLen shr 16 and 0xff).toByte()
            header[7] = (totalDataLen shr 24 and 0xff).toByte()
            header[8] = 'W'.toByte()
            header[9] = 'A'.toByte()
            header[10] = 'V'.toByte()
            header[11] = 'E'.toByte()
            header[12] = 'f'.toByte()
            header[13] = 'm'.toByte()
            header[14] = 't'.toByte()
            header[15] = ' '.toByte()
            header[16] = 16
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (longSampleRate and 0xff).toByte()
            header[25] = (longSampleRate shr 8 and 0xff).toByte()
            header[26] = (longSampleRate shr 16 and 0xff).toByte()
            header[27] = (longSampleRate shr 24 and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = (byteRate shr 8 and 0xff).toByte()
            header[30] = (byteRate shr 16 and 0xff).toByte()
            header[31] = (byteRate shr 24 and 0xff).toByte()
            header[32] = (2 * 16 / 8).toByte()
            header[33] = 0
            header[34] = RECORDER_BPP.toByte()
            header[35] = 0
            header[36] = 'd'.toByte()
            header[37] = 'a'.toByte()
            header[38] = 't'.toByte()
            header[39] = 'a'.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = (totalAudioLen shr 8 and 0xff).toByte()
            header[42] = (totalAudioLen shr 16 and 0xff).toByte()
            header[43] = (totalAudioLen shr 24 and 0xff).toByte()
            out.write(header, 0, 44)
        }
    }


}
