package pw.aru.game.battleroyale

import pw.aru.game.battleroyale.logic.BattleRoyaleMatch
import pw.aru.game.battleroyale.player.PlayerInfo
import kotlin.math.pow

data class BattleRoyale(
    val environiment: BattleRoyaleEnvironment,
    val players: List<PlayerInfo>,
    val threshold: Double
) {
    init {
        check(threshold > 0 && threshold < 1) { "Threshold needs to be between 0 and 1." }
    }

    val thresholdRt4 = threshold.pow(0.125).floor(100.0)
    val targetAmount = players.size

    fun newGame() = BattleRoyaleMatch(this)
}