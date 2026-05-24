package com.elyssov.navalbattle.game

import kotlin.random.Random

object Ai {

    fun takeTurn(state: GameState, lang: String): GameState {
        val aiIdx = state.currentPlayer
        val n = state.settings.fieldSize.n
        val ai = state.players[aiIdx]
        val enemyIdx = 1 - aiIdx
        val enemy = state.players[enemyIdx]

        // Hunt mode: continue around recent hits
        val lastHits = ai.shotsAtEnemy.filter { it.hit }.takeLast(20)
        val candidates = mutableListOf<Coord>()
        lastHits.forEach { hit ->
            // Find if surrounding ship not yet sunk
            val nearbyShip = enemy.ships.firstOrNull { it.placed && !it.sunk && it.cells.any { c -> c == hit.coord } }
            if (nearbyShip != null) {
                listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1).forEach { (dx, dy) ->
                    val c = Coord(hit.coord.x + dx, hit.coord.y + dy)
                    if (c.x in 0 until n && c.y in 0 until n && ai.shotsAtEnemy.none { it.coord == c }) {
                        candidates.add(c)
                    }
                }
            }
        }

        val target: Coord = if (candidates.isNotEmpty()) candidates.random()
        else findUntried(state, aiIdx, n)

        return Combat.regularShot(state, aiIdx, target, lang)
    }

    private fun findUntried(state: GameState, aiIdx: Int, n: Int): Coord {
        val shots = state.players[aiIdx].shotsAtEnemy.map { it.coord }.toSet()
        // Checkerboard pattern: parity matches min ship size
        repeat(200) {
            val x = Random.nextInt(n)
            val y = Random.nextInt(n)
            val c = Coord(x, y)
            if (c !in shots && (x + y) % 2 == 0) return c
        }
        for (x in 0 until n) for (y in 0 until n) {
            val c = Coord(x, y)
            if (c !in shots) return c
        }
        return Coord(0, 0)
    }
}
