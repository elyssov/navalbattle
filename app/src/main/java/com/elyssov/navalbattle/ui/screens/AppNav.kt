package com.elyssov.navalbattle.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.game.Screen

@Composable
fun AppNav(vm: GameViewModel) {
    val screen by vm.screen.collectAsState()
    when (screen) {
        Screen.Menu -> MenuScreen(vm)
        Screen.Settings -> SettingsScreen(vm)
        Screen.Placement -> PlacementScreen(vm)
        Screen.Battle -> BattleScreen(vm)
        Screen.NukeRitual -> NukeRitualScreen(vm)
        Screen.Victory -> VictoryScreen(vm)
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(label)
    }
}
