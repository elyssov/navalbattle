package com.elyssov.navalbattle.game

import kotlin.random.Random

object Recon {

    fun radarSweep(state: GameState, attackerIdx: Int, sourceShipId: String, lang: String): GameState {
        val attacker = state.players[attackerIdx]
        val defenderIdx = 1 - attackerIdx
        val defender = state.players[defenderIdx]
        val source = attacker.ships.firstOrNull { it.id == sourceShipId && !it.sunk } ?: return state
        if (source.radarCooldown > 0) return state
        if (source.type !in listOf(ShipType.Destroyer, ShipType.Cruiser, ShipType.Tarkr, ShipType.Carrier)) return state

        val detected = mutableListOf<ReconHit>()
        defender.ships.filter { !it.sunk && it.placed && (it.type != ShipType.Submarine || it.deployed) }
            .forEach { tgt ->
                val baseChance = 60 + tgt.size * 5 - if (source.damaged) 15 else 0
                val chance = baseChance.coerceIn(5, 95)
                if (Random.nextInt(100) < chance) {
                    tgt.cells.forEach { c -> detected.add(ReconHit(tgt.id, c, state.turn)) }
                }
            }

        val sourceUpdated = source.copy(radarCooldown = 3)
        return state.copy(
            players = state.players.mapIndexed { i, p ->
                when (i) {
                    attackerIdx -> p.copy(
                        ships = p.ships.map { if (it.id == sourceShipId) sourceUpdated else it },
                        reconHits = p.reconHits + detected
                    )
                    else -> p
                }
            },
            battleLog = state.battleLog + LogEntry(
                state.turn, attackerIdx, LogKind.Detection,
                if (detected.isEmpty()) "Radar from ${source.type.abbr(lang)} \"${source.name(lang)}\" — horizon clear."
                else "Radar from ${source.type.abbr(lang)} \"${source.name(lang)}\" — ${detected.map { it.shipId }.distinct().size} contacts!"
            )
        )
    }

    fun airRecon(state: GameState, attackerIdx: Int, lang: String): GameState {
        val attacker = state.players[attackerIdx]
        val defenderIdx = 1 - attackerIdx
        val defender = state.players[defenderIdx]
        val carrier = attacker.ships.firstOrNull { !it.sunk && it.type == ShipType.Carrier && !it.avionicsBlocked } ?: return state
        if (carrier.planesLeft <= 0) return state

        val detected = mutableListOf<ReconHit>()
        var planeAlive = true
        defender.ships.filter { !it.sunk && it.placed && (it.type != ShipType.Submarine || it.deployed) }
            .forEach { tgt ->
                if (!planeAlive) return@forEach
                val chance = (55 + tgt.size * 5).coerceIn(5, 95)
                if (Random.nextInt(100) < chance) {
                    tgt.cells.forEach { c -> detected.add(ReconHit(tgt.id, c, state.turn)) }
                } else if (Random.nextInt(100) < 30) {
                    planeAlive = false
                }
            }

        val updated = carrier.copy(planesLeft = carrier.planesLeft - 1)
        return state.copy(
            players = state.players.mapIndexed { i, p ->
                when (i) {
                    attackerIdx -> p.copy(
                        ships = p.ships.map { if (it.id == carrier.id) updated else it },
                        reconHits = p.reconHits + detected
                    )
                    else -> p
                }
            },
            battleLog = state.battleLog + LogEntry(
                state.turn, attackerIdx, LogKind.Detection,
                "Air recon: ${detected.map { it.shipId }.distinct().size} contacts. " +
                    (if (planeAlive) "Plane returned." else "Plane lost to AA!") +
                    " (${updated.planesLeft} planes left)"
            )
        )
    }

    fun ageReconHits(state: GameState): GameState {
        return state.copy(
            players = state.players.map { p ->
                p.copy(reconHits = p.reconHits.filter { state.turn - it.turnSeen <= 4 })
            }
        )
    }

    fun decrementCooldowns(state: GameState, playerIdx: Int): GameState {
        return state.copy(
            players = state.players.mapIndexed { i, p ->
                if (i == playerIdx) p.copy(
                    ships = p.ships.map {
                        it.copy(
                            radarCooldown = (it.radarCooldown - 1).coerceAtLeast(0),
                            sonarCooldown = (it.sonarCooldown - 1).coerceAtLeast(0),
                            avionicsBlocked = false
                        )
                    }
                ) else p
            }
        )
    }
}
