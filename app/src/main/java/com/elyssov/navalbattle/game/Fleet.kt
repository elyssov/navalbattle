package com.elyssov.navalbattle.game

import kotlin.random.Random

object Fleet {

    fun build(settings: GameSettings, lang: String, rng: Random = Random.Default): List<Ship> {
        val composition = FleetComposition.shipsFor(settings.fieldSize, settings.capital)
        val usedNames = mutableMapOf<ShipType, MutableSet<String>>()
        val ships = mutableListOf<Ship>()
        composition.forEach { (type, count) ->
            usedNames.getOrPut(type) { mutableSetOf() }
            repeat(count) { i ->
                val ruName = ShipNames.pick(type, "ru", usedNames[type]!!)
                usedNames[type]!!.add(ruName)
                val enUsed = ships.filter { it.type == type }.map { it.englishName }.toSet()
                val enName = ShipNames.pick(type, "en", enUsed)
                val crew = type.crewMin + rng.nextInt(type.crewMax - type.crewMin + 1)
                ships.add(
                    Ship(
                        id = "${type.name.lowercase()}_$i",
                        type = type,
                        russianName = ruName,
                        englishName = enName,
                        crew = crew
                    )
                )
            }
        }
        return ships
    }

    fun autoPlace(
        ships: List<Ship>, fieldSize: Int, terrain: List<TerrainCell>, rng: Random = Random.Default
    ): List<Ship>? {
        val occupied = Array(fieldSize) { BooleanArray(fieldSize) }
        terrain.forEach { occupied[it.coord.x][it.coord.y] = true }

        val placed = mutableListOf<Ship>()
        val placeable = ships.filter { it.type != ShipType.Submarine }

        for (ship in placeable) {
            var success = false
            repeat(300) {
                val ori = if (rng.nextBoolean()) Orientation.Horizontal else Orientation.Vertical
                val size = ship.size
                val maxX = if (ori == Orientation.Horizontal) fieldSize - size else fieldSize - 1
                val maxY = if (ori == Orientation.Vertical) fieldSize - size else fieldSize - 1
                val x = rng.nextInt(maxX + 1)
                val y = rng.nextInt(maxY + 1)
                val cells = cellsFor(x, y, size, ori)
                if (cells.all { canPlace(it, occupied, fieldSize) }) {
                    cells.forEach { occupied[it.x][it.y] = true }
                    placed.add(ship.copy(cells = cells, orientation = ori, placed = true))
                    success = true
                    return@repeat
                }
            }
            if (!success) return null
        }
        return placed + ships.filter { it.type == ShipType.Submarine }
    }

    fun cellsFor(x: Int, y: Int, size: Int, ori: Orientation): List<Coord> =
        (0 until size).map { if (ori == Orientation.Horizontal) Coord(x + it, y) else Coord(x, y + it) }

    private fun canPlace(c: Coord, occupied: Array<BooleanArray>, fieldSize: Int): Boolean {
        if (c.x !in 0 until fieldSize || c.y !in 0 until fieldSize) return false
        for (dx in -1..1) for (dy in -1..1) {
            val nx = c.x + dx; val ny = c.y + dy
            if (nx in 0 until fieldSize && ny in 0 until fieldSize && occupied[nx][ny]) return false
        }
        return true
    }
}
