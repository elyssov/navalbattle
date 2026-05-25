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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.game.Coord
import com.elyssov.navalbattle.game.Fleet
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.game.Orientation
import com.elyssov.navalbattle.game.Ship
import com.elyssov.navalbattle.game.ShipType
import com.elyssov.navalbattle.game.TerrainKind
import com.elyssov.navalbattle.ui.components.FleetGrid
import com.elyssov.navalbattle.ui.components.drawGridLines
import com.elyssov.navalbattle.ui.components.drawShipSprite
import com.elyssov.navalbattle.ui.components.fillCell
import com.elyssov.navalbattle.ui.components.rememberGridCamera
import com.elyssov.navalbattle.ui.components.rememberShipSprites
import com.elyssov.navalbattle.ui.theme.HitRed
import com.elyssov.navalbattle.ui.theme.IslandGreen
import com.elyssov.navalbattle.ui.theme.OceanBlue
import com.elyssov.navalbattle.ui.theme.OceanShallow
import com.elyssov.navalbattle.ui.theme.RadarAmber
import com.elyssov.navalbattle.ui.theme.SeaSurface
import com.elyssov.navalbattle.ui.theme.WreckBrown

@Composable
fun PlacementScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val lang by vm.lang.collectAsState()
    val playerIdx by vm.placingPlayer.collectAsState()
    val handoff by vm.handoffPending.collectAsState()
    val gs = state ?: return

    val player = gs.players[playerIdx]
    val fieldSize = gs.settings.fieldSize.n

    var selectedShipId by remember { mutableStateOf<String?>(null) }
    var selectedOri by remember { mutableStateOf(Orientation.Horizontal) }

    val camera = rememberGridCamera(fieldSize)
    val sprites = rememberShipSprites()

    val shipColor = MaterialTheme.colorScheme.primary
    val gridColor = OceanShallow.copy(alpha = 0.4f)
    val islandColor = IslandGreen
    val wreckColor = WreckBrown

    val unplaced = player.ships.filter { !it.placed && it.type != ShipType.Submarine }
    val placed = player.ships.filter { it.placed }
    val readyToFight = unplaced.isEmpty()

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(8.dp)) {

        // Header: title + counter + orientation toggle (compact)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "P${playerIdx + 1} · ${stringResource(R.string.placement_title)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            Text("${unplaced.size}", color = RadarAmber, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    selectedOri = if (selectedOri == Orientation.Horizontal) Orientation.Vertical else Orientation.Horizontal
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(if (selectedOri == Orientation.Horizontal) "→" else "↓", fontWeight = FontWeight.Bold)
            }
        }

        // Canvas — TAKES MOST SPACE via weight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .border(1.dp, MaterialTheme.colorScheme.tertiary)
        ) {
            FleetGrid(
                camera = camera,
                background = OceanBlue,
                onCellTap = { tap ->
                    if (tap.x < 0 || tap.y < 0 || tap.x >= fieldSize || tap.y >= fieldSize) return@FleetGrid
                    val existing = placed.firstOrNull { tap in it.cells }
                    if (existing != null) {
                        // Tap on an already-placed ship → rotate it 90°
                        rotateExisting(vm, playerIdx, existing, fieldSize)
                        return@FleetGrid
                    }
                    val sid = selectedShipId
                    if (sid != null) {
                        val ship = player.ships.firstOrNull { it.id == sid && !it.placed } ?: return@FleetGrid
                        val placedOk = tryPlace(vm, playerIdx, ship, tap, selectedOri, fieldSize)
                        if (placedOk) {
                            selectedShipId = player.ships.firstOrNull { !it.placed && it.id != sid && it.type != ShipType.Submarine }?.id
                        }
                    }
                },
                onCellLong = { tap ->
                    val existing = placed.firstOrNull { tap in it.cells } ?: return@FleetGrid
                    rotateExisting(vm, playerIdx, existing, fieldSize)
                }
            ) { cam ->
                gs.terrain.forEach {
                    fillCell(cam, it.coord, if (it.kind == TerrainKind.Island) islandColor else wreckColor)
                }
                placed.forEach { ship ->
                    val sprite = sprites[ship.type]
                    if (sprite != null) drawShipSprite(cam, ship, sprite)
                    else ship.cells.forEach { fillCell(cam, it, shipColor) }
                }
                drawGridLines(cam, gridColor)
            }
        }

        // Roster (horizontal scroll) — always visible above buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp, max = 84.dp)
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SeaSurface)
        ) {
            if (unplaced.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (lang == "ru") "Все корабли расставлены" else "All ships placed",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(unplaced, key = { it.id }) { ship ->
                        ShipChip(ship, lang, ship.id == selectedShipId) { selectedShipId = ship.id }
                    }
                }
            }
        }

        // Action buttons (compact row)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = { vm.autoPlaceCurrent(playerIdx); selectedShipId = null },
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) { Text(stringResource(R.string.placement_auto), maxLines = 1) }
            OutlinedButton(
                onClick = { vm.clearPlacement(playerIdx); selectedShipId = null },
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) { Text(stringResource(R.string.placement_clear), maxLines = 1) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = { vm.backToMenu() },
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
            ) { Text(stringResource(R.string.common_back)) }
            Button(
                onClick = { vm.finishPlacement() },
                enabled = readyToFight,
                modifier = Modifier.weight(2f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
            ) { Text(stringResource(R.string.placement_ready), fontWeight = FontWeight.Bold) }
        }
    }

    if (handoff) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                    .border(2.dp, RadarAmber, RoundedCornerShape(12.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("📱 →", color = RadarAmber, style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (lang == "ru") "Передай устройство P${playerIdx + 1}"
                        else "Pass the device to P${playerIdx + 1}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { vm.completeHandoff() }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(if (lang == "ru") "Я P${playerIdx + 1} — готов" else "I'm P${playerIdx + 1} — ready")
                    }
                }
            }
        }
    }
}

