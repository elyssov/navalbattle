package com.elyssov.navalbattle.game

import kotlin.random.Random

object Combat {

    fun regularShot(state: GameState, attackerIdx: Int, target: Coord, lang: String): GameState {
        val defenderIdx = 1 - attackerIdx
        val attacker = state.players[attackerIdx]
        val defender = state.players[defenderIdx]
        val n = state.settings.fieldSize.n
        if (target.x !in 0 until n || target.y !in 0 until n) return state
        if (attacker.shotsAtEnemy.any { it.coord == target }) return state

        val hitShip = defender.ships.firstOrNull {
            !it.sunk && it.placed && it.type != ShipType.Submarine && target in it.cells
        }
        val hit = hitShip != null
        var newState = state.copy(
            players = state.players.mapIndexed { i, p ->
                if (i == attackerIdx) p.copy(shotsAtEnemy = p.shotsAtEnemy + Shot(target, hit)) else p
            }
        )
        if (hitShip != null) {
            val cellIdx = hitShip.cells.indexOf(target)
            val newHits = hitShip.hits.toMutableList().also { it[cellIdx] = true }
            val sunk = newHits.all { it }
            val updated = hitShip.copy(hits = newHits, sunk = sunk)
            newState = newState.copy(
                players = newState.players.mapIndexed { i, p ->
                    if (i == defenderIdx) p.copy(ships = p.ships.map { if (it.id == updated.id) updated else it }) else p
                }
            )

            val coordStr = "${'A' + target.x}${target.y + 1}"
            val logText = if (sunk) {
                val name = updated.name(lang)
                val sunkDesc = Texts.sunkDescription(lang)
                if (attackerIdx == 0) "Admiral! ${updated.type.abbr(lang)} \"$name\" — critical damage! $sunkDesc"
                else "Hit on ${updated.type.abbr(lang)} \"$name\"! Ship sinking. $sunkDesc"
            } else "Hit at $coordStr! ${updated.type.abbr(lang)} (${newHits.count { it }}/${updated.size})"
            newState = newState.copy(
                battleLog = newState.battleLog + LogEntry(
                    state.turn, attackerIdx,
                    if (sunk) LogKind.Sunk else LogKind.Hit, logText
                )
            )

            // Reactor detonation check
            if (updated.reactorIdx == cellIdx && (updated.type == ShipType.Tarkr || updated.type == ShipType.Carrier)) {
                if (Random.nextInt(100) < 50) {
                    newState = triggerNuclearExplosion(newState, defenderIdx, target, 5, lang, log = "REACTOR DETONATION at $coordStr! 5×5 blast!")
                }
            }
            // PU detonation on TARKR
            if (updated.type == ShipType.Tarkr && updated.launchers.any { it.index == cellIdx && it.loaded }) {
                if (Random.nextInt(100) < 15) {
                    newState = triggerNuclearExplosion(newState, defenderIdx, target, 5, lang, log = "LAUNCHER DETONATION at $coordStr! Magazine explosion!")
                }
            }
            // Carrier fuel detonation
            if (updated.type == ShipType.Carrier && cellIdx != updated.reactorIdx && Random.nextInt(100) < 15) {
                newState = triggerNuclearExplosion(newState, defenderIdx, target, 5, lang, log = "AVIATION FUEL EXPLOSION at $coordStr!")
            }
        } else {
            val coordStr = "${'A' + target.x}${target.y + 1}"
            newState = newState.copy(
                battleLog = newState.battleLog + LogEntry(state.turn, attackerIdx, LogKind.Info, "Miss at $coordStr")
            )
        }
        return newState
    }

