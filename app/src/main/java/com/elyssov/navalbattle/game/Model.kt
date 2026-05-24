package com.elyssov.navalbattle.game

import kotlinx.serialization.Serializable

@Serializable
data class Coord(val x: Int, val y: Int) {
    operator fun plus(o: Coord) = Coord(x + o.x, y + o.y)
}

enum class Orientation { Horizontal, Vertical }

@Serializable
data class Launcher(val index: Int, val loaded: Boolean = true, val damaged: Boolean = false)

@Serializable
data class Ship(
    val id: String,
    val type: ShipType,
    val russianName: String,
    val englishName: String,
    val crew: Int,
    val cells: List<Coord> = emptyList(),
    val orientation: Orientation = Orientation.Horizontal,
    val hits: List<Boolean> = List(type.size) { false },
    val sunk: Boolean = false,
    val placed: Boolean = false,
    val reactorIdx: Int = if (type == ShipType.Tarkr || type == ShipType.Carrier) 2 else -1,
    val launchers: List<Launcher> = if (type == ShipType.Tarkr)
        listOf(Launcher(0), Launcher(1), Launcher(3), Launcher(4)) else emptyList(),
    val planesLeft: Int = if (type == ShipType.Carrier) 3 else 0,
    val strikersLeft: Int = if (type == ShipType.Carrier) 6 else 0,
    val torpedoesLeft: Int = if (type == ShipType.Submarine) 3 else 0,
    val nukeTorpedoLeft: Int = if (type == ShipType.Submarine) 1 else 0,
    val asrocLeft: Int = if (type == ShipType.Destroyer) 1 else 0,
    val radarCooldown: Int = 0,
    val sonarCooldown: Int = 0,
    val avionicsBlocked: Boolean = false,
    val deployed: Boolean = type != ShipType.Submarine,
    val surfacedUntilTurn: Int = -1,
    val radiationDamage: Boolean = false
) {
    fun name(lang: String) = if (lang == "ru") russianName else englishName
    val damaged get() = hits.any { it }
    val size get() = type.size
}

@Serializable
data class TerrainCell(val coord: Coord, val kind: TerrainKind)

enum class TerrainKind { Island, Wreck }

@Serializable
data class Shot(val coord: Coord, val hit: Boolean)

@Serializable
data class ReconHit(val shipId: String, val coord: Coord, val turnSeen: Int)

@Serializable
data class TorpedoTrack(
    val ownerPlayer: Int,
    val attackerPlayer: Int,
    val targetCell: Coord,
    val currentCell: Coord,
    val turnsLeft: Int,
    val nuclear: Boolean = false
)

@Serializable
data class NuclearZone(val center: Coord, val turnsLeft: Int)

@Serializable
data class PlayerState(
    val index: Int,
    val ships: List<Ship> = emptyList(),
    val shotsAtEnemy: List<Shot> = emptyList(),
    val reconHits: List<ReconHit> = emptyList(),
    val nuclearUsed: Boolean = false
)

@Serializable
data class GameState(
    val settings: GameSettings,
    val terrain: List<TerrainCell> = emptyList(),
    val players: List<PlayerState> = listOf(PlayerState(0), PlayerState(1)),
    val turn: Int = 1,
    val currentPlayer: Int = 0,
    val torpedoes: List<TorpedoTrack> = emptyList(),
    val nuclearZones: List<NuclearZone> = emptyList(),
    val battleLog: List<LogEntry> = emptyList(),
    val phase: GamePhase = GamePhase.Placement,
    val winner: Int? = null
)

enum class GamePhase { Placement, Battle, Finished }

enum class LogKind { Info, Detection, Hit, Sunk, Reactor, Nuke, Warning }

@Serializable
data class LogEntry(val turn: Int, val player: Int, val kind: LogKind, val text: String)
