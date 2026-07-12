package com.elyssov.navalbattle.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class Screen { Menu, Settings, Help, Placement, Battle, NukeRitual, Victory }

enum class BattleMode { Fire, Move, Radar, AirRecon, Pcr, Torpedo, NukeTorpedo, Striker, DeploySub }

class GameViewModel : ViewModel() {

    private val _screen = MutableStateFlow(Screen.Menu)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _state = MutableStateFlow<GameState?>(null)
    val state: StateFlow<GameState?> = _state.asStateFlow()

    private val _lang = MutableStateFlow(detectInitialLang())
    val lang: StateFlow<String> = _lang.asStateFlow()

    companion object {
        private fun detectInitialLang(): String {
            // EN by default. Switch to RU only if the device locale is Russian.
            val sys = java.util.Locale.getDefault().language
            return if (sys == "ru") "ru" else "en"
        }
    }

    private val _mode = MutableStateFlow(BattleMode.Fire)
    val mode: StateFlow<BattleMode> = _mode.asStateFlow()

    private val _selectedShipId = MutableStateFlow<String?>(null)
    val selectedShipId: StateFlow<String?> = _selectedShipId.asStateFlow()

    private val _pcrLauncherIdx = MutableStateFlow(-1)
    val pcrLauncherIdx: StateFlow<Int> = _pcrLauncherIdx.asStateFlow()

    private val _handoffPending = MutableStateFlow(false)
    val handoffPending: StateFlow<Boolean> = _handoffPending.asStateFlow()

    private val _placingPlayer = MutableStateFlow(0)
    val placingPlayer: StateFlow<Int> = _placingPlayer.asStateFlow()

    /** True once the current player has spent their single offensive/movement
     *  action this turn (fire / PCR / torpedo / striker / nuke / move / rotate).
     *  Reset on every turn change. The Help is explicit — "a maneuvering ship
     *  doesn't fire" — one action per turn. Without this, Fire had no per-turn
     *  limit and a player could carpet the whole enemy grid in one turn while the
     *  AI took a single action: the game was trivially winnable. */
    private val _actedThisTurn = MutableStateFlow(false)
    val actedThisTurn: StateFlow<Boolean> = _actedThisTurn.asStateFlow()

