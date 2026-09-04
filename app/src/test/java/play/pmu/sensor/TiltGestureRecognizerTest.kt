package play.pmu.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TiltGestureRecognizerTest {

    private val recognizer = TiltGestureRecognizer()

    @Test
    fun `nagib napred daje pogodak`() {
        assertEquals(TiltGesture.CORRECT, recognizer.onSensorValue(z = 8f, nowMillis = 1_000))
    }

    @Test
    fun `nagib nazad daje preskakanje`() {
        assertEquals(TiltGesture.SKIP, recognizer.onSensorValue(z = -8f, nowMillis = 1_000))
    }

    @Test
    fun `neutralan polozaj ne prijavljuje pokret`() {
        assertNull(recognizer.onSensorValue(z = 0f, nowMillis = 1_000))
    }

    @Test
    fun `jedan nagib prijavljuje tacno jedan pokret`() {
        assertEquals(TiltGesture.CORRECT, recognizer.onSensorValue(z = 8f, nowMillis = 1_000))

        // Telefon je jos nagnut: dalje vrednosti iznad praga se ignorisu.
        assertNull(recognizer.onSensorValue(z = 9f, nowMillis = 1_100))
        assertNull(recognizer.onSensorValue(z = 8f, nowMillis = 1_200))
    }

    @Test
    fun `novi pokret zahteva vracanje u neutralnu zonu`() {
        assertEquals(TiltGesture.CORRECT, recognizer.onSensorValue(z = 8f, nowMillis = 1_000))
        assertNull(recognizer.onSensorValue(z = 8f, nowMillis = 2_000))

        // Vracanje u neutralu ponovo "otkljucava" prepoznavanje...
        assertNull(recognizer.onSensorValue(z = 0f, nowMillis = 2_100))
        // ...pa sledeci nagib opet prolazi.
        assertEquals(TiltGesture.CORRECT, recognizer.onSensorValue(z = 8f, nowMillis = 2_200))
    }

    @Test
    fun `debounce odbacuje pokret koji dolazi prerano`() {
        assertEquals(TiltGesture.CORRECT, recognizer.onSensorValue(z = 8f, nowMillis = 1_000))
        assertNull(recognizer.onSensorValue(z = 0f, nowMillis = 1_050))

        // Manje od 500 ms od prethodnog pokreta - odbacuje se.
        assertNull(recognizer.onSensorValue(z = 8f, nowMillis = 1_100))
    }

    @Test
    fun `suprotan nagib posle neutrale daje suprotan pokret`() {
        assertEquals(TiltGesture.CORRECT, recognizer.onSensorValue(z = 8f, nowMillis = 1_000))
        assertNull(recognizer.onSensorValue(z = 0f, nowMillis = 1_600))
        assertEquals(TiltGesture.SKIP, recognizer.onSensorValue(z = -8f, nowMillis = 1_700))
    }
}