@Composable
private fun ShipChip(ship: Ship, lang: String, selected: Boolean, onTap: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else SeaSurface.copy(alpha = 0.6f)
    val border = if (selected) RadarAmber else Color.Transparent
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(8.dp))
            .clickable { onTap() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            ship.type.abbr(lang),
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Text("${ship.size}", color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(6.dp))
        Text(
            "«${ship.name(lang)}»",
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun tryPlace(
    vm: GameViewModel, playerIdx: Int, ship: Ship, origin: Coord, ori: Orientation, fieldSize: Int
): Boolean {
    val state = vm.state.value ?: return false
    val cells = Fleet.cellsFor(origin.x, origin.y, ship.size, ori)
    if (cells.any { it.x !in 0 until fieldSize || it.y !in 0 until fieldSize }) return false

    val occupied = mutableSetOf<Coord>()
    state.terrain.forEach { occupied += it.coord }
    state.players[playerIdx].ships.filter { it.placed && it.id != ship.id }
        .forEach { it.cells.forEach { c -> for (dx in -1..1) for (dy in -1..1) occupied += Coord(c.x + dx, c.y + dy) } }

    if (cells.any { it in occupied }) return false

    val updated = ship.copy(cells = cells, orientation = ori, placed = true)
    val newState = state.copy(
        players = state.players.mapIndexed { i, p ->
            if (i == playerIdx) p.copy(ships = p.ships.map { if (it.id == ship.id) updated else it }) else p
        }
    )
    vm.replaceState(newState)
    return true
}

private fun rotateExisting(vm: GameViewModel, playerIdx: Int, ship: Ship, fieldSize: Int) {
    val state = vm.state.value ?: return
    val newOri = if (ship.orientation == Orientation.Horizontal) Orientation.Vertical else Orientation.Horizontal
    val anchor = ship.cells.first()
    val newCells = Fleet.cellsFor(anchor.x, anchor.y, ship.size, newOri)
    if (newCells.any { it.x !in 0 until fieldSize || it.y !in 0 until fieldSize }) return

    val occupied = mutableSetOf<Coord>()
    state.terrain.forEach { occupied += it.coord }
    state.players[playerIdx].ships.filter { it.placed && it.id != ship.id }
        .forEach { it.cells.forEach { c -> for (dx in -1..1) for (dy in -1..1) occupied += Coord(c.x + dx, c.y + dy) } }
    if (newCells.any { it in occupied }) return

    val rotated = ship.copy(cells = newCells, orientation = newOri)
    val newState = state.copy(
        players = state.players.mapIndexed { i, p ->
            if (i == playerIdx) p.copy(ships = p.ships.map { if (it.id == ship.id) rotated else it }) else p
        }
    )
    vm.replaceState(newState)
}
