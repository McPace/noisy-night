package com.papapace.noisynight

import java.util.Random

interface NoiseGenerator {
    fun fillBuffer(buffer: FloatArray)
}

class WhiteNoiseGenerator : NoiseGenerator {
    private val random = Random()

    override fun fillBuffer(buffer: FloatArray) {
        for (i in buffer.indices) {
            buffer[i] = random.nextFloat() * 2f - 1f
        }
    }
}

/**
 * Voss-McCartney Pink Noise Generator (1/f spectral density)
 * Uses multiple random generators running at octave-spaced frequencies.
 */
class PinkNoiseGenerator : NoiseGenerator {
    private val random = Random()
    private val numRows = 12
    private val rows = FloatArray(numRows)
    private var runningSum = 0f
    private var index = 0

    init {
        for (i in 0 until numRows) {
            rows[i] = random.nextFloat() * 2f - 1f
            runningSum += rows[i]
        }
    }

    override fun fillBuffer(buffer: FloatArray) {
        for (i in buffer.indices) {
            val lastIndex = index
            index = (index + 1) and 0x7FFFFFFF

            // Count trailing zeros to determine which generator to update
            var diff = lastIndex xor index
            var count = 0
            while (diff and 1 == 0) {
                count++
                diff = diff ushr 1
            }

            if (count < numRows) {
                runningSum -= rows[count]
                rows[count] = random.nextFloat() * 2f - 1f
                runningSum += rows[count]
            }

            val white = random.nextFloat() * 2f - 1f
            // Voss noise has a sum range of roughly [-numRows, numRows].
            // Normalize and scale for a pleasant listening volume.
            val sample = (runningSum + white) / (numRows + 1)
            buffer[i] = sample * 1.5f // scale factor to match white/pink volume
        }
    }
}

/**
 * Brown Noise Generator (1/f^2 spectral density)
 * Created by integrating white noise with a leaky/lossy high-pass integrator to prevent drift.
 */
class BrownNoiseGenerator : NoiseGenerator {
    private val random = Random()
    private var currentSample = 0f

    override fun fillBuffer(buffer: FloatArray) {
        for (i in buffer.indices) {
            val white = random.nextFloat() * 2f - 1f
            // Leaky integrator
            currentSample = 0.995f * currentSample + 0.005f * white
            // Scale up and clamp since integration dampens overall amplitude
            val sample = currentSample * 15f
            buffer[i] = sample.coerceIn(-1f, 1f)
        }
    }
}

/**
 * Green Noise Generator
 * White noise passed through a band-pass filter centered around the human ear's most sensitive
 * middle range (centered at ~800Hz, Q = 0.4) to simulate nature/rustling leaves.
 */
class GreenNoiseGenerator : NoiseGenerator {
    private val random = Random()
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f

    // Biquad bandpass filter coefficients for f0 = 800Hz, Q = 0.4, fs = 44100Hz
    private val b0 = 0.12448f
    private val b2 = -0.12448f
    private val a1 = -1.73969f
    private val a2 = 0.75105f

    override fun fillBuffer(buffer: FloatArray) {
        for (i in buffer.indices) {
            val white = random.nextFloat() * 2f - 1f
            val y = b0 * white + b2 * x2 - a1 * y1 - a2 * y2
            x2 = x1
            x1 = white
            y2 = y1
            y1 = y
            // Scale green noise to match white/pink volume and clamp
            buffer[i] = (y * 4f).coerceIn(-1f, 1f)
        }
    }
}

/**
 * Blue Noise Generator (+3dB/octave)
 * Derivative of Pink Noise (which is -3dB/octave).
 */
class BlueNoiseGenerator : NoiseGenerator {
    private val pinkGenerator = PinkNoiseGenerator()
    private var lastPink = 0f

    override fun fillBuffer(buffer: FloatArray) {
        // Generate pink noise into the buffer first
        pinkGenerator.fillBuffer(buffer)
        // Differentiate it in place to get blue noise
        for (i in buffer.indices) {
            val pink = buffer[i]
            buffer[i] = ((pink - lastPink) * 2.5f).coerceIn(-1f, 1f)
            lastPink = pink
        }
    }
}

/**
 * Purple Noise Generator (+6dB/octave)
 * Derivative of White Noise.
 */
