package com.elyssov.navalbattle.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import kotlin.math.PI
import kotlin.math.sin

enum class Sfx { Shot, Hit, Miss, Sunk, PcrLaunch, PcrIntercept, Torpedo, Striker, Radar, Reactor, NukeLaunch, NukeBoom, Click }

class AudioEngine(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val streams: Map<Sfx, Int>
    private var muted = false

    init {
        // Generate procedural sounds (no asset files required)
        streams = mapOf(
            Sfx.Shot to loadGenerated(0.12, 220.0, 80.0, decay = 0.7),
            Sfx.Hit to loadGenerated(0.20, 110.0, 60.0, decay = 0.5, noise = 0.6),
            Sfx.Miss to loadGenerated(0.18, 600.0, 300.0, decay = 0.4, noise = 0.4),
            Sfx.Sunk to loadGenerated(0.50, 80.0, 30.0, decay = 0.3, noise = 0.7),
            Sfx.PcrLaunch to loadGenerated(0.35, 180.0, 320.0, decay = 0.85, noise = 0.5),
            Sfx.PcrIntercept to loadGenerated(0.22, 800.0, 200.0, decay = 0.5, noise = 0.3),
            Sfx.Torpedo to loadGenerated(0.40, 90.0, 50.0, decay = 0.8, noise = 0.6),
            Sfx.Striker to loadGenerated(0.30, 300.0, 600.0, decay = 0.7, noise = 0.4),
            Sfx.Radar to loadGenerated(0.18, 1200.0, 1200.0, decay = 0.4),
            Sfx.Reactor to loadGenerated(0.60, 60.0, 25.0, decay = 0.4, noise = 0.8),
            Sfx.NukeLaunch to loadGenerated(0.45, 150.0, 380.0, decay = 0.9, noise = 0.5),
            Sfx.NukeBoom to loadGenerated(0.90, 50.0, 20.0, decay = 0.3, noise = 0.9),
            Sfx.Click to loadGenerated(0.04, 1000.0, 1000.0, decay = 0.2)
        )
    }

    fun play(s: Sfx, volume: Float = 0.7f) {
        if (muted) return
        val id = streams[s] ?: return
        pool.play(id, volume, volume, 1, 0, 1f)
    }

    fun setMuted(m: Boolean) { muted = m }

    fun release() { pool.release() }

    private fun loadGenerated(
        duration: Double, freqStart: Double, freqEnd: Double,
        decay: Double = 0.5, noise: Double = 0.0
    ): Int {
        val sampleRate = 22050
        val samples = (sampleRate * duration).toInt()
        val data = ShortArray(samples)
        var phase = 0.0
        val rng = java.util.Random()
        for (i in 0 until samples) {
            val t = i.toDouble() / samples
            val freq = freqStart + (freqEnd - freqStart) * t
            phase += 2.0 * PI * freq / sampleRate
            val env = Math.pow(1.0 - t, decay)
            val tone = sin(phase) * (1.0 - noise)
            val noiseVal = (rng.nextDouble() * 2 - 1) * noise
            val sample = (tone + noiseVal) * env * Short.MAX_VALUE * 0.7
            data[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return loadPcmIntoPool(data, sampleRate)
    }

    private fun loadPcmIntoPool(data: ShortArray, sampleRate: Int): Int {
        // SoundPool doesn't load from memory; we play via AudioTrack and pre-cache won't work.
        // Workaround: write WAV bytes to a temp file, load that into SoundPool.
        val wav = pcmToWav(data, sampleRate)
        val tmp = java.io.File.createTempFile("nb_sfx_", ".wav").apply { writeBytes(wav); deleteOnExit() }
        return pool.load(tmp.absolutePath, 1)
    }

    private fun pcmToWav(data: ShortArray, sampleRate: Int): ByteArray {
        val dataBytes = ByteArray(data.size * 2)
        for (i in data.indices) {
            val v = data[i].toInt()
            dataBytes[2 * i] = (v and 0xff).toByte()
            dataBytes[2 * i + 1] = ((v shr 8) and 0xff).toByte()
        }
        val totalLen = 36 + dataBytes.size
        val byteRate = sampleRate * 2 // mono 16-bit
        val header = ByteArray(44)
        // RIFF chunk
        "RIFF".toByteArray().copyInto(header, 0)
        intToLe(totalLen, header, 4)
        "WAVE".toByteArray().copyInto(header, 8)
        "fmt ".toByteArray().copyInto(header, 12)
        intToLe(16, header, 16)
        shortToLe(1, header, 20)            // PCM
        shortToLe(1, header, 22)            // channels
        intToLe(sampleRate, header, 24)
        intToLe(byteRate, header, 28)
        shortToLe(2, header, 32)            // block align
        shortToLe(16, header, 34)           // bits per sample
        "data".toByteArray().copyInto(header, 36)
        intToLe(dataBytes.size, header, 40)
        return header + dataBytes
    }

    private fun intToLe(v: Int, out: ByteArray, off: Int) {
        out[off] = (v and 0xff).toByte()
        out[off + 1] = ((v shr 8) and 0xff).toByte()
        out[off + 2] = ((v shr 16) and 0xff).toByte()
        out[off + 3] = ((v shr 24) and 0xff).toByte()
    }

    private fun shortToLe(v: Int, out: ByteArray, off: Int) {
        out[off] = (v and 0xff).toByte()
        out[off + 1] = ((v shr 8) and 0xff).toByte()
    }
}
