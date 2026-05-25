package com.elyssov.navalbattle

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elyssov.navalbattle.audio.AudioEngine
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.ui.audio.LocalAudio
import com.elyssov.navalbattle.ui.audio.RealAudio
import com.elyssov.navalbattle.ui.screens.AppNav
import com.elyssov.navalbattle.ui.theme.NavalBattleTheme
import com.elyssov.navalbattle.ui.theme.SeaBackground

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavalBattleTheme {
                val ctx = LocalContext.current
                val engine = remember { AudioEngine(ctx) }
                DisposableEffect(engine) { onDispose { engine.release() } }
                val audio = remember { RealAudio(engine) }

                CompositionLocalProvider(LocalAudio provides audio) {
                    Surface(modifier = Modifier.fillMaxSize(), color = SeaBackground) {
                        val vm: GameViewModel = viewModel()
                        AppNav(vm)
                    }
                }
            }
        }
    }
}
