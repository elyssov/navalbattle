package com.elyssov.navalbattle.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.game.Screen

@Composable
fun MenuScreen(vm: GameViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.menu_title),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.tertiary
        )
        Text(
            stringResource(R.string.menu_subtitle),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(48.dp))

        Column(
            modifier = Modifier.width(260.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { vm.startNewGame() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text(stringResource(R.string.menu_new_game)) }

            Button(
                onClick = { vm.navigate(Screen.Settings) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text(stringResource(R.string.menu_settings)) }
        }
    }
}
