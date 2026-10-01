package com.example.emulator

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class RetroAudioSynth {
    private val sampleRate = 22050
    private var isMuted = false
    private val scope = CoroutineScope(Dispatchers.Default)

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
    }

    /**
     * Plays a vintage 8-bit square wave tone at a specific frequency for a given duration.
     */
    fun playSquareTone(frequencyHz: Float, durationMs: Int, dutyCycle: Float = 0.5f) {
        if (isMuted) return
        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(100)
                val buffer = ShortArray(numSamples)
                val periodSamples = sampleRate / frequencyHz

                for (i in 0 until numSamples) {
                    val pos = (i % periodSamples) / periodSamples
                    val sample = if (pos < dutyCycle) 16000 else -16000
                    // Apply slight envelope decay to prevent clicking
                    val decay = 1.0f - (i.toFloat() / numSamples)
                    buffer[i] = (sample * decay).toInt().toShort()
                }

                writeAndPlay(buffer)
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * Plays an authentic retro sound effect by preset.
     */
    fun playSoundEffect(effect: RetroSoundEffect) {
        if (isMuted) return
        scope.launch {
            try {
                val buffer = when (effect) {
                    RetroSoundEffect.JUMP -> generateSlideTone(200f, 600f, 120)
                    RetroSoundEffect.LASER_SHOOT -> generateSlideTone(800f, 150f, 100)
                    RetroSoundEffect.EXPLOSION -> generateNoiseBurst(180)
                    RetroSoundEffect.COIN -> generateArpeggio(listOf(987.77f, 1318.51f), 150)
                    RetroSoundEffect.POWER_UP -> generateArpeggio(listOf(330f, 440f, 550f, 660f, 880f), 220)
                    RetroSoundEffect.HIT -> generateSlideTone(300f, 80f, 80)
                    RetroSoundEffect.BEEP -> generateSquareBuffer(440f, 60)
                }
                writeAndPlay(buffer)
            } catch (ignored: Exception) {
            }
        }
    }

    private fun generateSquareBuffer(freq: Float, durationMs: Int): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        val period = sampleRate / freq
        for (i in 0 until numSamples) {
            val s = if ((i % period) < period / 2) 12000 else -12000
            buffer[i] = s.toShort()
        }
        return buffer
    }

    private fun generateSlideTone(startFreq: Float, endFreq: Float, durationMs: Int): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val phaseInc = 2.0 * Math.PI * currentFreq / sampleRate
            phase += phaseInc

            val sample = if (sin(phase) > 0) 14000 else -14000
            val decay = 1.0 - progress * 0.5
            buffer[i] = (sample * decay).toInt().toShort()
        }
        return buffer
    }

    private fun generateNoiseBurst(durationMs: Int): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val decay = (1.0f - (i.toFloat() / numSamples))
            val noise = (Random.nextInt(-16000, 16000) * decay).toInt()
            buffer[i] = noise.toShort()
        }
        return buffer
    }

    private fun generateArpeggio(frequencies: List<Float>, totalDurationMs: Int): ShortArray {
        val noteDuration = totalDurationMs / frequencies.size
        val numSamplesPerNote = (sampleRate * (noteDuration / 1000f)).toInt()
        val totalSamples = numSamplesPerNote * frequencies.size
        val buffer = ShortArray(totalSamples)

        frequencies.forEachIndexed { noteIndex, freq ->
            val period = sampleRate / freq
            val startIdx = noteIndex * numSamplesPerNote
            for (i in 0 until numSamplesPerNote) {
                val s = if ((i % period) < period / 2) 14000 else -14000
                buffer[startIdx + i] = s.toShort()
            }
        }
        return buffer
    }

    private fun writeAndPlay(buffer: ShortArray) {
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
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        audioTrack.notificationMarkerPosition = buffer.size
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) {
                track?.release()
            }
            override fun onPeriodicNotification(track: AudioTrack?) {}
        })
    }

    enum class RetroSoundEffect {
        JUMP,
        LASER_SHOOT,
        EXPLOSION,
        COIN,
        POWER_UP,
        HIT,
        BEEP
    }
}