    fun launchPcr(state: GameState, attackerIdx: Int, launcherIndex: Int, target: Coord, lang: String): GameState {
        val attacker = state.players[attackerIdx]
        val defenderIdx = 1 - attackerIdx
        val defender = state.players[defenderIdx]
        val tarkr = attacker.ships.firstOrNull { !it.sunk && it.type == ShipType.Tarkr } ?: return state
        val launcher = tarkr.launchers.firstOrNull { it.index == launcherIndex && it.loaded && !it.damaged } ?: return state

        val newLaunchers = tarkr.launchers.map { if (it.index == launcherIndex) it.copy(loaded = false) else it }
        val updatedTarkr = tarkr.copy(launchers = newLaunchers)
        var newState = state.copy(
            players = state.players.mapIndexed { i, p ->
                if (i == attackerIdx) p.copy(ships = p.ships.map { if (it.id == tarkr.id) updatedTarkr else it }) else p
            }
        )

        val targetShip = defender.ships.firstOrNull {
            !it.sunk && it.placed && it.type != ShipType.Submarine && target in it.cells
        }
        val coordStr = "${'A' + target.x}${target.y + 1}"
        if (targetShip == null) {
            return newState.copy(
                battleLog = newState.battleLog + LogEntry(state.turn, attackerIdx, LogKind.Info, "PCR Vulkan to $coordStr — empty water, missile lost.")
            )
        }
        val chance = if (targetShip.damaged) targetShip.type.pcrDamagedChance else targetShip.type.pcrIntactChance
        val intercepted = Random.nextInt(100) < chance
        return if (intercepted) {
            newState.copy(
                battleLog = newState.battleLog + LogEntry(
                    state.turn, attackerIdx, LogKind.Info,
                    "PCR intercepted by ${targetShip.type.abbr(lang)} \"${targetShip.name(lang)}\" AA defenses!"
                )
            )
        } else {
            val killed = targetShip.copy(
                hits = List(targetShip.size) { true },
                sunk = true
            )
            newState = newState.copy(
                players = newState.players.mapIndexed { i, p ->
                    if (i == defenderIdx) p.copy(ships = p.ships.map { if (it.id == killed.id) killed else it }) else p
                }
            )
            newState.copy(
                battleLog = newState.battleLog + LogEntry(
                    state.turn, attackerIdx, LogKind.Sunk,
                    "PCR Vulkan struck ${targetShip.type.abbr(lang)} \"${targetShip.name(lang)}\" — DIRECT HIT! ${Texts.sunkDescription(lang)}"
                )
            )
        }
    }

    fun launchTorpedo(state: GameState, attackerIdx: Int, target: Coord, nuclear: Boolean, lang: String): GameState {
        val attacker = state.players[attackerIdx]
        val sub = attacker.ships.firstOrNull { !it.sunk && it.type == ShipType.Submarine && it.deployed } ?: return state
        if (nuclear && sub.nukeTorpedoLeft <= 0) return state
        if (!nuclear && sub.torpedoesLeft <= 0) return state

        val updated = sub.copy(
            torpedoesLeft = if (nuclear) sub.torpedoesLeft else sub.torpedoesLeft - 1,
            nukeTorpedoLeft = if (nuclear) sub.nukeTorpedoLeft - 1 else sub.nukeTorpedoLeft,
            surfacedUntilTurn = state.turn + 1
        )
        val track = TorpedoTrack(
            ownerPlayer = 1 - attackerIdx, attackerPlayer = attackerIdx,
            targetCell = target, currentCell = target, turnsLeft = 2, nuclear = nuclear
        )
        return state.copy(
            players = state.players.mapIndexed { i, p ->
                if (i == attackerIdx) p.copy(ships = p.ships.map { if (it.id == sub.id) updated else it }) else p
            },
            torpedoes = state.torpedoes + track,
            battleLog = state.battleLog + LogEntry(
                state.turn, attackerIdx,
                if (nuclear) LogKind.Nuke else LogKind.Warning,
                if (nuclear) "T-15 nuclear torpedo launched! 3 turns to impact!"
                else "Torpedo launched! 533mm in the water!"
            )
        )
    }

    fun launchStriker(state: GameState, attackerIdx: Int, target: Coord, lang: String): GameState {
        val attacker = state.players[attackerIdx]
        val defenderIdx = 1 - attackerIdx
        val defender = state.players[defenderIdx]
        val carrier = attacker.ships.firstOrNull { !it.sunk && it.type == ShipType.Carrier && !it.avionicsBlocked } ?: return state
        if (carrier.strikersLeft <= 0) return state

        val updated = carrier.copy(strikersLeft = carrier.strikersLeft - 1)
        var newState = state.copy(
            players = state.players.mapIndexed { i, p ->
                if (i == attackerIdx) p.copy(ships = p.ships.map { if (it.id == carrier.id) updated else it }) else p
            }
        )

        val targetShip = defender.ships.firstOrNull {
            !it.sunk && it.placed && it.type != ShipType.Submarine && target in it.cells
        } ?: return newState.copy(
            battleLog = newState.battleLog + LogEntry(state.turn, attackerIdx, LogKind.Info, "Striker found no target.")
        )

        val a = Random.nextInt(100)
        val d = Random.nextInt(100)
        if (a > d) {
            val cellIdx = targetShip.cells.indexOf(target)
            val newHits = targetShip.hits.toMutableList().also { it[cellIdx] = true }
            val sunk = newHits.all { it }
            val ts = targetShip.copy(hits = newHits, sunk = sunk)
            newState = newState.copy(
                players = newState.players.mapIndexed { i, p ->
                    if (i == defenderIdx) p.copy(ships = p.ships.map { if (it.id == ts.id) ts else it }) else p
                },
                battleLog = newState.battleLog + LogEntry(
                    state.turn, attackerIdx,
                    if (sunk) LogKind.Sunk else LogKind.Hit,
                    if (sunk) "Striker direct hit! ${ts.type.abbr(lang)} \"${ts.name(lang)}\" DESTROYED! ${Texts.sunkDescription(lang)}"
                    else "Striker hit on ${ts.type.abbr(lang)} \"${ts.name(lang)}\"! (${newHits.count { it }}/${ts.size})"
                )
            )
            return newState
        }
        return newState.copy(
            battleLog = newState.battleLog + LogEntry(state.turn, attackerIdx, LogKind.Info, "Striker shot down by AA fire!")
        )
    }

