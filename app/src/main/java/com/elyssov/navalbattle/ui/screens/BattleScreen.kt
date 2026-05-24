package com.elyssov.navalbattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.game.LogKind
import com.elyssov.navalbattle.game.ShipType
import com.elyssov.navalbattle.game.TerrainKind
import com.elyssov.navalbattle.ui.components.FleetGrid
import com.elyssov.navalbattle.ui.components.drawGridLines
import com.elyssov.navalbattle.ui.components.fillCell
import com.elyssov.navalbattle.ui.components.rememberGridCamera
import com.elyssov.navalbattle.ui.components.strokeCell
import com.elyssov.navalbattle.ui.theme.HitRed
import com.elyssov.navalbattle.ui.theme.IslandGreen
import com.elyssov.navalbattle.ui.theme.MissWhite
import com.elyssov.navalbattle.ui.theme.OceanBlue
import com.elyssov.navalbattle.ui.theme.OceanShallow
import com.elyssov.navalbattle.ui.theme.RadarAmber
import com.elyssov.navalbattle.ui.theme.RadarGreen
import com.elyssov.navalbattle.ui.theme.SeaBackground
import com.elyssov.navalbattle.ui.theme.SeaSurface
import com.elyssov.navalbattle.ui.theme.SunkBlack
import com.elyssov.navalbattle.ui.theme.WreckBrown

enum class BattleView { FleetMap, Radar }

@Composable
fun BattleScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val lang by vm.lang.collectAsState()
    val gs = state ?: return
    val n = gs.settings.fieldSize.n

    val me = 0
    val enemy = 1

    var view by remember { mutableStateOf(BattleView.Radar) }
    val cameraFleet = rememberGridCamera(n)
    val cameraRadar = rememberGridCamera(n)
    val nuclearTargeting by vm.nuclearTargeting.collectAsState()
    val nukeUsed = gs.players[me].nuclearUsed
    val nukeAvailable = !nukeUsed && gs.settings.fieldSize != com.elyssov.navalbattle.game.FieldSize.Skirmish

    val shipColor = MaterialTheme.colorScheme.primary
    val islandC = IslandGreen
    val wreckC = WreckBrown
    val gridC = OceanShallow.copy(alpha = 0.4f)
    val radarGridC = RadarGreen.copy(alpha = 0.3f)
    val hitC = HitRed
    val missC = MissWhite
    val sunkC = SunkBlack
    val reconC = RadarAmber

    Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.battle_turn, gs.turn),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.weight(1f))
            Text(
                if (gs.currentPlayer == 0) "Your turn" else "AI thinking...",
                color = if (gs.currentPlayer == 0) RadarGreen else RadarAmber
            )
        }

        // Tab switch
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TabButton(stringResource(R.string.battle_fleet_map), view == BattleView.FleetMap, Modifier.weight(1f)) {
                view = BattleView.FleetMap
            }
            TabButton(stringResource(R.string.battle_radar), view == BattleView.Radar, Modifier.weight(1f)) {
                view = BattleView.Radar
            }
        }

        // Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.tertiary)
        ) {
            when (view) {
                BattleView.FleetMap -> {
                    FleetGrid(camera = cameraFleet, background = OceanBlue) { cam ->
                        gs.terrain.forEach {
                            fillCell(cam, it.coord, if (it.kind == TerrainKind.Island) islandC else wreckC)
                        }
                        gs.players[me].ships.filter { it.placed }.forEach { ship ->
                            ship.cells.forEachIndexed { idx, c ->
                                val color = if (ship.sunk) sunkC else if (ship.hits[idx]) hitC else shipColor
                                fillCell(cam, c, color)
                            }
                        }
                        // Enemy shots at us
                        gs.players[enemy].shotsAtEnemy.forEach { shot ->
                            if (!shot.hit) {
                                strokeCell(cam, shot.coord, missC.copy(alpha = 0.6f), 1f)
                            }
                        }
                        drawGridLines(cam, gridC)
                    }
                }
                BattleView.Radar -> {
                    FleetGrid(
                        camera = cameraRadar,
                        background = SeaBackground,
                        onCellTap = { c ->
                            if (gs.currentPlayer == me && c.x in 0 until n && c.y in 0 until n) {
                                if (nuclearTargeting) vm.launchNuke(c) else vm.fireShot(c)
                            }
                        }
                    ) { cam ->
                        // Show only islands on radar (wrecks invisible)
                        gs.terrain.filter { it.kind == TerrainKind.Island }
                            .forEach { fillCell(cam, it.coord, islandC.copy(alpha = 0.5f)) }
                        // Our shots
                        gs.players[me].shotsAtEnemy.forEach { shot ->
                            fillCell(cam, shot.coord, if (shot.hit) hitC.copy(alpha = 0.7f) else missC.copy(alpha = 0.4f))
                        }
                        // Sunk enemy ships visible
                        gs.players[enemy].ships.filter { it.sunk }.forEach { ship ->
                            ship.cells.forEach { fillCell(cam, it, sunkC) }
                        }
                        // Recon hits (aging visualization)
                        gs.players[me].reconHits.forEach { hit ->
                            val age = gs.turn - hit.turnSeen
                            val alpha = when (age) { 0 -> 0.9f; 1 -> 0.6f; 2 -> 0.4f; else -> 0.2f }
                            strokeCell(cam, hit.coord, reconC.copy(alpha = alpha), 2f)
                        }
                        // Nuclear contamination zones
                        gs.nuclearZones.forEach { z ->
                            val half = 5
                            for (dx in -half..half) for (dy in -half..half) {
                                val cz = com.elyssov.navalbattle.game.Coord(z.center.x + dx, z.center.y + dy)
                                if (cz.x in 0 until n && cz.y in 0 until n) {
                                    fillCell(cam, cz, NukeColor.copy(alpha = 0.15f))
                                }
                            }
                        }
                        drawGridLines(cam, radarGridC)
                    }
                }
            }
        }

        if (nuclearTargeting) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    .background(NukeColor.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                Text(
                    "Σ☢ SELECT EPICENTER ON RADAR",
                    color = NukeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Action bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { vm.startNukeRitual() },
                enabled = gs.currentPlayer == me && nukeAvailable && !nuclearTargeting,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NukeColor)
            ) { Text("☢", fontWeight = FontWeight.Bold) }
            Button(
                onClick = { vm.endTurn() },
                enabled = gs.currentPlayer == me && !nuclearTargeting,
                modifier = Modifier.weight(3f).height(48.dp)
            ) { Text(stringResource(R.string.action_end_turn)) }
        }

        // Battle log
        BattleLog(gs.battleLog.takeLast(20), lang, Modifier.fillMaxWidth().weight(1f).padding(8.dp))
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else SeaSurface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = fg, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun BattleLog(entries: List<com.elyssov.navalbattle.game.LogEntry>, lang: String, modifier: Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) listState.animateScrollToItem(entries.size - 1)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SeaSurface)
            .padding(8.dp)
    ) {
        if (entries.isEmpty()) {
            Text("Battle log empty", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        } else {
            LazyColumn(state = listState) {
                items(entries) { e ->
                    val color = when (e.kind) {
                        LogKind.Nuke -> NukeColor
                        LogKind.Sunk -> HitRed
                        LogKind.Hit -> RadarAmber
                        LogKind.Detection -> RadarGreen
                        LogKind.Reactor -> NukeColor
                        LogKind.Warning -> RadarAmber
                        LogKind.Info -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                    Text(
                        "[T${e.turn}/P${e.player}] ${e.text}",
                        color = color,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private val NukeColor = Color(0xFFFF6B00)
