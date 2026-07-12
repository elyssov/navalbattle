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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.elyssov.navalbattle.ui.components.drawShipSprite
import com.elyssov.navalbattle.ui.components.fillCell
import com.elyssov.navalbattle.ui.components.rememberGridCamera
import com.elyssov.navalbattle.ui.components.rememberShipSprites
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

@Composable
fun BattleScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val lang by vm.lang.collectAsState()
    val mode by vm.mode.collectAsState()
    val selectedShipId by vm.selectedShipId.collectAsState()
    val pcrLauncherIdx by vm.pcrLauncherIdx.collectAsState()
    val nuclearTargeting by vm.nuclearTargeting.collectAsState()
    val handoff by vm.handoffPending.collectAsState()
    val acted by vm.actedThisTurn.collectAsState()
    val audio = LocalAudio.current

    val gs = state ?: return
    val n = gs.settings.fieldSize.n
    val me = gs.currentPlayer
    val enemy = 1 - me
    val isHotseat = gs.settings.mode == GameMode.Hotseat
    val viewerSeesShips = !isHotseat || !handoff

    val cameraFleet = rememberGridCamera(n)
    val cameraRadar = rememberGridCamera(n)
    val sprites = rememberShipSprites()

    val shipColor = MaterialTheme.colorScheme.primary
    val selectedShipColor = RadarAmber
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
    val hasNukeSub = player.ships.any {
        it.type == ShipType.Submarine && it.deployed && !it.sunk && it.nukeTorpedoLeft > 0
    }
    val nukeAvailable = !nukeUsed && gs.settings.fieldSize != FieldSize.Skirmish && hasNukeSub

    val hasTarkr = player.ships.any { it.type == ShipType.Tarkr && !it.sunk && it.launchers.any { l -> l.loaded && !l.damaged } }
    val hasCarrier = player.ships.any { it.type == ShipType.Carrier && !it.sunk && !it.avionicsBlocked && it.strikersLeft > 0 }
    val hasAirRecon = player.ships.any { it.type == ShipType.Carrier && !it.sunk && !it.avionicsBlocked && it.planesLeft > 0 }
    val hasRadarShip = player.ships.any { !it.sunk && it.radarCooldown == 0 && it.type in listOf(ShipType.Destroyer, ShipType.Cruiser, ShipType.Tarkr, ShipType.Carrier) }
    val hasSub = player.ships.any { it.type == ShipType.Submarine && it.deployed && !it.sunk && it.torpedoesLeft > 0 }
    val hasUndeployedSub = player.ships.any { it.type == ShipType.Submarine && !it.deployed && !it.sunk }

    // ──────────────────────────── LAYOUT ────────────────────────────
    // Landscape split-view: [Fleet Map | Radar] | [Action panel + log]
    Row(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(4.dp)) {

        // LEFT: two maps side-by-side
        Row(modifier = Modifier.weight(2f).fillMaxHeight()) {

            // Fleet Map
            MapPanel(
                label = stringResource(R.string.battle_fleet_map),
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
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
                            val sprite = sprites[ship.type]
                            if (sprite != null && !ship.sunk) drawShipSprite(cam, ship, sprite)
                            val isSel = ship.id == selectedShipId
                            ship.cells.forEachIndexed { idx, c ->
                                when {
                                    ship.sunk -> fillCell(cam, c, sunkC)
                                    ship.hits[idx] -> fillCell(cam, c, hitC.copy(alpha = 0.7f))
                                    isSel -> strokeCell(cam, c, selectedShipColor, 2f)
                                    else -> {}
                                }
                            }
                        }
                    }
                    gs.players[enemy].shotsAtEnemy.forEach { shot ->
                        if (!shot.hit) strokeCell(cam, shot.coord, missC.copy(alpha = 0.6f), 1f)
                    }
                    gs.nuclearZones.forEach { z ->
                        for (dx in -5..5) for (dy in -5..5) {
                            val c = Coord(z.center.x + dx, z.center.y + dy)
                            if (c.x in 0 until n && c.y in 0 until n) fillCell(cam, c, NukeOrange.copy(alpha = 0.08f))
                        }
                    }
                    drawGridLines(cam, gridC)
                }
            }

            // Radar
            MapPanel(
                label = stringResource(R.string.battle_radar),
                accent = RadarGreen,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                FleetGrid(
                    camera = cameraRadar,
                    background = SeaBackground,
                    onCellTap = { c ->
                        if (gs.currentPlayer != me || c.x !in 0 until n || c.y !in 0 until n) return@FleetGrid
                        // One offensive action per turn. Nuclear targeting is exempt — the
                        // ritual is its own once-per-match flow gated by nuclearUsed.
                        if (acted && !nuclearTargeting) return@FleetGrid
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
                    // ONE bounding rect per detected ship — no per-cell spam
                    player.reconHits.groupBy { it.shipId }.forEach { (_, hits) ->
                        val coords = hits.map { it.coord }
                        if (coords.isEmpty()) return@forEach
                        val minX = coords.minOf { it.x }
                        val maxX = coords.maxOf { it.x }
                        val minY = coords.minOf { it.y }
                        val maxY = coords.maxOf { it.y }
                        val age = gs.turn - hits.maxOf { it.turnSeen }
                        val alpha = when (age) { 0 -> 0.9f; 1 -> 0.6f; 2 -> 0.4f; else -> 0.2f }
                        val cs = cam.cellPx
                        val topLeft = Offset(cam.offset.x + minX * cs, cam.offset.y + minY * cs)
                        val sz = Size((maxX - minX + 1) * cs, (maxY - minY + 1) * cs)
                        drawRect(
                            color = reconC.copy(alpha = alpha),
                            topLeft = topLeft, size = sz,
                            style = Stroke(width = 2.5f)
                        )
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

        // RIGHT: actions + log
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 8.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.battle_turn, gs.turn),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "P${gs.currentPlayer + 1}",
                    color = if (gs.currentPlayer == 0) RadarGreen else RadarAmber,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(mode.name, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
            }

            ModeHint(mode, pcrLauncherIdx, nuclearTargeting, lang)

            // Action buttons grid (3 columns)
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModeRow3(
                    Triple(Spec("🎯", "Fire", mode == BattleMode.Fire, true, false) { vm.setMode(BattleMode.Fire) },
                           Spec("📡", "Radar", mode == BattleMode.Radar, hasRadarShip, false) { vm.setMode(BattleMode.Radar) },
                           Spec("🛩", "Air", false, hasAirRecon, false) { audio.play(Sfx.Radar); vm.useAirRecon() })
                )
                ModeRow3(
                    Triple(Spec("🚀", "PCR", mode == BattleMode.Pcr, hasTarkr, false) { vm.setMode(BattleMode.Pcr) },
                           Spec("✈", "Strike", mode == BattleMode.Striker, hasCarrier, false) { vm.setMode(BattleMode.Striker) },
                           Spec("💣", "Torp", mode == BattleMode.Torpedo, hasSub, false) { vm.setMode(BattleMode.Torpedo) })
                )
                ModeRow3(
                    Triple(Spec("🐬", "Deploy", mode == BattleMode.DeploySub, hasUndeployedSub, false) { vm.setMode(BattleMode.DeploySub) },
                           Spec("⚓", "Move", mode == BattleMode.Move, true, false) { vm.setMode(BattleMode.Move) },
                           Spec("☢", "Nuke", false, nukeAvailable && !nuclearTargeting, true) { vm.startNukeRitual() })
                )
            }

            // PCR launcher picker
            if (mode == BattleMode.Pcr && hasTarkr) {
                val tarkr = player.ships.first { it.type == ShipType.Tarkr }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tarkr.launchers.forEach { l ->
                        val enabled = l.loaded && !l.damaged
                        val sel = pcrLauncherIdx == l.index
                        Button(
                            onClick = { vm.selectPcrLauncher(l.index) },
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                            colors = if (sel) ButtonDefaults.buttonColors(containerColor = RadarAmber) else ButtonDefaults.buttonColors()
                        ) { Text("ПУ${l.index + 1}", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }

            // Move panel
            if (mode == BattleMode.Move && selectedShipId != null) {
                val ship = player.ships.firstOrNull { it.id == selectedShipId }
                if (ship != null) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(-3, -2, -1).forEach { d ->
                                Button(onClick = { if (vm.moveShip(ship.id, d)) vm.endTurn() }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)) { Text("$d") }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1, 2, 3).forEach { d ->
                                Button(onClick = { if (vm.moveShip(ship.id, d)) vm.endTurn() }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)) { Text("+$d") }
                            }
                        }
                        Button(onClick = { if (vm.rotateShip(ship.id)) vm.endTurn() }, modifier = Modifier.fillMaxWidth()) { Text("⟳ 90°") }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { audio.play(Sfx.Click); vm.endTurn() },
                enabled = gs.currentPlayer == me && !nuclearTargeting,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) { Text(stringResource(R.string.action_end_turn), fontWeight = FontWeight.Bold) }

            BattleLog(gs.battleLog.takeLast(20), Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp))
        }
    }

    if (handoff) {
        HotseatHandoff(
            playerLabel = "P${gs.currentPlayer + 1}",
            onReady = { vm.completeHandoff() }
        )
    }
}

