package com.elyssov.navalbattle.game

import kotlinx.serialization.Serializable

enum class FieldSize(val n: Int) { Skirmish(30), Squadron(50), Admiral(100) }
enum class Landscape { Ocean, Wrecks, Islands, Archipelago }
enum class GameMode { Ai, Hotseat }
enum class CapitalChoice { Tarkr, Carrier }
enum class AiDifficulty { Easy, Medium, Hard }

@Serializable
data class GameSettings(
    val fieldSize: FieldSize = FieldSize.Squadron,
    val landscape: Landscape = Landscape.Ocean,
    val mode: GameMode = GameMode.Ai,
    val capital: CapitalChoice = CapitalChoice.Tarkr,
    val pcrIntercept: Boolean = false,
    val aiDifficulty: AiDifficulty = AiDifficulty.Medium,
    val soundOn: Boolean = true
)
