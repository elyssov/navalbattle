package com.elyssov.navalbattle.game

object Movement {

    fun canMove(state: GameState, ship: Ship, newAnchor: Coord, newOri: Orientation): Boolean {
        val n = state.settings.fieldSize.n
        val newCells = Fleet.cellsFor(newAnchor.x, newAnchor.y, ship.size, newOri)
        if (newCells.any { it.x !in 0 until n || it.y !in 0 until n }) return false

        val playerIdx = state.players.indexOfFirst { p -> p.ships.any { it.id == ship.id } }
        if (playerIdx == -1) return false
        val occupied = mutableSetOf<Coord>()
        val terrainBlockedForShip = when (ship.type) {
            ShipType.Submarine -> state.terrain.filter { it.kind == TerrainKind.Island }.map { it.coord }
            else -> state.terrain.map { it.coord }
        }
        occupied += terrainBlockedForShip
        state.players[playerIdx].ships.filter { it.id != ship.id && it.placed && !it.sunk }
            .forEach { it.cells.forEach { c -> occupied += c } }
        return newCells.none { it in occupied }
    }

    /** Move along ship axis by +/- 1, 2, 3 cells. */
    fun moveAlongAxis(state: GameState, playerIdx: Int, shipId: String, dx: Int, dy: Int): GameState? {
        val ship = state.players[playerIdx].ships.firstOrNull { it.id == shipId } ?: return null
        val newAnchor = Coord(ship.cells.first().x + dx, ship.cells.first().y + dy)
        if (!canMove(state, ship, newAnchor, ship.orientation)) return null
        val newCells = Fleet.cellsFor(newAnchor.x, newAnchor.y, ship.size, ship.orientation)
        val updated = ship.copy(cells = newCells)
        return state.copy(
            players = state.players.mapIndexed { i, p ->
                if (i == playerIdx) p.copy(ships = p.ships.map { if (it.id == shipId) updated else it }) else p
            }
        )
    }

    fun rotate(state: GameState, playerIdx: Int, shipId: String): GameState? {
        val ship = state.players[playerIdx].ships.firstOrNull { it.id == shipId } ?: return null
        val newOri = if (ship.orientation == Orientation.Horizontal) Orientation.Vertical else Orientation.Horizontal
        val anchor = ship.cells.first()
        if (!canMove(state, ship, anchor, newOri)) return null
        val newCells = Fleet.cellsFor(anchor.x, anchor.y, ship.size, newOri)
        val updated = ship.copy(cells = newCells, orientation = newOri)
        return state.copy(
            players = state.players.mapIndexed { i, p ->
                if (i == playerIdx) p.copy(ships = p.ships.map { if (it.id == shipId) updated else it }) else p
            }
        )
    }

    /** Returns list of valid destination coords for the ship's first cell (anchor). */
    fun validDestinations(state: GameState, playerIdx: Int, shipId: String): List<Coord> {
        val ship = state.players[playerIdx].ships.firstOrNull { it.id == shipId } ?: return emptyList()
        val result = mutableListOf<Coord>()
        val (ax, ay) = if (ship.orientation == Orientation.Horizontal) 1 to 0 else 0 to 1
        for (dist in -3..3) {
            if (dist == 0) continue
            val newAnchor = Coord(ship.cells.first().x + ax * dist, ship.cells.first().y + ay * dist)
            if (canMove(state, ship, newAnchor, ship.orientation)) result.add(newAnchor)
        }
        return result
    }
}