    fun setLang(code: String) {
        _lang.update { code }
        // Per-app locale: recreates Activity, strings.xml reloads from values-<lang>/.
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(code)
        )
    }
    fun navigate(s: Screen) { _screen.update { s } }
    fun updateSettings(block: (GameSettings) -> GameSettings) { _settings.update(block) }
    fun setMode(m: BattleMode) {
        _mode.update { m }
        _selectedShipId.update { null }
        _pcrLauncherIdx.update { -1 }
    }
    fun selectShip(id: String?) { _selectedShipId.update { id } }
    fun selectPcrLauncher(idx: Int) { _pcrLauncherIdx.update { idx } }

    fun startNewGame() {
        val s = _settings.value
        val n = s.fieldSize.n
        val terrain = LandscapeGen.generate(n, s.landscape)
        val p0Fleet = Fleet.build(s, _lang.value)
        val p1Fleet = Fleet.build(s, _lang.value)
        _state.update {
            GameState(
                settings = s, terrain = terrain,
                players = listOf(
                    PlayerState(0, ships = p0Fleet),
                    PlayerState(1, ships = p1Fleet)
                )
            )
        }
        _placingPlayer.update { 0 }
        _actedThisTurn.update { false }
        _screen.update { Screen.Placement }
    }

    fun autoPlaceCurrent(playerIdx: Int) {
        val st = _state.value ?: return
        val n = st.settings.fieldSize.n
        val placed = Fleet.autoPlace(st.players[playerIdx].ships, n, st.terrain) ?: return
        _state.update { current ->
            current?.copy(players = current.players.mapIndexed { i, p ->
                if (i == playerIdx) p.copy(ships = placed) else p
            })
        }
    }

    fun clearPlacement(playerIdx: Int) {
        _state.update { current ->
            current?.copy(players = current.players.mapIndexed { i, p ->
                if (i == playerIdx) p.copy(
                    ships = p.ships.map { it.copy(cells = emptyList(), placed = false) }
                ) else p
            })
        }
    }

    fun replaceState(s: GameState) { _state.update { s } }

    fun finishPlacement() {
        val st = _state.value ?: return
        if (st.settings.mode == GameMode.Hotseat) {
            val current = _placingPlayer.value
            if (current == 0) {
                _placingPlayer.update { 1 }
                _handoffPending.update { true }
                return
            }
        }
        // AI placement (or hotseat both done)
        val n = st.settings.fieldSize.n
        val newState: GameState? = if (st.settings.mode == GameMode.Ai) {
            // Retry auto-placement (each attempt reshuffles). NEVER fall back to the
            // unplaced fleet and enter Battle: placed=false ships are invisible to
            // checkVictory, so the player would win instantly on their first shot
            // against an "already dead" AI. If placement genuinely can't succeed on
            // this board, stay in Placement rather than start a broken match.
            var aiPlaced = Fleet.autoPlace(st.players[1].ships, n, st.terrain)
            var tries = 0
            while (aiPlaced == null && tries < 20) {
                aiPlaced = Fleet.autoPlace(st.players[1].ships, n, st.terrain)
                tries++
            }
            aiPlaced?.let { placed ->
                st.copy(
                    phase = GamePhase.Battle,
                    players = st.players.mapIndexed { i, p -> if (i == 1) p.copy(ships = placed) else p }
                )
            }
        } else {
            st.copy(phase = GamePhase.Battle)
        }
        if (newState == null) return
        _state.update { newState }
        _screen.update { Screen.Battle }
        _placingPlayer.update { 0 }
    }

    fun completeHandoff() { _handoffPending.update { false } }

    fun goToBattle() = finishPlacement()

    // ── Battle actions ──

    fun fireShot(target: Coord) {
        val st = _state.value ?: return
        if (st.phase != GamePhase.Battle) return
        if (_actedThisTurn.value) return
        val after = Combat.regularShot(st, st.currentPlayer, target, _lang.value)
        if (after !== st) _actedThisTurn.update { true }   // a shot was actually recorded
        _state.update { after }
        setMode(BattleMode.Fire)
        checkWinner()
    }

    fun useRadar(shipId: String) {
        val st = _state.value ?: return
        val after = Recon.radarSweep(st, st.currentPlayer, shipId, _lang.value)
        _state.update { after }
        setMode(BattleMode.Fire)
    }

    fun useAirRecon() {
        val st = _state.value ?: return
        val after = Recon.airRecon(st, st.currentPlayer, _lang.value)
        _state.update { after }
        setMode(BattleMode.Fire)
    }

    fun launchPcr(target: Coord) {
        val st = _state.value ?: return
        if (_actedThisTurn.value) return
        val launcherIdx = _pcrLauncherIdx.value
        if (launcherIdx < 0) return
        val after = Combat.launchPcr(st, st.currentPlayer, launcherIdx, target, _lang.value)
        if (after !== st) _actedThisTurn.update { true }
        _state.update { after }
        setMode(BattleMode.Fire)
        checkWinner()
    }

    fun launchTorpedo(target: Coord, nuclear: Boolean = false) {
        val st = _state.value ?: return
        if (_actedThisTurn.value) return
        val after = Combat.launchTorpedo(st, st.currentPlayer, target, nuclear, _lang.value)
        if (after !== st) _actedThisTurn.update { true }
        _state.update { after }
        setMode(BattleMode.Fire)
    }

    fun launchStriker(target: Coord) {
        val st = _state.value ?: return
        if (_actedThisTurn.value) return
        val after = Combat.launchStriker(st, st.currentPlayer, target, _lang.value)
        if (after !== st) _actedThisTurn.update { true }
        _state.update { after }
        setMode(BattleMode.Fire)
        checkWinner()
    }

    fun deploySubAt(anchor: Coord, ori: Orientation) {
        val st = _state.value ?: return
        // Deploy window is turns 1..10 (Help). After that the sub is written off.
        if (st.turn > 10) return
        val playerIdx = st.currentPlayer
        val sub = st.players[playerIdx].ships.firstOrNull { it.type == ShipType.Submarine && !it.deployed && !it.sunk } ?: return
        val cells = Fleet.cellsFor(anchor.x, anchor.y, sub.size, ori)
        val n = st.settings.fieldSize.n
        if (cells.any { it.x !in 0 until n || it.y !in 0 until n }) return
        // Submarine: cannot be on islands, can be on wrecks; no overlap with own surface ships' cells with spacing
        val islandSet = st.terrain.filter { it.kind == TerrainKind.Island }.map { it.coord }.toSet()
        if (cells.any { it in islandSet }) return
        val ownOccupied = mutableSetOf<Coord>()
        st.players[playerIdx].ships.filter { it.placed && it.id != sub.id }
            .forEach { it.cells.forEach { c -> for (dx in -1..1) for (dy in -1..1) ownOccupied += Coord(c.x + dx, c.y + dy) } }
        if (cells.any { it in ownOccupied }) return
        val updated = sub.copy(cells = cells, orientation = ori, deployed = true, placed = true)
        val newState = st.copy(
            players = st.players.mapIndexed { i, p ->
                if (i == playerIdx) p.copy(ships = p.ships.map { if (it.id == sub.id) updated else it }) else p
            },
            battleLog = st.battleLog + LogEntry(st.turn, playerIdx, LogKind.Info, "Submarine \"${sub.name(_lang.value)}\" deployed. Diving to operating depth.")
        )
        _state.update { newState }
        setMode(BattleMode.Fire)
    }

    /** Returns true only if the ship actually moved, so the caller ends the turn
     *  only on a successful move (a rejected move must not silently forfeit a turn). */
    fun moveShip(shipId: String, dist: Int): Boolean {
        val st = _state.value ?: return false
        if (_actedThisTurn.value) return false
        val ship = st.players[st.currentPlayer].ships.firstOrNull { it.id == shipId } ?: return false
        val (ax, ay) = if (ship.orientation == Orientation.Horizontal) 1 to 0 else 0 to 1
        val after = Movement.moveAlongAxis(st, st.currentPlayer, shipId, ax * dist, ay * dist) ?: return false
        _actedThisTurn.update { true }
        _state.update { after }
        setMode(BattleMode.Fire)
        return true
    }

    /** Returns true only if the ship actually rotated (see moveShip). */
    fun rotateShip(shipId: String): Boolean {
        val st = _state.value ?: return false
        if (_actedThisTurn.value) return false
        val after = Movement.rotate(st, st.currentPlayer, shipId) ?: return false
        _actedThisTurn.update { true }
        _state.update { after }
        setMode(BattleMode.Fire)
        return true
    }

    fun endTurn() {
        var st = _state.value ?: return
        if (st.phase != GamePhase.Battle) return
        st = Combat.advanceTorpedoes(st, _lang.value)
        st = Recon.ageReconHits(st)
        st = Recon.decrementCooldowns(st, st.currentPlayer)
        val nextPlayer = 1 - st.currentPlayer
        val nextTurn = if (nextPlayer == 0) st.turn + 1 else st.turn
        // Crossing out of turn 10 (10 -> 11): any submarine never deployed is written
        // off as lost, per the Help ("deploy by the end of turn ten — or she's lost").
        if (st.turn == 10 && nextPlayer == 0) {
            val newLogs = mutableListOf<LogEntry>()
            val newPlayers = st.players.mapIndexed { idx, p ->
                if (p.ships.none { it.type == ShipType.Submarine && !it.deployed && !it.sunk }) p
                else {
                    newLogs += LogEntry(st.turn, idx, LogKind.Warning, "Submarine never deployed by turn 10 — written off as lost.")
                    p.copy(ships = p.ships.map {
                        if (it.type == ShipType.Submarine && !it.deployed && !it.sunk) it.copy(sunk = true) else it
                    })
                }
            }
            st = st.copy(players = newPlayers, battleLog = st.battleLog + newLogs)
        }
        st = st.copy(currentPlayer = nextPlayer, turn = nextTurn)
        _state.update { st }
        setMode(BattleMode.Fire)
        _actedThisTurn.update { false }
        checkWinner()

        // Re-read the phase AFTER checkWinner (which mutates _state). The local `st`
        // snapshot still reads Battle even when advanceTorpedoes just sank the last
        // ship, so the old `st.phase` guard let the AI take a full turn on a game that
        // was already finished — shooting behind the Victory screen.
        val phaseNow = _state.value?.phase
        if (st.settings.mode == GameMode.Ai && st.currentPlayer == 1 && phaseNow == GamePhase.Battle) {
            aiTakeTurn()
        } else if (st.settings.mode == GameMode.Hotseat && phaseNow == GamePhase.Battle) {
            _handoffPending.update { true }
        }
    }

    // ── Nuclear ──
    private val _nuclearTargeting = MutableStateFlow(false)
    val nuclearTargeting: StateFlow<Boolean> = _nuclearTargeting.asStateFlow()

    fun startNukeRitual() {
        val st = _state.value ?: return
        if (st.players[st.currentPlayer].nuclearUsed) return
        if (st.settings.fieldSize == FieldSize.Skirmish) return
        // T-15 is a submarine-launched nuclear torpedo: require a deployed sub with charge
        val hasNukeSub = st.players[st.currentPlayer].ships.any {
            it.type == ShipType.Submarine && it.deployed && !it.sunk && it.nukeTorpedoLeft > 0
        }
        if (!hasNukeSub) return
        _screen.update { Screen.NukeRitual }
    }

    fun beginNuclearTargeting() {
        _nuclearTargeting.update { true }
        _screen.update { Screen.Battle }
    }

    fun launchNuke(center: Coord) {
        val st = _state.value ?: return
        if (st.players[st.currentPlayer].nuclearUsed) return
        val after = applyNuclearStrike(st, 1 - st.currentPlayer, center, 11, _lang.value)
            .let { s ->
                s.copy(players = s.players.mapIndexed { i, p ->
                    if (i == st.currentPlayer) p.copy(nuclearUsed = true) else p
                })
            }
        _state.update { after }
        _nuclearTargeting.update { false }
        _actedThisTurn.update { true }
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
        val coordStr = coordLabel(center.x, center.y)
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
        // If the AI's action won the game, stop — don't advance torpedoes / flip the
        // turn / re-run checkWinner on an already-finished match.
        if (_state.value?.phase != GamePhase.Battle) return
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
        setMode(BattleMode.Fire)
        _handoffPending.update { false }
    }
}
