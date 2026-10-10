package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(context: Context) {
    @Suppress("DEPRECATION")
    private val vibrator: Vibrator? = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val audioScope = CoroutineScope(Dispatchers.Default)

    fun playLaserSound() {
        vibrate(30)
        audioScope.launch {
            generateTone(
                startFreq = 880f,
                endFreq = 220f,
                durationMs = 90,
                volume = 0.4f
            )
        }
    }

    fun playMissileSound() {
        vibrate(80)
        audioScope.launch {
            generateTone(
                startFreq = 300f,
                endFreq = 100f,
                durationMs = 300,
                volume = 0.6f
            )
        }
    }

    fun playExplosionSound() {
        vibrateHeavy()
        audioScope.launch {
            generateNoise(
                durationMs = 350,
                volume = 0.7f
            )
        }
    }

    fun playShieldHitSound() {
        vibrate(50)
        audioScope.launch {
            generateTone(
                startFreq = 1500f,
                endFreq = 400f,
                durationMs = 120,
                volume = 0.5f
            )
        }
    }

    fun playLockOnBeep() {
        vibrate(20)
        audioScope.launch {
            generateTone(
                startFreq = 1200f,
                endFreq = 1200f,
                durationMs = 60,
                volume = 0.3f
            )
        }
    }

    private fun generateTone(startFreq: Float, endFreq: Float, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val samples = ShortArray(numSamples)

            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val phaseIncrement = 2.0 * Math.PI * currentFreq / sampleRate
                phase += phaseIncrement
                val value = (sin(phase) * 32767 * volume * (1f - progress)).toInt()
                samples[i] = value.coerceIn(-32768, 32767).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, numSamples)
            audioTrack.play()
            audioScope.launch {
                kotlinx.coroutines.delay(durationMs + 50L)
                audioTrack.release()
            }
        } catch (e: Exception) {
            // Graceful fallback
        }
    }

    private fun generateNoise(durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val samples = ShortArray(numSamples)

            var lastSample = 0f
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val whiteNoise = (Math.random() * 2.0 - 1.0).toFloat()
                lastSample = (lastSample + (0.02f * whiteNoise)) / 1.02f
                val sampleValue = (lastSample * 32767 * volume * (1f - progress)).toInt()
                samples[i] = sampleValue.coerceIn(-32768, 32767).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, numSamples)
            audioTrack.play()
            audioScope.launch {
                kotlinx.coroutines.delay(durationMs + 50L)
                audioTrack.release()
            }
        } catch (e: Exception) {
            // Fallback
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun vibrateHeavy() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 80, 40, 120)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
