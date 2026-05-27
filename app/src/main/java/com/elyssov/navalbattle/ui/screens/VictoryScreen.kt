package com.elyssov.navalbattle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.ui.theme.HitRed
import com.elyssov.navalbattle.ui.theme.RadarGreen
import com.elyssov.navalbattle.ui.theme.SeaBackground

@Composable
fun VictoryScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val gs = state ?: return
    val playerWon = gs.winner == 0

    Box(modifier = Modifier.fillMaxSize().background(SeaBackground)) {
        Image(
            painter = painterResource(if (playerWon) R.drawable.bg_victory else R.drawable.bg_defeat),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.6f)
        )
        Box(modifier = Modifier.fillMaxSize().background(SeaBackground.copy(alpha = 0.4f)))

        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(if (playerWon) R.string.victory_title else R.string.defeat_title),
                style = MaterialTheme.typography.displayLarge,
                color = if (playerWon) RadarGreen else HitRed,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(20.dp))
            Text("Turns: ${gs.turn}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
            val sunkEnemy = gs.players[1].ships.count { it.sunk }
            val sunkUs = gs.players[0].ships.count { it.sunk }
            Text("Enemy fleet losses: $sunkEnemy", color = MaterialTheme.colorScheme.onSurface)
            Text("Our fleet losses: $sunkUs", color = MaterialTheme.colorScheme.onSurface)

            Spacer(Modifier.height(40.dp))
            Button(
                onClick = { vm.backToMenu() },
                modifier = Modifier.width(260.dp).height(52.dp)
            ) { Text(stringResource(R.string.victory_continue), fontWeight = FontWeight.Bold) }
        }
    }
}
