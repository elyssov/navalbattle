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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.game.Coord
import com.elyssov.navalbattle.game.Fleet
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.game.Orientation
import com.elyssov.navalbattle.game.Screen
import com.elyssov.navalbattle.game.Ship
import com.elyssov.navalbattle.game.ShipType
import com.elyssov.navalbattle.game.TerrainKind
import com.elyssov.navalbattle.ui.components.FleetGrid
import com.elyssov.navalbattle.ui.components.drawGridLines
import com.elyssov.navalbattle.ui.components.fillCell
import com.elyssov.navalbattle.ui.components.rememberGridCamera
import com.elyssov.navalbattle.ui.theme.HitRed
import com.elyssov.navalbattle.ui.theme.IslandGreen
import com.elyssov.navalbattle.ui.theme.OceanBlue
import com.elyssov.navalbattle.ui.theme.OceanShallow
import com.elyssov.navalbattle.ui.theme.SeaSurface
import com.elyssov.navalbattle.ui.theme.WreckBrown

@Composable
fun PlacementScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val lang by vm.lang.collectAsState()
    val gs = state ?: return

    val playerIdx = 0
    val player = gs.players[playerIdx]
    val fieldSize = gs.settings.fieldSize.n

    var selectedShipId by remember { mutableStateOf<String?>(null) }
    var selectedOri by remember { mutableStateOf(Orientation.Horizontal) }

    val camera = rememberGridCamera(fieldSize)

    val shipColor = MaterialTheme.colorScheme.primary
    val gridColor = OceanShallow.copy(alpha = 0.4f)
    val islandColor = IslandGreen
    val wreckColor = WreckBrown

    val unplaced = player.ships.filter { !it.placed && it.type != ShipType.Submarine }
    val placed = player.ships.filter { it.placed }
    val readyToFight = unplaced.isEmpty()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text(
            stringResource(R.string.placement_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text("${unplaced.size} left", color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(16.dp))
            Text(
                if (selectedOri == Orientation.Horizontal) "→ horizontal" else "↓ vertical",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.clickable {
                    selectedOri = if (selectedOri == Orientation.Horizontal) Orientation.Vertical else Orientation.Horizontal
                }
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(360.dp).padding(4.dp).border(1.dp, MaterialTheme.colorScheme.tertiary)) {
            FleetGrid(
                camera = camera,
                background = OceanBlue,
                onCellTap = { tap ->
                    if (tap.x < 0 || tap.y < 0 || tap.x >= fieldSize || tap.y >= fieldSize) return@FleetGrid
                    val sid = selectedShipId ?: return@FleetGrid
                    val ship = player.ships.firstOrNull { it.id == sid && !it.placed } ?: return@FleetGrid
                    tryPlace(vm, playerIdx, ship, tap, selectedOri, fieldSize)?.let {
                        selectedShipId = unplaced.drop(1).firstOrNull { it.id != sid }?.id
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
                    ship.cells.forEach { c -> fillCell(cam, c, shipColor) }
                }
                drawGridLines(cam, gridColor)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { vm.autoPlaceCurrent(playerIdx) },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.placement_auto)) }
            OutlinedButton(
                onClick = { vm.clearPlacement(playerIdx); selectedShipId = null },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.placement_clear)) }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.placement_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp)
                .background(SeaSurface, RoundedCornerShape(8.dp))
        ) {
            if (unplaced.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("All ships placed", color = MaterialTheme.colorScheme.tertiary)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(unplaced) { ship ->
                        ShipRosterRow(ship, lang, ship.id == selectedShipId) {
                            selectedShipId = ship.id
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { vm.backToMenu() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.common_back))
            }
            Button(
                onClick = { vm.goToBattle() },
                enabled = readyToFight,
                modifier = Modifier.weight(2f)
            ) {
                Text(stringResource(R.string.placement_ready))
            }
        }
    }
}

@Composable
private fun ShipRosterRow(ship: Ship, lang: String, selected: Boolean, onTap: () -> Unit) {
    val border = if (selected) MaterialTheme.colorScheme.tertiary else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable { onTap() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            ship.type.abbr(lang),
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(64.dp)
        )
        Text("«${ship.name(lang)}»", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text("${ship.size}×", color = MaterialTheme.colorScheme.secondary)
    }
}

private fun tryPlace(
    vm: GameViewModel,
    playerIdx: Int,
    ship: Ship,
    origin: Coord,
    ori: Orientation,
    fieldSize: Int
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