@Composable
private fun MapPanel(label: String, accent: Color, modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.padding(4.dp)) {
        Text(label, color = accent, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
        Box(modifier = Modifier.fillMaxSize().clipToBounds().border(1.dp, accent)) {
            content()
        }
    }
}

@Composable
private fun ModeHint(mode: BattleMode, pcrLauncherIdx: Int, nuclearTargeting: Boolean, lang: String) {
    val text = when {
        nuclearTargeting -> if (lang == "ru") "Σ☢ ВЫБЕРИ ЭПИЦЕНТР НА РАДАРЕ" else "Σ☢ SELECT EPICENTER ON RADAR"
        mode == BattleMode.Fire -> if (lang == "ru") "Радар: тап = выстрел" else "Radar: tap = fire"
        mode == BattleMode.Radar -> if (lang == "ru") "Карта флота: тап по своему кораблю" else "Fleet Map: tap your ship"
        mode == BattleMode.Pcr -> if (pcrLauncherIdx < 0) if (lang == "ru") "Выбери ПУ" else "Pick launcher" else if (lang == "ru") "Радар: тап = пуск" else "Radar: tap = launch"
        mode == BattleMode.Torpedo -> if (lang == "ru") "Радар: тап = торпеда" else "Radar: tap = torpedo"
        mode == BattleMode.Striker -> if (lang == "ru") "Радар: тап = штурмовик" else "Radar: tap = strike"
        mode == BattleMode.DeploySub -> if (lang == "ru") "Карта: тап = ПЛ" else "Fleet Map: tap = sub"
        mode == BattleMode.Move -> if (lang == "ru") "Карта: тап = выбор" else "Fleet Map: tap = pick"
        else -> ""
    }
    if (text.isNotEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                .background(if (nuclearTargeting) NukeOrange.copy(alpha = 0.2f) else SeaSurface, RoundedCornerShape(4.dp))
                .padding(6.dp)
        ) {
            Text(
                text,
                color = if (nuclearTargeting) NukeOrange else MaterialTheme.colorScheme.tertiary,
                fontWeight = if (nuclearTargeting) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class Spec(
    val icon: String, val label: String, val selected: Boolean,
    val enabled: Boolean, val isNuke: Boolean, val onClick: () -> Unit
)

@Composable
private fun ModeRow3(specs: Triple<Spec, Spec, Spec>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ModeButton(specs.first, Modifier.weight(1f))
        ModeButton(specs.second, Modifier.weight(1f))
        ModeButton(specs.third, Modifier.weight(1f))
    }
}

@Composable
private fun ModeButton(s: Spec, modifier: Modifier) {
    val bg = when {
        !s.enabled -> SeaSurface.copy(alpha = 0.4f)
        s.isNuke -> NukeOrange
        s.selected -> RadarAmber
        else -> SeaSurface
    }
    val fg = if (s.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(enabled = s.enabled) { s.onClick() }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(s.icon, color = fg, fontWeight = FontWeight.Bold)
            Text(s.label, color = fg, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BattleLog(entries: List<LogEntry>, modifier: Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) listState.animateScrollToItem(entries.size - 1)
    }
    Box(
        modifier = modifier.clip(RoundedCornerShape(6.dp)).background(SeaSurface).padding(6.dp)
    ) {
        if (entries.isEmpty()) {
            Text("…", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
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
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📱 →", color = RadarAmber, style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text("Pass the device to $playerLabel", color = MissWhite, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onReady, modifier = Modifier.width(220.dp).height(56.dp)) {
                Text("I'm $playerLabel — ready", fontWeight = FontWeight.Bold)
            }
        }
    }
}
