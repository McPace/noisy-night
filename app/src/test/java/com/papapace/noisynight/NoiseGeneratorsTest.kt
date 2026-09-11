package com.papapace.noisynight

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoiseGeneratorsTest {

    @Test
    fun testWhiteNoiseGenerator() {
        val generator = WhiteNoiseGenerator()
        val buffer = FloatArray(1000)
        generator.fillBuffer(buffer)

        var nonZeroCount = 0
        for (sample in buffer) {
            assertTrue("White noise out of range: $sample", sample >= -1.0f && sample <= 1.0f)
            if (sample != 0.0f) {
                nonZeroCount++
            }
        }
        assertTrue("White noise buffer is mostly empty", nonZeroCount > 950)
        assertNotEquals("Consecutive samples are identical (not random)", buffer[0], buffer[1])
    }

    @Test
    fun testPinkNoiseGenerator() {
        val generator = PinkNoiseGenerator()
        val buffer = FloatArray(1000)
        generator.fillBuffer(buffer)

        var nonZeroCount = 0
        for (sample in buffer) {
            assertTrue("Pink noise out of range: $sample", sample >= -1.0f && sample <= 1.0f)
            if (sample != 0.0f) {
                nonZeroCount++
            }
        }
        assertTrue("Pink noise buffer is mostly empty", nonZeroCount > 950)
        assertNotEquals("Consecutive samples are identical", buffer[0], buffer[1])
    }

    @Test
    fun testBrownNoiseGenerator() {
        val generator = BrownNoiseGenerator()
        val buffer = FloatArray(1000)
        generator.fillBuffer(buffer)

        var nonZeroCount = 0
        for (sample in buffer) {
            assertTrue("Brown noise out of range: $sample", sample >= -1.0f && sample <= 1.0f)
            if (sample != 0.0f) {
                nonZeroCount++
            }
        }
        assertTrue("Brown noise buffer is mostly empty", nonZeroCount > 950)
        assertNotEquals("Consecutive samples are identical", buffer[0], buffer[1])
    }

    @Test
    fun testGreenNoiseGenerator() {
        val generator = GreenNoiseGenerator()
        val buffer = FloatArray(1000)
        generator.fillBuffer(buffer)

        var nonZeroCount = 0
        for (sample in buffer) {
            assertTrue("Green noise out of range: $sample", sample >= -1.0f && sample <= 1.0f)
            if (sample != 0.0f) {
                nonZeroCount++
            }
        }
        assertTrue("Green noise buffer is mostly empty", nonZeroCount > 950)
        assertNotEquals("Consecutive samples are identical", buffer[0], buffer[1])
    }
}
