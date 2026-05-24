package com.elyssov.navalbattle.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

enum class Screen { Menu, Settings, Placement, Battle, NukeRitual, Victory }

class GameViewModel : ViewModel() {

    private val _screen = MutableStateFlow(Screen.Menu)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _state = MutableStateFlow<GameState?>(null)
    val state: StateFlow<GameState?> = _state.asStateFlow()

    private val _lang = MutableStateFlow("ru")
    val lang: StateFlow<String> = _lang.asStateFlow()

    fun setLang(code: String) { _lang.update { code } }

    fun navigate(s: Screen) { _screen.update { s } }

    fun updateSettings(block: (GameSettings) -> GameSettings) { _settings.update(block) }

    fun startNewGame() {
        val s = _settings.value
        val n = s.fieldSize.n
        val terrain = LandscapeGen.generate(n, s.landscape)
        val p0Fleet = Fleet.build(s, _lang.value)
        val p1Fleet = Fleet.build(s, _lang.value)
        _state.update {
            GameState(
                settings = s,
                terrain = terrain,
                players = listOf(
                    PlayerState(0, ships = p0Fleet),
                    PlayerState(1, ships = p1Fleet)
                )
            )
        }
        _screen.update { Screen.Placement }
    }

    fun autoPlaceCurrent(playerIdx: Int) {
        val st = _state.value ?: return
        val n = st.settings.fieldSize.n
        val placed = Fleet.autoPlace(st.players[playerIdx].ships, n, st.terrain) ?: return
        _state.update { current ->
            current?.copy(
                players = current.players.mapIndexed { i, p ->
                    if (i == playerIdx) p.copy(ships = placed) else p
                }
            )
        }
    }

    fun clearPlacement(playerIdx: Int) {
        _state.update { current ->
            current?.copy(
                players = current.players.mapIndexed { i, p ->
                    if (i == playerIdx) p.copy(
                        ships = p.ships.map { it.copy(cells = emptyList(), placed = false) }
                    ) else p
                }
            )
        }
    }

    fun replaceState(s: GameState) { _state.update { s } }

    fun goToBattle() {
        val st = _state.value ?: return
        // Auto-place AI opponent
        val n = st.settings.fieldSize.n
        val aiPlaced = Fleet.autoPlace(st.players[1].ships, n, st.terrain) ?: st.players[1].ships
        val ready = st.copy(
            phase = GamePhase.Battle,
            players = st.players.mapIndexed { i, p ->
                if (i == 1) p.copy(ships = aiPlaced) else p
            }
        )
        _state.update { ready }
        _screen.update { Screen.Battle }
    }

    fun fireShot(target: Coord) {
        val st = _state.value ?: return
        if (st.phase != GamePhase.Battle) return
        val after = Combat.regularShot(st, st.currentPlayer, target, _lang.value)
        _state.update { after }
        checkWinner()
    }

    fun endTurn() {
        var st = _state.value ?: return
        if (st.phase != GamePhase.Battle) return
        st = Combat.advanceTorpedoes(st, _lang.value)
        st = Recon.ageReconHits(st)
        st = Recon.decrementCooldowns(st, st.currentPlayer)
        val nextPlayer = 1 - st.currentPlayer
        val nextTurn = if (nextPlayer == 0) st.turn + 1 else st.turn
        st = st.copy(currentPlayer = nextPlayer, turn = nextTurn)
        _state.update { st }
        checkWinner()

        // AI auto-play if it's AI's turn
        if (st.settings.mode == GameMode.Ai && st.currentPlayer == 1 && st.phase == GamePhase.Battle) {
            aiTakeTurn()
        }
    }

    private val _nuclearTargeting = MutableStateFlow(false)
    val nuclearTargeting: StateFlow<Boolean> = _nuclearTargeting.asStateFlow()

    fun startNukeRitual() {
        val st = _state.value ?: return
        if (st.players[st.currentPlayer].nuclearUsed) return
        if (st.settings.fieldSize == FieldSize.Skirmish) return
        _screen.update { Screen.NukeRitual }
    }

    fun beginNuclearTargeting() {
        _nuclearTargeting.update { true }
        _screen.update { Screen.Battle }
    }

    fun launchNuke(center: Coord) {
        val st = _state.value ?: return
        if (st.players[st.currentPlayer].nuclearUsed) return
        // Direct nuclear strike (radius 11) on enemy field
        val after = applyNuclearStrike(st, 1 - st.currentPlayer, center, 11, _lang.value)
            .let { s ->
                s.copy(players = s.players.mapIndexed { i, p ->
                    if (i == st.currentPlayer) p.copy(nuclearUsed = true) else p
                })
            }
        _state.update { after }
        _nuclearTargeting.update { false }
        checkWinner()
    }

    private fun applyNuclearStrike(
        state: GameState, victimIdx: Int, center: Coord, radius: Int, lang: String
    ): GameState {
        val n = state.settings.fieldSize.n
        val half = radius / 2
        val affectedShips = mutableSetOf<String>()
        for (dx in -half..half) for (dy in -half..half) {
            val c = Coord(center.x + dx, center.y + dy)
            if (c.x !in 0 until n || c.y !in 0 until n) continue
            state.players[victimIdx].ships.firstOrNull { !it.sunk && it.placed && c in it.cells }
                ?.let { affectedShips.add(it.id) }
        }
        val newPlayers = state.players.mapIndexed { i, p ->
            if (i == victimIdx) p.copy(
                ships = p.ships.map { s ->
                    if (s.id in affectedShips) s.copy(hits = List(s.size) { true }, sunk = true) else s
                }
            ) else p
        }
        val coordStr = "${'A' + center.x}${center.y + 1}"
        return state.copy(
            players = newPlayers,
            nuclearZones = state.nuclearZones + NuclearZone(center, 3),
            battleLog = state.battleLog + LogEntry(
                state.turn, 1 - victimIdx, LogKind.Nuke,
                "Σ☢ NUCLEAR DETONATION at $coordStr! Yield — 15 kilotons! ${affectedShips.size} ships vaporized."
            )
        )
    }

    private fun aiTakeTurn() {
        val st = _state.value ?: return
        val after = Ai.takeTurn(st, _lang.value)
        _state.update { after }
        checkWinner()
        // End AI turn
        var s = _state.value ?: return
        s = Combat.advanceTorpedoes(s, _lang.value)
        s = Recon.ageReconHits(s)
        s = Recon.decrementCooldowns(s, s.currentPlayer)
        s = s.copy(currentPlayer = 0, turn = s.turn + 1)
        _state.update { s }
        checkWinner()
    }

    private fun checkWinner() {
        val st = _state.value ?: return
        val w = Combat.checkVictory(st) ?: return
        _state.update { it?.copy(winner = w, phase = GamePhase.Finished) }
        _screen.update { Screen.Victory }
    }

    fun backToMenu() {
        _state.update { null }
        _screen.update { Screen.Menu }
    }
}
