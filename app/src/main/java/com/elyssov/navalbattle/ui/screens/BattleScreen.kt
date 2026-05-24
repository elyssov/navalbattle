package com.elyssov.navalbattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.audio.Sfx
import com.elyssov.navalbattle.game.BattleMode
import com.elyssov.navalbattle.game.Coord
import com.elyssov.navalbattle.game.FieldSize
import com.elyssov.navalbattle.game.GameMode
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.game.LogEntry
import com.elyssov.navalbattle.game.LogKind
import com.elyssov.navalbattle.game.Orientation
import com.elyssov.navalbattle.game.ShipType
import com.elyssov.navalbattle.game.TerrainKind
import com.elyssov.navalbattle.ui.audio.LocalAudio
import com.elyssov.navalbattle.ui.components.FleetGrid
import com.elyssov.navalbattle.ui.components.drawGridLines
import com.elyssov.navalbattle.ui.components.fillCell
import com.elyssov.navalbattle.ui.components.rememberGridCamera
import com.elyssov.navalbattle.ui.components.strokeCell
import com.elyssov.navalbattle.ui.theme.HitRed
import com.elyssov.navalbattle.ui.theme.IslandGreen
import com.elyssov.navalbattle.ui.theme.MissWhite
import com.elyssov.navalbattle.ui.theme.NukeOrange
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
    val mode by vm.mode.collectAsState()
    val selectedShipId by vm.selectedShipId.collectAsState()
    val pcrLauncherIdx by vm.pcrLauncherIdx.collectAsState()
    val nuclearTargeting by vm.nuclearTargeting.collectAsState()
    val handoff by vm.handoffPending.collectAsState()
    val audio = LocalAudio.current

    val gs = state ?: return
    val n = gs.settings.fieldSize.n
    val me = gs.currentPlayer
    val enemy = 1 - me
    val isHotseat = gs.settings.mode == GameMode.Hotseat
    val viewerSeesShips = !isHotseat || !handoff

    var view by remember { mutableStateOf(BattleView.Radar) }
    val cameraFleet = rememberGridCamera(n)
    val cameraRadar = rememberGridCamera(n)

    val shipColor = MaterialTheme.colorScheme.primary
    val selectedShipColor = RadarAmber
    val moveTargetColor = RadarGreen
    val islandC = IslandGreen
    val wreckC = WreckBrown
    val gridC = OceanShallow.copy(alpha = 0.4f)
    val radarGridC = RadarGreen.copy(alpha = 0.3f)
    val hitC = HitRed
    val missC = MissWhite
    val sunkC = SunkBlack
    val reconC = RadarAmber

    val player = gs.players[me]
    val nukeUsed = player.nuclearUsed
    val nukeAvailable = !nukeUsed && gs.settings.fieldSize != FieldSize.Skirmish

    val hasTarkr = player.ships.any { it.type == ShipType.Tarkr && !it.sunk && it.launchers.any { l -> l.loaded && !l.damaged } }
    val hasCarrier = player.ships.any { it.type == ShipType.Carrier && !it.sunk && !it.avionicsBlocked && it.strikersLeft > 0 }
    val hasAirRecon = player.ships.any { it.type == ShipType.Carrier && !it.sunk && !it.avionicsBlocked && it.planesLeft > 0 }
    val hasRadarShip = player.ships.any { !it.sunk && it.radarCooldown == 0 && it.type in listOf(ShipType.Destroyer, ShipType.Cruiser, ShipType.Tarkr, ShipType.Carrier) }
    val hasSub = player.ships.any { it.type == ShipType.Submarine && it.deployed && !it.sunk && it.torpedoesLeft > 0 }
    val hasUndeployedSub = player.ships.any { it.type == ShipType.Submarine && !it.deployed && !it.sunk }

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
            Spacer(Modifier.width(12.dp))
            Text(
                "P${gs.currentPlayer + 1}",
                color = if (gs.currentPlayer == 0) RadarGreen else RadarAmber,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text("Mode: ${mode.name}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
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
            modifier = Modifier.fillMaxWidth().height(340.dp).padding(8.dp).border(1.dp, MaterialTheme.colorScheme.tertiary)
        ) {
            when (view) {
                BattleView.FleetMap -> {
                    FleetGrid(
                        camera = cameraFleet,
                        background = OceanBlue,
                        onCellTap = { c ->
                            if (c.x !in 0 until n || c.y !in 0 until n) return@FleetGrid
                            audio.play(Sfx.Click)
                            when (mode) {
                                BattleMode.Radar -> {
                                    val ship = player.ships.firstOrNull { !it.sunk && it.placed && c in it.cells } ?: return@FleetGrid
                                    if (ship.radarCooldown == 0 && ship.type in listOf(ShipType.Destroyer, ShipType.Cruiser, ShipType.Tarkr, ShipType.Carrier)) {
                                        audio.play(Sfx.Radar)
                                        vm.useRadar(ship.id)
                                    }
                                }
                                BattleMode.DeploySub -> {
                                    vm.deploySubAt(c, Orientation.Horizontal)
                                    audio.play(Sfx.Torpedo)
                                }
                                BattleMode.Move -> {
                                    val ship = player.ships.firstOrNull { !it.sunk && it.placed && c in it.cells }
                                    vm.selectShip(ship?.id)
                                }
                                else -> {}
                            }
                        }
                    ) { cam ->
                        gs.terrain.forEach {
                            fillCell(cam, it.coord, if (it.kind == TerrainKind.Island) islandC else wreckC)
                        }
                        if (viewerSeesShips) {
                            player.ships.filter { it.placed }.forEach { ship ->
                                val isSel = ship.id == selectedShipId
                                ship.cells.forEachIndexed { idx, c ->
                                    val color = when {
                                        ship.sunk -> sunkC
                                        ship.hits[idx] -> hitC
                                        isSel -> selectedShipColor
                                        else -> shipColor
                                    }
                                    fillCell(cam, c, color)
                                }
                            }
                        }
                        gs.players[enemy].shotsAtEnemy.forEach { shot ->
                            if (!shot.hit) strokeCell(cam, shot.coord, missC.copy(alpha = 0.6f), 1f)
                        }
                        // Nuclear zones over own field too if applicable
                        gs.nuclearZones.forEach { z ->
                            for (dx in -5..5) for (dy in -5..5) {
                                val c = Coord(z.center.x + dx, z.center.y + dy)
                                if (c.x in 0 until n && c.y in 0 until n) fillCell(cam, c, NukeOrange.copy(alpha = 0.08f))
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
                            if (gs.currentPlayer != me || c.x !in 0 until n || c.y !in 0 until n) return@FleetGrid
                            audio.play(Sfx.Click)
                            when {
                                nuclearTargeting -> { audio.play(Sfx.NukeBoom); vm.launchNuke(c) }
                                mode == BattleMode.Pcr && pcrLauncherIdx >= 0 -> { audio.play(Sfx.PcrLaunch); vm.launchPcr(c) }
                                mode == BattleMode.Torpedo -> { audio.play(Sfx.Torpedo); vm.launchTorpedo(c) }
                                mode == BattleMode.Striker -> { audio.play(Sfx.Striker); vm.launchStriker(c) }
                                mode == BattleMode.Fire -> { audio.play(Sfx.Shot); vm.fireShot(c) }
                                else -> {}
                            }
                        }
                    ) { cam ->
                        gs.terrain.filter { it.kind == TerrainKind.Island }
                            .forEach { fillCell(cam, it.coord, islandC.copy(alpha = 0.5f)) }
                        player.shotsAtEnemy.forEach { shot ->
                            fillCell(cam, shot.coord, if (shot.hit) hitC.copy(alpha = 0.7f) else missC.copy(alpha = 0.35f))
                        }
                        gs.players[enemy].ships.filter { it.sunk }.forEach { ship ->
                            ship.cells.forEach { fillCell(cam, it, sunkC) }
                        }
                        player.reconHits.forEach { hit ->
                            val age = gs.turn - hit.turnSeen
                            val alpha = when (age) { 0 -> 0.9f; 1 -> 0.6f; 2 -> 0.4f; else -> 0.2f }
                            strokeCell(cam, hit.coord, reconC.copy(alpha = alpha), 2f)
                        }
                        gs.nuclearZones.forEach { z ->
                            for (dx in -5..5) for (dy in -5..5) {
                                val c = Coord(z.center.x + dx, z.center.y + dy)
                                if (c.x in 0 until n && c.y in 0 until n) fillCell(cam, c, NukeOrange.copy(alpha = 0.15f))
                            }
                        }
                        drawGridLines(cam, radarGridC)
                    }
                }
            }
        }

        // Mode hint
        ModeHint(mode, pcrLauncherIdx, nuclearTargeting, lang)

        // Action bar — horizontal scrollable row of modes
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ModeButton("🎯", "Fire", mode == BattleMode.Fire, true) { vm.setMode(BattleMode.Fire) }
            ModeButton("📡", "Radar", mode == BattleMode.Radar, hasRadarShip) { vm.setMode(BattleMode.Radar) }
            ModeButton("🛩", "Air", false, hasAirRecon) { audio.play(Sfx.Radar); vm.useAirRecon() }
            ModeButton("🚀", "PCR", mode == BattleMode.Pcr, hasTarkr) { vm.setMode(BattleMode.Pcr) }
            ModeButton("✈", "Strike", mode == BattleMode.Striker, hasCarrier) { vm.setMode(BattleMode.Striker) }
            ModeButton("💣", "Torp", mode == BattleMode.Torpedo, hasSub) { vm.setMode(BattleMode.Torpedo) }
            ModeButton("🐬", "Deploy", mode == BattleMode.DeploySub, hasUndeployedSub) { vm.setMode(BattleMode.DeploySub) }
            ModeButton("⚓", "Move", mode == BattleMode.Move, true) { vm.setMode(BattleMode.Move) }
            ModeButton("☢", "Nuke", false, nukeAvailable && !nuclearTargeting, isNuke = true) { vm.startNukeRitual() }
        }

        // PCR launcher picker
        if (mode == BattleMode.Pcr && hasTarkr) {
            val tarkr = player.ships.first { it.type == ShipType.Tarkr }
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tarkr.launchers.forEach { l ->
                    val enabled = l.loaded && !l.damaged
                    val sel = pcrLauncherIdx == l.index
                    Button(
                        onClick = { vm.selectPcrLauncher(l.index) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        colors = if (sel) ButtonDefaults.buttonColors(containerColor = RadarAmber) else ButtonDefaults.buttonColors()
                    ) { Text("PU-${l.index + 1}") }
                }
            }
        }

        // Move panel — when ship selected
        if (mode == BattleMode.Move && selectedShipId != null) {
            val ship = player.ships.firstOrNull { it.id == selectedShipId }
            if (ship != null) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(-3, -2, -1, 1, 2, 3).forEach { d ->
                        Button(
                            onClick = { vm.moveShip(ship.id, d); vm.endTurn() },
                            modifier = Modifier.weight(1f)
                        ) { Text(if (d > 0) "+$d" else "$d") }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Button(
                        onClick = { vm.rotateShip(ship.id); vm.endTurn() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("⟳ Rotate 90°") }
                }
            }
        }

        // End turn bar
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = { audio.play(Sfx.Click); vm.endTurn() },
                enabled = gs.currentPlayer == me && !nuclearTargeting,
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text(stringResource(R.string.action_end_turn)) }
        }

        BattleLog(gs.battleLog.takeLast(20), Modifier.fillMaxWidth().weight(1f).padding(8.dp))
    }

    // Hotseat handoff overlay
    if (handoff) {
        HotseatHandoff(
            playerLabel = "P${gs.currentPlayer + 1}",
            onReady = { vm.completeHandoff() }
        )
    }
}

