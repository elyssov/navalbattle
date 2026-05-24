package com.elyssov.navalbattle.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.game.CapitalChoice
import com.elyssov.navalbattle.game.FieldSize
import com.elyssov.navalbattle.game.GameMode
import com.elyssov.navalbattle.game.GameSettings
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.game.Landscape
import com.elyssov.navalbattle.game.Screen

@Composable
fun SettingsScreen(vm: GameViewModel) {
    val current by vm.settings.collectAsState()
    // Local working copy — changes don't touch the VM until Apply.
    var draft by remember { mutableStateOf(current) }

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
        )

        Section(stringResource(R.string.settings_field_size)) {
            OptionList(
                listOf(
                    stringResource(R.string.field_30) to FieldSize.Skirmish,
                    stringResource(R.string.field_50) to FieldSize.Squadron,
                    stringResource(R.string.field_100) to FieldSize.Admiral
                ),
                draft.fieldSize
            ) { f -> draft = draft.copy(fieldSize = f) }
        }

        Section(stringResource(R.string.settings_landscape)) {
            OptionList(
                listOf(
                    stringResource(R.string.landscape_ocean) to Landscape.Ocean,
                    stringResource(R.string.landscape_wrecks) to Landscape.Wrecks,
                    stringResource(R.string.landscape_islands) to Landscape.Islands,
                    stringResource(R.string.landscape_archipelago) to Landscape.Archipelago
                ),
                draft.landscape
            ) { l -> draft = draft.copy(landscape = l) }
        }

        Section(stringResource(R.string.settings_game_mode)) {
            OptionList(
                listOf(
                    stringResource(R.string.mode_ai) to GameMode.Ai,
                    stringResource(R.string.mode_hotseat) to GameMode.Hotseat
                ),
                draft.mode
            ) { m -> draft = draft.copy(mode = m) }
        }

        Section(stringResource(R.string.settings_capital_ship)) {
            OptionList(
                listOf(
                    stringResource(R.string.ship_tarkr) to CapitalChoice.Tarkr,
                    stringResource(R.string.ship_carrier) to CapitalChoice.Carrier
                ),
                draft.capital
            ) { c -> draft = draft.copy(capital = c) }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { vm.navigate(Screen.Menu) },
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text(stringResource(R.string.settings_cancel)) }

            Button(
                onClick = {
                    vm.updateSettings { draft }
                    vm.navigate(Screen.Menu)
                },
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text(stringResource(R.string.settings_apply), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
    content()
}

@Composable
private fun <T> OptionList(items: List<Pair<String, T>>, current: T, onPick: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { (label, value) ->
            val selected = value == current
            OutlinedButton(
                onClick = { onPick(value) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(if (selected) "● $label" else "○ $label")
                }
            }
        }
    }
}
