package io.square1.limor

import io.square1.limor.scenes.utils.CommonsKt.Companion.calculateDurationMinutesAndSeconds
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    /*@Test
    fun calculateDurationMinutesAndSeconds_isCorrect() {
        var zero : Long= 0
        var seconds30 : Long = 30 * 1000
        var seconds59 : Long = 59 * 1000
        var seconds60 : Long = 60 * 1000
        var seconds51 : Long = 61 * 1000

        calculateDurationMinutesAndSeconds(10)
    }*/
}