class PurpleNoiseGenerator : NoiseGenerator {
    private val random = Random()
    private var lastWhite = 0f

    override fun fillBuffer(buffer: FloatArray) {
        for (i in buffer.indices) {
            val white = random.nextFloat() * 2f - 1f
            buffer[i] = ((white - lastWhite) * 0.7f).coerceIn(-1f, 1f)
            lastWhite = white
        }
    }
}

/**
 * Deep Brown Noise (-12dB/octave)
 * Double integration of white noise with soft clipping.
 */
class DeepBrownNoiseGenerator : NoiseGenerator {
    private val random = Random()
    private var currentSample1 = 0f
    private var currentSample2 = 0f

    override fun fillBuffer(buffer: FloatArray) {
        for (i in buffer.indices) {
            val white = random.nextFloat() * 2f - 1f
            // Two cascaded simple low-pass filters (12dB/oct)
            currentSample1 = 0.96f * currentSample1 + 0.04f * white
            currentSample2 = 0.96f * currentSample2 + 0.04f * currentSample1
            
            // Gain compensation
            var sample = currentSample2 * 12f 
            // Soft clipping to prevent harsh sputtering if it wanders too far
            sample = sample / (1f + kotlin.math.abs(sample))
            
            buffer[i] = sample
        }
    }
}

/**
 * Amber Noise (-4.5dB/octave approx)
 * Sits between Pink and Brown noise. Warm and soothing.
 */
class AmberNoiseGenerator : NoiseGenerator {
    private val pinkGenerator = PinkNoiseGenerator()
    private var lastSample = 0f

    override fun fillBuffer(buffer: FloatArray) {
        pinkGenerator.fillBuffer(buffer)
        for (i in buffer.indices) {
            val pink = buffer[i]
            // Gentle low-pass filter on pink noise
            lastSample = 0.8f * lastSample + 0.2f * pink
            buffer[i] = (lastSample * 2f).coerceIn(-1f, 1f)
        }
    }
}

class OceanWavesGenerator : NoiseGenerator {
    private val pinkGenerator = PinkNoiseGenerator()
    private var phase = 0.0
    
    override fun fillBuffer(buffer: FloatArray) {
        pinkGenerator.fillBuffer(buffer)
        // 11 second period to perfectly loop in the 11 second static buffer
        val lfoRate = 2.0 * Math.PI / (NoiseAudioService.SAMPLE_RATE * 11.0) 
        for (i in buffer.indices) {
            phase += lfoRate
            // Sine wave from 0 to 1
            val lfo = (Math.sin(phase).toFloat() + 1f) / 2f
            // Deep, swelling wave volume
            val volume = 0.05f + 0.95f * (lfo * lfo) 
            buffer[i] = buffer[i] * volume * 2f
        }
    }
}

class WindStormGenerator : NoiseGenerator {
    private val pinkGenerator = PinkNoiseGenerator()
    private var phase1 = 0.0
    private var phase2 = 0.0
    private var lpfState = 0f
    
    override fun fillBuffer(buffer: FloatArray) {
        // Needs a full buffer of pink noise
        pinkGenerator.fillBuffer(buffer)
        val lfoRate1 = 2.0 * Math.PI / (NoiseAudioService.SAMPLE_RATE * 11.0) // 11s period
        val lfoRate2 = 2.0 * Math.PI / (NoiseAudioService.SAMPLE_RATE * 5.5)  // 5.5s period
        
        for (i in buffer.indices) {
            phase1 += lfoRate1
            phase2 += lfoRate2
            val lfo1 = (Math.sin(phase1).toFloat() + 1f) / 2f
            val lfo2 = (Math.sin(phase2).toFloat() + 1f) / 2f
            
            val mixLfo = (lfo1 * 0.7f) + (lfo2 * 0.3f)
            
            // Filter cutoff sweeps from muffled to bright
            val cutoff = 0.02f + 0.1f * mixLfo
            
            // Apply low pass filter to pink noise
            lpfState += cutoff * (buffer[i] - lpfState)
            
            // Add some howling resonance (dynamic gain)
            val volume = 1.0f + mixLfo * 1.5f
            buffer[i] = lpfState * volume * 1.5f
        }
    }
}
