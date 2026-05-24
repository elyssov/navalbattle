package com.elyssov.navalbattle.game

import kotlin.random.Random

object LandscapeGen {

    fun generate(size: Int, kind: Landscape, rng: Random = Random.Default): List<TerrainCell> {
        if (kind == Landscape.Ocean) return emptyList()

        val (terrainKind, fillRatio, sizes) = when (kind) {
            Landscape.Wrecks -> Triple(TerrainKind.Wreck, 0.10, listOf(1 to 1, 2 to 1, 1 to 2, 2 to 2))
            Landscape.Islands -> Triple(TerrainKind.Island, 0.20, listOf(2 to 2, 3 to 3, 2 to 3, 3 to 2, 4 to 4))
            Landscape.Archipelago -> Triple(TerrainKind.Island, 0.30, listOf(2 to 2, 3 to 3, 2 to 3, 3 to 2, 4 to 4))
            else -> error("ocean handled above")
        }

        repeat(20) { attempt ->
            val grid = Array(size) { BooleanArray(size) }
            val result = mutableListOf<TerrainCell>()
            val targetCells = (size * size * fillRatio).toInt()
            var placed = 0
            var tries = 0
            val budget = targetCells * 6

            while (placed < targetCells && tries < budget) {
                tries++
                val (w, h) = sizes.random(rng)
                val x = rng.nextInt(size - w + 1)
                val y = rng.nextInt(size - h + 1)
                var ok = true
                for (dx in 0 until w) for (dy in 0 until h) {
                    if (grid[x + dx][y + dy]) { ok = false; break }
                }
                if (!ok) continue
                for (dx in 0 until w) for (dy in 0 until h) {
                    grid[x + dx][y + dy] = true
                    result.add(TerrainCell(Coord(x + dx, y + dy), terrainKind))
                    placed++
                }
            }

            if (terrainKind == TerrainKind.Wreck || isConnected(grid)) return result
        }
        return emptyList()
    }

    private fun isConnected(grid: Array<BooleanArray>): Boolean {
        val n = grid.size
        var start: Pair<Int, Int>? = null
        outer@ for (x in 0 until n) for (y in 0 until n) {
            if (!grid[x][y]) { start = x to y; break@outer }
        }
        if (start == null) return false
        val seen = Array(n) { BooleanArray(n) }
        val stack = ArrayDeque<Pair<Int, Int>>()
        stack.addLast(start)
        seen[start.first][start.second] = true
        var free = 0
        for (x in 0 until n) for (y in 0 until n) if (!grid[x][y]) free++
        var visited = 0
        while (stack.isNotEmpty()) {
            val (x, y) = stack.removeLast()
            visited++
            for ((dx, dy) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                val nx = x + dx; val ny = y + dy
                if (nx in 0 until n && ny in 0 until n && !grid[nx][ny] && !seen[nx][ny]) {
                    seen[nx][ny] = true
                    stack.addLast(nx to ny)
                }
            }
        }
        return visited == free
    }
}