@Composable
private fun ModeHint(mode: BattleMode, pcrLauncherIdx: Int, nuclearTargeting: Boolean, lang: String) {
    val text = when {
        nuclearTargeting -> if (lang == "ru") "Σ☢ ВЫБЕРИ ЭПИЦЕНТР НА РАДАРЕ" else "Σ☢ SELECT EPICENTER ON RADAR"
        mode == BattleMode.Fire -> if (lang == "ru") "Тапни клетку на радаре — выстрел" else "Tap radar cell to fire"
        mode == BattleMode.Radar -> if (lang == "ru") "Карта флота: тапни корабль-источник РЛС" else "Fleet Map: tap source ship for radar"
        mode == BattleMode.Pcr ->
            if (pcrLauncherIdx < 0) if (lang == "ru") "ПКР: выбери ПУ ниже" else "PCR: pick launcher below"
            else if (lang == "ru") "Радар: тапни цель" else "Radar: tap target"
        mode == BattleMode.Torpedo -> if (lang == "ru") "Торпеда: тапни цель на радаре" else "Torpedo: tap target on radar"
        mode == BattleMode.Striker -> if (lang == "ru") "Штурмовик: тапни цель на радаре" else "Striker: tap target on radar"
        mode == BattleMode.DeploySub -> if (lang == "ru") "Карта флота: тапни клетку для ПЛ" else "Fleet Map: tap cell for submarine"
        mode == BattleMode.Move -> if (lang == "ru") "Карта флота: тапни свой корабль" else "Fleet Map: tap your ship"
        else -> ""
    }
    if (text.isNotEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                .background(if (nuclearTargeting) NukeOrange.copy(alpha = 0.2f) else SeaSurface, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Text(
                text,
                color = if (nuclearTargeting) NukeOrange else MaterialTheme.colorScheme.tertiary,
                fontWeight = if (nuclearTargeting) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ModeButton(icon: String, label: String, selected: Boolean, enabled: Boolean, isNuke: Boolean = false, onClick: () -> Unit) {
    val bg = when {
        !enabled -> SeaSurface.copy(alpha = 0.4f)
        isNuke -> NukeOrange
        selected -> RadarAmber
        else -> SeaSurface
    }
    val fg = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, color = fg, fontWeight = FontWeight.Bold)
            Text(label, color = fg, style = MaterialTheme.typography.bodyMedium)
        }
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
private fun BattleLog(entries: List<LogEntry>, modifier: Modifier) {
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
                        LogKind.Nuke -> NukeOrange
                        LogKind.Sunk -> HitRed
                        LogKind.Hit -> RadarAmber
                        LogKind.Detection -> RadarGreen
                        LogKind.Reactor -> NukeOrange
                        LogKind.Warning -> RadarAmber
                        LogKind.Info -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                    Text(
                        "[T${e.turn}/P${e.player + 1}] ${e.text}",
                        color = color,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HotseatHandoff(playerLabel: String, onReady: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📱 →", color = RadarAmber, style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text("Pass the device to $playerLabel", color = MissWhite, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onReady, modifier = Modifier.width(220.dp).height(56.dp)) {
                Text("I'm $playerLabel — ready", fontWeight = FontWeight.Bold)
            }
        }
    }
}
