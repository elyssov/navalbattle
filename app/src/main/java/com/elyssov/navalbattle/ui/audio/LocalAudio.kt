package com.elyssov.navalbattle.ui.audio

import androidx.compose.runtime.staticCompositionLocalOf
import com.elyssov.navalbattle.audio.AudioEngine
import com.elyssov.navalbattle.audio.Sfx

object NoopAudio {
    fun play(@Suppress("UNUSED_PARAMETER") s: Sfx, @Suppress("UNUSED_PARAMETER") volume: Float = 0.7f) {}
}

interface AudioApi {
    fun play(s: Sfx, volume: Float = 0.7f)
}

class RealAudio(private val engine: AudioEngine) : AudioApi {
    override fun play(s: Sfx, volume: Float) = engine.play(s, volume)
}

class SilentAudio : AudioApi {
    override fun play(s: Sfx, volume: Float) {}
}

val LocalAudio = staticCompositionLocalOf<AudioApi> { SilentAudio() }