    private fun triggerNuclearExplosion(
        state: GameState, victimIdx: Int, center: Coord, radius: Int, lang: String, log: String
    ): GameState {
        var newState = state
        val n = state.settings.fieldSize.n
        val half = radius / 2
        val affectedShips = mutableListOf<Ship>()
        for (dx in -half..half) for (dy in -half..half) {
            val c = Coord(center.x + dx, center.y + dy)
            if (c.x !in 0 until n || c.y !in 0 until n) continue
            val ship = newState.players[victimIdx].ships.firstOrNull { !it.sunk && it.placed && c in it.cells } ?: continue
            if (affectedShips.any { it.id == ship.id }) continue
            affectedShips.add(ship)
        }
        affectedShips.forEach { ship ->
            val killed = ship.copy(hits = List(ship.size) { true }, sunk = true)
            newState = newState.copy(
                players = newState.players.mapIndexed { i, p ->
                    if (i == victimIdx) p.copy(ships = p.ships.map { if (it.id == killed.id) killed else it }) else p
                }
            )
        }
        return newState.copy(
            nuclearZones = newState.nuclearZones + NuclearZone(center, 3),
            battleLog = newState.battleLog + LogEntry(state.turn, 1 - victimIdx, LogKind.Nuke, log)
        )
    }

    fun advanceTorpedoes(state: GameState, lang: String): GameState {
        if (state.torpedoes.isEmpty()) return state
        var newState = state
        val survivors = mutableListOf<TorpedoTrack>()
        for (t in state.torpedoes) {
            val nt = t.copy(turnsLeft = t.turnsLeft - 1)
            if (nt.turnsLeft <= 0) {
                if (nt.nuclear) {
                    newState = triggerNuclearExplosion(newState, nt.ownerPlayer, nt.targetCell, 11, lang, "NUCLEAR DETONATION! T-15 yield 15 kt!")
                } else {
                    val victim = newState.players[nt.ownerPlayer]
                    val ship = victim.ships.firstOrNull { !it.sunk && it.placed && nt.targetCell in it.cells }
                    if (ship != null) {
                        val killed = ship.copy(hits = List(ship.size) { true }, sunk = true)
                        newState = newState.copy(
                            players = newState.players.mapIndexed { i, p ->
                                if (i == nt.ownerPlayer) p.copy(ships = p.ships.map { if (it.id == killed.id) killed else it }) else p
                            },
                            battleLog = newState.battleLog + LogEntry(
                                state.turn, nt.attackerPlayer, LogKind.Sunk,
                                "Torpedo struck ${killed.type.abbr(lang)} \"${killed.name(lang)}\"! ${Texts.sunkDescription(lang)}"
                            )
                        )
                    } else {
                        newState = newState.copy(
                            battleLog = newState.battleLog + LogEntry(state.turn, nt.attackerPlayer, LogKind.Info, "Torpedo missed.")
                        )
                    }
                }
            } else {
                survivors.add(nt)
            }
        }
        return newState.copy(torpedoes = survivors)
    }

    fun checkVictory(state: GameState): Int? {
        val p0Alive = state.players[0].ships.any { !it.sunk && it.placed }
        val p1Alive = state.players[1].ships.any { !it.sunk && it.placed }
        return when {
            !p0Alive && !p1Alive -> null
            !p0Alive -> 1
            !p1Alive -> 0
            else -> null
        }
    }
}
