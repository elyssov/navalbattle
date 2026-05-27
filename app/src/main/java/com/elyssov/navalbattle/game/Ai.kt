package com.elyssov.navalbattle.game

import kotlin.random.Random

object Ai {

    fun takeTurn(state: GameState, lang: String): GameState {
        val aiIdx = state.currentPlayer
        val n = state.settings.fieldSize.n
        val ai = state.players[aiIdx]
        val enemyIdx = 1 - aiIdx
        val enemy = state.players[enemyIdx]

        // Priority 1: deploy submarine on turn 4-6 if not yet deployed
        val sub = ai.ships.firstOrNull { it.type == ShipType.Submarine && !it.deployed && !it.sunk }
        if (sub != null && state.turn in 4..9) {
            val deployedSub = tryDeploySub(state, aiIdx, sub) ?: return regularAttack(state, aiIdx, lang)
            return state.copy(
                players = state.players.mapIndexed { i, p ->
                    if (i == aiIdx) p.copy(ships = p.ships.map { if (it.id == sub.id) deployedSub else it }) else p
                },
                battleLog = state.battleLog + LogEntry(
                    state.turn, aiIdx, LogKind.Info,
                    if (lang == "ru") "ПЛ \"${deployedSub.name(lang)}\" противника развёрнута."
                    else "Enemy submarine \"${deployedSub.name(lang)}\" deployed."
                )
            )
        }

        // Priority 2: if we have recon hits and special weapons, use them
        val deployedSubAttacker = ai.ships.firstOrNull { it.type == ShipType.Submarine && it.deployed && !it.sunk && it.torpedoesLeft > 0 }
        val tarkr = ai.ships.firstOrNull { it.type == ShipType.Tarkr && !it.sunk && it.launchers.any { l -> l.loaded && !l.damaged } }
        val carrier = ai.ships.firstOrNull { it.type == ShipType.Carrier && !it.sunk && !it.avionicsBlocked && it.strikersLeft > 0 }
        val reconTargets = ai.reconHits.groupBy { it.shipId }.mapValues { it.value.map { rh -> rh.coord } }

        if (reconTargets.isNotEmpty()) {
            val (targetShipId, coords) = reconTargets.entries.random()
            val pickCell = coords.random()
            val targetShip = enemy.ships.firstOrNull { it.id == targetShipId }
            // Prefer PCR on larger ships (cruiser/TARKR/carrier) — guaranteed kill if penetrates
            if (tarkr != null && targetShip != null && targetShip.size >= 3 && Random.nextInt(100) < 60) {
                val launcher = tarkr.launchers.first { it.loaded && !it.damaged }
                return Combat.launchPcr(state, aiIdx, launcher.index, pickCell, lang)
            }
            // Torpedo for any deployed sub + recon
            if (deployedSubAttacker != null && Random.nextInt(100) < 40) {
                return Combat.launchTorpedo(state, aiIdx, pickCell, nuclear = false, lang)
            }
            // Striker if available
            if (carrier != null && Random.nextInt(100) < 50) {
                return Combat.launchStriker(state, aiIdx, pickCell, lang)
            }
            // Regular shot on a known target — much more efficient than random
            return Combat.regularShot(state, aiIdx, pickCell, lang)
        }

        // Priority 3: occasionally radar sweep to gather intel
        val radarShip = ai.ships.firstOrNull {
            !it.sunk && it.radarCooldown == 0 &&
            it.type in listOf(ShipType.Destroyer, ShipType.Cruiser, ShipType.Tarkr, ShipType.Carrier)
        }
        if (radarShip != null && state.turn >= 2 && Random.nextInt(100) < 25) {
            return Recon.radarSweep(state, aiIdx, radarShip.id, lang)
        }

        // Air recon — same, occasional
        if (carrier != null && (carrier.planesLeft > 1 || (carrier.planesLeft > 0 && state.turn % 3 == 0))) {
            if (Random.nextInt(100) < 30) {
                return Recon.airRecon(state, aiIdx, lang)
            }
        }

        // Fallback: regular hunt-and-shoot
        return regularAttack(state, aiIdx, lang)
    }

    private fun regularAttack(state: GameState, aiIdx: Int, lang: String): GameState {
        val n = state.settings.fieldSize.n
        val ai = state.players[aiIdx]
        val enemy = state.players[1 - aiIdx]
        val lastHits = ai.shotsAtEnemy.filter { it.hit }.takeLast(20)
        val candidates = mutableListOf<Coord>()
        lastHits.forEach { hit ->
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
        val target: Coord = if (candidates.isNotEmpty()) candidates.random() else findUntried(state, aiIdx, n)
        return Combat.regularShot(state, aiIdx, target, lang)
    }

    private fun tryDeploySub(state: GameState, playerIdx: Int, sub: Ship): Ship? {
        val n = state.settings.fieldSize.n
        val islandSet = state.terrain.filter { it.kind == TerrainKind.Island }.map { it.coord }.toSet()
        val ownOccupied = mutableSetOf<Coord>()
        state.players[playerIdx].ships.filter { it.placed && it.id != sub.id }
            .forEach { it.cells.forEach { c -> for (dx in -1..1) for (dy in -1..1) ownOccupied += Coord(c.x + dx, c.y + dy) } }

        repeat(200) {
            val ori = if (Random.nextBoolean()) Orientation.Horizontal else Orientation.Vertical
            val maxX = if (ori == Orientation.Horizontal) n - sub.size else n - 1
            val maxY = if (ori == Orientation.Vertical) n - sub.size else n - 1
            val x = Random.nextInt(maxX + 1)
            val y = Random.nextInt(maxY + 1)
            val cells = Fleet.cellsFor(x, y, sub.size, ori)
            if (cells.any { it in islandSet }) return@repeat
            if (cells.any { it in ownOccupied }) return@repeat
            return sub.copy(cells = cells, orientation = ori, deployed = true, placed = true)
        }
        return null
    }

    private fun findUntried(state: GameState, aiIdx: Int, n: Int): Coord {
        val shots = state.players[aiIdx].shotsAtEnemy.map { it.coord }.toSet()
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
