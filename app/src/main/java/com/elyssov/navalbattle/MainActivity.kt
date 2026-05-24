package com.elyssov.navalbattle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.ui.screens.AppNav
import com.elyssov.navalbattle.ui.theme.NavalBattleTheme
import com.elyssov.navalbattle.ui.theme.SeaBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavalBattleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SeaBackground
                ) {
                    val vm: GameViewModel = viewModel()
                    AppNav(vm)
                }
            }
        }
    }
}
