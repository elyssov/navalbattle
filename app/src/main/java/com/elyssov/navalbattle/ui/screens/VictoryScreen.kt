package com.elyssov.navalbattle.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.ui.theme.HitRed
import com.elyssov.navalbattle.ui.theme.RadarGreen

@Composable
fun VictoryScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val gs = state ?: return
    val playerWon = gs.winner == 0
    var showTip by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(if (playerWon) R.string.victory_title else R.string.defeat_title),
            style = MaterialTheme.typography.displayLarge,
            color = if (playerWon) RadarGreen else HitRed
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Turns: ${gs.turn}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        val sunkEnemy = gs.players[1].ships.count { it.sunk }
        val sunkUs = gs.players[0].ships.count { it.sunk }
        Text("Enemy fleet losses: $sunkEnemy", color = MaterialTheme.colorScheme.onSurface)
        Text("Our fleet losses: $sunkUs", color = MaterialTheme.colorScheme.onSurface)

        Spacer(Modifier.height(48.dp))

        Column(modifier = Modifier.width(260.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { vm.backToMenu() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text(stringResource(R.string.victory_continue)) }
        }
    }
}
